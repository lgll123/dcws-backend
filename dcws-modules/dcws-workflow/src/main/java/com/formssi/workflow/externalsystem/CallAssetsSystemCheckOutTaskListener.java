package com.formssi.workflow.externalsystem;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.CategoryBo;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CallAssetsSystemCheckOutTaskListener implements TaskListener {
    private static final String API_URL = "http://10.101.68.29:8000/api/v1/";
//    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiMDE1YTgxM2ZhMGFlNzkxMzc3Mzc1MmEzZmMxYjc2MzU1NjAxZDU0NzZmZDk3YjJlNzgwMTA5NjE5ZTA2N2Y3MWUzMTM0YTI3OTU1MmFiYmUiLCJpYXQiOjE3Mzg4OTkzNzAuMjIxMjAxLCJuYmYiOjE3Mzg4OTkzNzAuMjIxMjA0LCJleHAiOjIyMTIxOTg1NzAuMjEwNzU5LCJzdWIiOiI3NDEiLCJzY29wZXMiOltdfQ.0hcozE2jwP7nkrt3oKLrF4sG8R2NSva3cVJHRzdM13cyLscGs4-J6IXoZnZc29CSWJJ3uedINH8bfG-vixFDCjZop3C800LnjSz4y4zwvP3-tqAy9PbufEvJYJW4X-84jRHQY14XWcRjM2LE1gleW6yiJfAZV4X3BqXoH3p6jjMlyP9AQAvRjqWMfnEv5gquj2_CJCXjHr-oaPvJDDQccFUiRLWS3vFq7r2ePqj4KhpEmheCwup5lcxJfZnMxIC6eIQmFqOQMqyFON2ukSOeqZsEdFVD__2TGlbMHsc2uGZDAHI3zRY8vZnYQvq4elNXKTkCNapmzARdKOXBMh_i0T3Qu9RQ7sdqdIGDPksp78SaXVsD-JtAn4m6RyEeZEwYV_UWXZDgcj2AF_PmmXgLPQjo-SMl6V0mSNDIVn8LYen_oLvU5Z8MzzbrgQO9nww3XO7Mp0CTWz0y643mFBdkFdYrPeVstoO38Y5Mn7fvwo07MOGuKYtPfP5vbGq8qT0QqJjw3J7swCIZQCAjsp1Mu8yMnTdcV37Qw_e4iqIsOKOjUciJ-H5EfLjU3b13l5mcvGZImEW3mkhC32lqO4Gmw1egM2rkbfW57ZWY6CFX_8ehuOD0vi3uFKnC0mzlfYFcaMrlbwOO5SBgBzp0jSuB9zudn1wP1iD0-8MHKAfhilY";
    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiZjRhNDM1MDFmMmFiMWQ1OWRjNzE3NjY5ZWU1MWVkMGE0ZTY2NDA4Mzc2MmY4NmVjNGUxNzU5NTZiN2UyZThlOTM2ZmFjNTNkMWVmODViODUiLCJpYXQiOjE3NDAxMTg2MjcuNDI4OTM2LCJuYmYiOjE3NDAxMTg2MjcuNDI4OTQ2LCJleHAiOjMwMDI0MjI2MjcuNDI2Mjk5LCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.VT4tifmGbr9CblfzRiX8xdatgK_VkiD7nWeY7NUZ3n3ww2Gkks1S8L4q7E7a114EgUCEFRyNrNYRrQdFCkV5mh-JqvMMog-89FtWMEdPOCIShtgv-wzgUaUjborBiCM7Ho0nTP3d0ih8-HPL5m8anJfbxjs4pMNo2CZuX82qx7lldzc1i_zc3Hl4W1F89dmM32gQc-CcY9mvWZLT-mmAsrmsNq5HvrHBupscIZwF7XWRWbVwiG5OV4LoniuD-S7r0fU6fr5uNnkJnP5x_UXVUmGOgqwj-SVp37oiGtEYbP5WwHqTGEF8w5GAWdA9qD8FvYP08GtYo53acYAIEMA67t14z-Ey6vHlxiyAtuYra0QFjsmKwHGOZf3DtoLmpFQIGbxdBj-6esWSESARxojkLcKKowGXEzPGVhRj3U9CY-ecRcH1ABGTDGovSArQQT3AiOJJnd6e4Vw-Il-1E257H9bDCA6nCXkcYTEFwgerp5D_d-RVVdBLfp2td-6cVfLv6YfdE8IJ3Nn3i0uhYv9g_-4ULJYHPfGxNGafEUeqdUsPXpMAP42LNCEPJeAmLMtvzheZvOayAsWaepGpLF22TFdPzc0fNEYSss-hHWIcglwPt84agGdevBCvpVVApY5R1UoHS0Uf4IManmCdaqJo_Lz1_3cQdToEEPBTglD8gpQ";
    private final OkHttpClient client = new OkHttpClient();
    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        variables.forEach((k,value)->{
            log.info(k+"---------------"+value.toString());
        });
        TaskNodeDataBo taskNodeDataBo = null;
        HashMap<String,Object> hashMap =null;
        try {
            log.info("Calling the external system for Assets with URL: {}", API_URL);
            Object entity = variables.get("entity");

            if(variables.get("entity")!=null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    taskNodeDataBo = objectMapper.readValue(JSON.toJSONString(entity), TaskNodeDataBo.class);
                    hashMap = objectMapper.readValue(taskNodeDataBo.getApplyDetail(), HashMap.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                ArrayList<Map<String, Object>> assets = (ArrayList<Map<String, Object>>) hashMap.get("asset");
                ArrayList<Map<String, Object>> licenses = (ArrayList<Map<String, Object>>) hashMap.get("license");
                ArrayList<Map<String, Object>> hards = (ArrayList<Map<String, Object>>) hashMap.get("hard");
                Long finalApplicantId = taskNodeDataBo.getApplicantId();
                if (!CollectionUtil.isEmpty(assets)) {
                    assets.forEach(m -> {
                        Integer id = (Integer) m.get("id");
                        Map<String, Object> requestBodyMap = new HashMap<>();
                        requestBodyMap.put("status_id",  m.get("assetStatusId"));
                        requestBodyMap.put("checkout_to_type",  "user");
                        requestBodyMap.put("assigned_user",  finalApplicantId);
                        try {
                            this.callSystem(requestBodyMap, "hardware/"+id+"/checkout");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                if (!CollectionUtil.isEmpty(licenses)) {
                    for (int i = 0; i < licenses.size(); i++) {
                        Integer id = (Integer) licenses.get(i).get("id");
                        List<Map<String, Object>> maps = callLicensesSysAuery("licenses", String.valueOf(id));
                        List<Integer> seatIds = maps.stream().filter(l -> l.get("assigned_user") == null && l.get("location") == null).map(seat -> {
                            return (Integer) seat.get("id");
                        }).collect(Collectors.toList());
                        Map<String, Object> requestBodyMap = new HashMap<>();
//                        requestBodyMap.put("id", id);
//                        requestBodyMap.put("seat_id", seatIds.get(i));
                        requestBodyMap.put("assigned_to", finalApplicantId);
                        try {
                            this.callLicensesSysUpdate(requestBodyMap, "licenses/"+id+"/seats/"+seatIds.get(i));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                /*if(!CollectionUtil.isEmpty(hards)){
                    hards.forEach(m->{
                        Integer id = (Integer) m.get("id");
                        Map<String, Object> requestBodyMap = new HashMap<>();
                        requestBodyMap.put("assigned_user", String.valueOf(finalApplicantId));
                        requestBodyMap.put("asset_tag", (String)m.get("assetTag"));
                        requestBodyMap.put("status_id", m.get("assetStatusId"));
                        requestBodyMap.put("model_id", m.get("modelId"));
                        try {
                            this.callSystem(requestBodyMap,"hardware");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }*/


            }


        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
            // 抛出BPMN错误，触发错误边界事件
            throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: " + e.getMessage());
        }
    }

    private void callSystem(Map<String, Object> requestBodyMap,String urlPath) throws Exception{
        MediaType mediaType = MediaType.parse("application/json");
        String requestBody = null;
        ObjectMapper objectMapper = new ObjectMapper();
        requestBody = objectMapper.writeValueAsString(requestBodyMap);
        RequestBody body = RequestBody.create(requestBody, mediaType);
        Request request = new Request.Builder()
                .url(API_URL+urlPath)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", BEARER_TOKEN)
                .addHeader("content-type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                // TODO 待优化
                //{"status":"error","messages":{"asset_tag":["asset tag \u5c5e\u6027\u5fc5\u987b\u552f\u4e00\u3002"]},"payload":null}
                log.info("External system response: {}", responseBody);
            } else {
                log.error("External system call failed with status code: {}", response.code());
                // 抛出BPMN错误，触发错误边界事件
                throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: "+response.toString());

            }
        }
    }

    private List<Map<String,Object>> callLicensesSysAuery(String urlPath,String id){
        try {
            log.info("Calling the external system for Assets with URL: {}", API_URL);
            //sort=name&order=asc&offset=0&limit=20
            String requestData = urlPath+"/"+id+"/seats"+"?sort=name"+"&order=asc"+"&limit=9999"+"&offset=0";
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


    private void callLicensesSysUpdate(Map<String, Object> requestBodyMap,String urlPath) throws Exception{
        MediaType mediaType = MediaType.parse("application/json");
        String requestBody = null;
        ObjectMapper objectMapper = new ObjectMapper();
        requestBody = objectMapper.writeValueAsString(requestBodyMap);
        RequestBody body = RequestBody.create(requestBody, mediaType);
        Request request = new Request.Builder()
                .url(API_URL+urlPath)
                .put(body)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", BEARER_TOKEN)
                .addHeader("content-type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                log.info("External system response: {}", responseBody);
            } else {
                log.error("External system call failed with status code: {}", response.code());
                // 抛出BPMN错误，触发错误边界事件
                throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: "+response.toString());

            }
        }
    }
}
