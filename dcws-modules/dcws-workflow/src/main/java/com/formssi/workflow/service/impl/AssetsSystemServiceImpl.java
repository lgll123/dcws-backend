package com.formssi.workflow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.AssetsSystemBo;
import com.formssi.workflow.domain.bo.CategoryBo;
import com.formssi.workflow.service.IAssetsSystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 物料申请Service业务层处理
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class AssetsSystemServiceImpl implements IAssetsSystemService {
    private static final String API_URL = "http://10.100.218.4/api/v1/";
    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiMDE1YTgxM2ZhMGFlNzkxMzc3Mzc1MmEzZmMxYjc2MzU1NjAxZDU0NzZmZDk3YjJlNzgwMTA5NjE5ZTA2N2Y3MWUzMTM0YTI3OTU1MmFiYmUiLCJpYXQiOjE3Mzg4OTkzNzAuMjIxMjAxLCJuYmYiOjE3Mzg4OTkzNzAuMjIxMjA0LCJleHAiOjIyMTIxOTg1NzAuMjEwNzU5LCJzdWIiOiI3NDEiLCJzY29wZXMiOltdfQ.0hcozE2jwP7nkrt3oKLrF4sG8R2NSva3cVJHRzdM13cyLscGs4-J6IXoZnZc29CSWJJ3uedINH8bfG-vixFDCjZop3C800LnjSz4y4zwvP3-tqAy9PbufEvJYJW4X-84jRHQY14XWcRjM2LE1gleW6yiJfAZV4X3BqXoH3p6jjMlyP9AQAvRjqWMfnEv5gquj2_CJCXjHr-oaPvJDDQccFUiRLWS3vFq7r2ePqj4KhpEmheCwup5lcxJfZnMxIC6eIQmFqOQMqyFON2ukSOeqZsEdFVD__2TGlbMHsc2uGZDAHI3zRY8vZnYQvq4elNXKTkCNapmzARdKOXBMh_i0T3Qu9RQ7sdqdIGDPksp78SaXVsD-JtAn4m6RyEeZEwYV_UWXZDgcj2AF_PmmXgLPQjo-SMl6V0mSNDIVn8LYen_oLvU5Z8MzzbrgQO9nww3XO7Mp0CTWz0y643mFBdkFdYrPeVstoO38Y5Mn7fvwo07MOGuKYtPfP5vbGq8qT0QqJjw3J7swCIZQCAjsp1Mu8yMnTdcV37Qw_e4iqIsOKOjUciJ-H5EfLjU3b13l5mcvGZImEW3mkhC32lqO4Gmw1egM2rkbfW57ZWY6CFX_8ehuOD0vi3uFKnC0mzlfYFcaMrlbwOO5SBgBzp0jSuB9zudn1wP1iD0-8MHKAfhilY";
    private final OkHttpClient client = new OkHttpClient();
    @Override
    public List<Map<String,Object>> queryRepertory(AssetsSystemBo bo,String urlPath) {
        try {
            log.info("Calling the external system for Assets with URL: {}", API_URL);
            String name = StringUtils.isBlank(bo.getName()) ? "" : bo.getName();
            String modelNo = StringUtils.isBlank(bo.getModelNo()) ? "" : bo.getModelNo();

            String requestData = urlPath+"?search="+name+"&limit=50&offset=0&order_number=null&sort=created_at&order=desc&expand=false";
            Request request = new Request.Builder()
                .url(API_URL+ requestData)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", BEARER_TOKEN)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    log.info("External system response: {}", responseBody);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map map = objectMapper.readValue(responseBody, Map.class);
                    List<Map<String,Object>> rows = (List<Map<String,Object>>)map.get("rows");
                    Integer remainingQty = (Integer)rows.get(0).get("remaining_qty");
                    if(rows.isEmpty() || rows.size()>1){
                        throw new ServiceException( "库存查询失败：物料名称或物料型号不正确，result size:  "+rows.size());
                    }
                    return rows;
                } else {
                    log.error("External system call failed with status code: {}", response.code());
                    throw new ServiceException( "调用外部接口失败: "+response.toString());
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
            throw new ServiceException( "调用外部接口失败: " + e.getMessage());
        }

    }




    @Override
    public AssetsSystemBo queryQtyById(AssetsSystemBo bo,String urlPath) {
        try {
            log.info("Calling the external system for Assets with URL: {}", API_URL);
            String requestData = urlPath+"/" + bo.getId().toString();
            Request request = new Request.Builder()
                .url(API_URL+ requestData)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", BEARER_TOKEN)
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    log.info("External system response: {}", responseBody);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map map = objectMapper.readValue(responseBody, Map.class);
                    AssetsSystemBo assetsSystemBo = new AssetsSystemBo();
                    assetsSystemBo.setId((Integer)map.get("id"));
                    assetsSystemBo.setQty((Integer)map.get("qty"));
                    assetsSystemBo.setRemainQty((Integer)map.get("remaining_qty"));
                    assetsSystemBo.setCheckoutsCount((Integer)map.get("checkouts_count"));
                    return assetsSystemBo;
                } else {
                    log.error("External system call failed with status code: {}", response.code());
                    throw new ServiceException( "调用外部接口失败: "+response.toString());
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
            throw new ServiceException( "调用外部接口失败: " + e.getMessage());
        }

    }


    @Override
    public TableDataInfo<CategoryBo> queryPageCategories(String categoryType, PageQuery pageQuery, String urlPath) {
        try {
            log.info("Calling the external system for Assets with URL: {}", API_URL);
            String requestData = urlPath+"?category_type" + categoryType+"&search="+"&limit="+pageQuery.getPageSize()+"&offset="+pageQuery.getPageSize()*pageQuery.getPageNum()+"&order="+pageQuery.getIsAsc();
            Request request = new Request.Builder()
                    .url(API_URL+ requestData)
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("Authorization", BEARER_TOKEN)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    log.info("External system response: {}", responseBody);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map map = objectMapper.readValue(responseBody, Map.class);
                    List<Map<String,Object>> rows = (List<Map<String,Object>>)map.get("rows");
                    Integer total = (Integer)map.get("total");
                    List<CategoryBo> categoryBos = new ArrayList<>();
                    rows.forEach(t->{
                        CategoryBo categoryBo = new CategoryBo();
                        categoryBo.setId((Integer)t.get("id"));
                        categoryBo.setCategoryType((String)t.get("category_type"));
                        categoryBo.setName((String)t.get("name"));
                        categoryBos.add(categoryBo);
                    });
                    return new TableDataInfo<>(categoryBos,Long.parseLong(String.valueOf(total)));
                } else {
                    log.error("External system call failed with status code: {}", response.code());
                    throw new ServiceException( "调用外部接口失败: "+response.toString());
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
            throw new ServiceException( "调用外部接口失败: " + e.getMessage());
        }
    }

    @Override
    public TableDataInfo<AssetsSystemBo> queryAccessoriesById(Integer categoryId, PageQuery pageQuery, String urlPath) {
        try {
            log.info("Calling the external system for Assets with URL: {}", API_URL);

            String requestData = urlPath+"?category_id" + categoryId+"&search="+"&limit="+pageQuery.getPageSize()
                    +"&offset="+pageQuery.getPageSize()*pageQuery.getPageNum()+"&order="+pageQuery.getIsAsc();
            if("hardware".equals(urlPath)){
                requestData+="&status=Requestable";
            }
            Request request = new Request.Builder()
                    .url(API_URL+ requestData)
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader("Authorization", BEARER_TOKEN)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    log.info("External system response: {}", responseBody);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map map = objectMapper.readValue(responseBody, Map.class);
                    List<Map<String,Object>> rows = (List<Map<String,Object>>)map.get("rows");
                    Integer total = (Integer)map.get("total");
                    List<AssetsSystemBo> assetsSystemBos = new ArrayList<>();
                    if("hardware".equals(urlPath)){
                        rows.forEach(t->{
                            AssetsSystemBo assetsSystemBo = new AssetsSystemBo();
                            assetsSystemBo.setId((Integer)t.get("id"));
                            assetsSystemBo.setName((String)t.get("name"));
                            assetsSystemBo.setModelNo(StringUtils.isBlank((String)t.get("model_number"))?(String)((Map<String,Object>)t.get("model")).get("name"):(String)t.get("model_number"));
                            assetsSystemBo.setAssetTag((String)t.get("asset_tag"));
                            assetsSystemBo.setAssetStatus((String)((Map<String,Object>)t.get("status_label")).get("status_type"));
                            assetsSystemBos.add(assetsSystemBo);
                        });
                    }else {
                        rows.forEach(t->{
                            AssetsSystemBo assetsSystemBo = new AssetsSystemBo();
                            assetsSystemBo.setId((Integer)t.get("id"));
                            assetsSystemBo.setName((String)t.get("name"));
                            assetsSystemBo.setModelNo((String)t.get("model_number"));
                            assetsSystemBo.setQty((Integer)t.get("qty"));
                            assetsSystemBo.setRemainQty((Integer)t.get("remaining_qty"));
                            assetsSystemBo.setCheckoutsCount((Integer)t.get("checkouts_count"));
                            assetsSystemBos.add(assetsSystemBo);
                        });
                    }

                    return new TableDataInfo<>(assetsSystemBos,Long.parseLong(String.valueOf(total)));
                } else {
                    log.error("External system call failed with status code: {}", response.code());
                    throw new ServiceException( "调用外部接口失败: "+response.toString());
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
            throw new ServiceException( "调用外部接口失败: " + e.getMessage());
        }
    }
}
