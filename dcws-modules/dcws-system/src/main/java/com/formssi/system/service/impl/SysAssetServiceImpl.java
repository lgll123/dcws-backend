package com.formssi.system.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.system.domain.AjaxResult;
import com.formssi.system.domain.vo.HrDeptVo;
import com.formssi.system.service.ISysAssetService;
import com.formssi.system.service.ISysHrService;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import okhttp3.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.dev33.satoken.SaManager.log;

/**
 * 调用资产系统相关接口
 */
@Service
@RequiredArgsConstructor
public class SysAssetServiceImpl implements ISysAssetService {
    private static final String API_URL = "http://10.101.68.29:8000/api/v1/";
    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiNDRlMWQ2OTA0ZjM3NTQyNDJmZmU5ZTMxOTk0MjZiNzI1MDRkYzNhMmM4ODhhNTIxZWFjNDdlNDA4MjliZWUxZjBmNWRhOGFhYTA2MDZmMjIiLCJpYXQiOjE3NDAwNDEwNzUuNzY0Nzg5LCJuYmYiOjE3NDAwNDEwNzUuNzY0NzkxLCJleHAiOjMwMDIzNDUwNzUuNzYyMjEzLCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.TowtjDbbsNkWvdLxz16s69Cj3zA1KF8iioV3jOF26W6SwjP8JWA1r4Fvk71rcOXNTlETeZMS-xmFgjeXyZHYOcqeizIOUXwpdppsBXKuzTH8z2XaZKrpYLNVJrrip1hZEYC_xdln6vapWHRtxwVYLM-L-yzz3Ytc97_afab7f5El6-WxsIvPy7lBIhfexwICweOTU-7EgRdyA5DO21Y5YVC6yBGQBb4U4GRqxhNM_WjsKg4msH_MT19FzEBY0JLgIPzDF1sS66IywsGRJr-yBy6cSdk9h4aOxjprSuWZYaI9Lk_B1jHkcE58EB7Tjvrw_jiOofPoUVSPTxx_wGIhpk8Lz_3Yyb-Jzimp59JxbWaek5TPbxcLEaElrsL2MFY6mgbWpAlzdxQTgYzG5VkY9wjqw08NOklGhMIAtukX4-fwK2dHLu_AiyFNojYKcOinCNKb-ybeSIyG-hE9Tk9Iw4H0h2B5RH3TA64GRCwjl1y1RIgurxUR7peKFABxGpNBJ-63R90byn9Q5GXzndtOp9UZplYLg4XME7jt7yarZJUMjIFnW8ef938UTzg2Gig7HetrKeHEE_-Vl8RDFcpYpRn29QxTOrkriSX6SNu8SdH_zE-sL9NStHz0BfwI6jfb96gFretBzArxs2tVcUHZysxwTn-LkenpMcHX4owrNCU";
    private final OkHttpClient client = new OkHttpClient();


    /**
     * 查询部门列表
     * @return
     */
    public List<Map<String, Object>> selectDeptList() {
        String apiUrl = API_URL + "departments?order=asc" ;
        List<Map<String, Object>> depList = callGetApi(apiUrl);//调用接口
        return depList;
    }


    /**
     * 新增部门信息
     * @return
     */
    public void insertDeptFromOa(List<HrDeptVo> oaDeptList ) {
        String apiUrl = API_URL + "departments";
        ObjectMapper objectMapper = new ObjectMapper();
        oaDeptList.forEach(e ->{
            Map<String, String> requestBodyMap = new HashMap<>();
            requestBodyMap.put("name", e.getDeptName());
            String requestBody = null;
            try {
                requestBody = objectMapper.writeValueAsString(requestBodyMap);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
            callPostApi(apiUrl, requestBody);//调用接口
        });


    }




    public void callPostApi(String apiUrl, String requestBody) {
        log.info("调用资产系统URL: {}", apiUrl);

        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
        RequestBody body = RequestBody.create(requestBody, mediaType);
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", BEARER_TOKEN)
                .addHeader("content-type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                log.info("资产系统成功，返回response: {}", responseBody);
            } else {
                log.error("调用资产系统失败，返回code: {}", response.code());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String,Object>> callGetApi(String apiUrl) {
        log.info("调用资产系统URL: {}", apiUrl);
        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", BEARER_TOKEN)
                .addHeader("content-type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                log.info("资产系统成功，返回response: {}", responseBody);
                ObjectMapper objectMapper = new ObjectMapper();
                Map map = objectMapper.readValue(responseBody, Map.class);
                List<Map<String,Object>> rows = (List<Map<String,Object>>)map.get("rows");
                return rows;
            } else {
                log.error("调用资产系统失败，返回code: {}", response.code());
                throw new RuntimeException();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void callPutApi(String apiUrl, String requestBody) {
        log.info("调用资产系统URL: {}", apiUrl);

        okhttp3.MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(requestBody, mediaType);
        Request request = new Request.Builder()
                .url(apiUrl)
                .put(body)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", BEARER_TOKEN)
                .addHeader("content-type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                log.info("资产系统成功，返回response: {}", responseBody);
            } else {
                log.error("调用资产系统失败，返回code: {}", response.code());
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
