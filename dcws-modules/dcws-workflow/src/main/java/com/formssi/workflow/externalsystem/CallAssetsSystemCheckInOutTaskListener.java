package com.formssi.workflow.externalsystem;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 更换领用人，先归还，再借出
 */
@Slf4j
public class CallAssetsSystemCheckInOutTaskListener implements TaskListener {
    private static final String API_URL = "http://10.101.68.29:8000/api/v1/";
    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiNDRlMWQ2OTA0ZjM3NTQyNDJmZmU5ZTMxOTk0MjZiNzI1MDRkYzNhMmM4ODhhNTIxZWFjNDdlNDA4MjliZWUxZjBmNWRhOGFhYTA2MDZmMjIiLCJpYXQiOjE3NDAwNDEwNzUuNzY0Nzg5LCJuYmYiOjE3NDAwNDEwNzUuNzY0NzkxLCJleHAiOjMwMDIzNDUwNzUuNzYyMjEzLCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.TowtjDbbsNkWvdLxz16s69Cj3zA1KF8iioV3jOF26W6SwjP8JWA1r4Fvk71rcOXNTlETeZMS-xmFgjeXyZHYOcqeizIOUXwpdppsBXKuzTH8z2XaZKrpYLNVJrrip1hZEYC_xdln6vapWHRtxwVYLM-L-yzz3Ytc97_afab7f5El6-WxsIvPy7lBIhfexwICweOTU-7EgRdyA5DO21Y5YVC6yBGQBb4U4GRqxhNM_WjsKg4msH_MT19FzEBY0JLgIPzDF1sS66IywsGRJr-yBy6cSdk9h4aOxjprSuWZYaI9Lk_B1jHkcE58EB7Tjvrw_jiOofPoUVSPTxx_wGIhpk8Lz_3Yyb-Jzimp59JxbWaek5TPbxcLEaElrsL2MFY6mgbWpAlzdxQTgYzG5VkY9wjqw08NOklGhMIAtukX4-fwK2dHLu_AiyFNojYKcOinCNKb-ybeSIyG-hE9Tk9Iw4H0h2B5RH3TA64GRCwjl1y1RIgurxUR7peKFABxGpNBJ-63R90byn9Q5GXzndtOp9UZplYLg4XME7jt7yarZJUMjIFnW8ef938UTzg2Gig7HetrKeHEE_-Vl8RDFcpYpRn29QxTOrkriSX6SNu8SdH_zE-sL9NStHz0BfwI6jfb96gFretBzArxs2tVcUHZysxwTn-LkenpMcHX4owrNCU";
    private final OkHttpClient client = new OkHttpClient();
    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        variables.forEach((k,value)->{
            log.info(k+"---------------"+value.toString());
        });
        TaskNodeDataBo taskNodeDataBo = null;
        HashMap<String,Object> hashMap =null;
        ArrayList<Map<String, Object>> assetList = null;
        ArrayList<Map<String, Object>> licenseList = null;
        ArrayList<Map<String, Object>> hardList = null;
        try {
            Object entity = variables.get("entity");
            if(variables.get("entity")!=null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    taskNodeDataBo = objectMapper.readValue(JSON.toJSONString(entity), TaskNodeDataBo.class);
                    hashMap = objectMapper.readValue(taskNodeDataBo.getApplyDetail(), HashMap.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                assetList = (ArrayList<Map<String, Object>>) hashMap.get("asset");
                licenseList = (ArrayList<Map<String, Object>>) hashMap.get("license");
                hardList = (ArrayList<Map<String, Object>>) hashMap.get("hard");
            }

//            String jsonData = "{\"hard\": [], \"asset\": [{\"id\": 5, \"qty\": null, \"name\": \"资产-苹果电脑\", \"serial\": \"2\", \"modelNo\": \"test\", \"assetTag\": \"C02G64PV3D6T\", \"assetType\": null, \"remainQty\": null, \"productKey\": null, \"assetStatus\": \"deployable\", \"licenseName\": null, \"categoryName\": \"笔记本\", \"licenseEmail\": null, \"locationName\": null, \"purchaseCost\": null, \"purchaseDate\": null, \"checkoutsCount\": null, \"expirationDate\": null, \"manufacturerName\": null}], \"license\": []}";
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, List<Map<String, String>>> mapData = mapper.readValue(jsonData, new TypeReference<Map<String, List<Map<String, String>>>>() {});
//            List<Map<String, String>> assetList = mapData.get("asset");//IT资产
//            List<Map<String, String>> licenseList = mapData.get("license");//软件清单
//            List<Map<String, String>> hardList = mapData.get("hard");//硬件清单

            //一、资产
            if(!CollectionUtils.isEmpty(assetList)){
                assetList.forEach(e ->{
                    //1、归还
                    //String assetIdIn = "6"; //资产id 先写死 todo
                    String assetIdIn = (String) e.get("id"); //资产id
                    String apiUrlIn = API_URL + "hardware/" + assetIdIn + "/checkin";
                    ObjectMapper objectMapperIn = new ObjectMapper();
                    Map<String, String> requestBodyMapIn = new HashMap<>();
                    requestBodyMapIn.put("status_id", "2");
                    String requestBodyIn = null;
                    try {
                        requestBodyIn = objectMapperIn.writeValueAsString(requestBodyMapIn);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                    callPostApi(apiUrlIn, requestBodyIn);//调用接口
                    //2、借出
                    String apiUrlOut = API_URL + "hardware/" + assetIdIn + "/checkout";
                    ObjectMapper objectMapperOut = new ObjectMapper();
                    Map<String, String> requestBodyMapOut = new HashMap<>();
                    requestBodyMapOut.put("checkout_to_type", "user");
                    requestBodyMapOut.put("assigned_user", "4"); //先写死用户（协同用户） todo
                    String requestBodyOut = null;
                    try {
                        requestBodyOut = objectMapperOut.writeValueAsString(requestBodyMapOut);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                    callPostApi(apiUrlOut, requestBodyOut);//调用接口
                });
            }
            //二、许可证
            if(!CollectionUtils.isEmpty(licenseList)){
                licenseList.forEach(e ->{
                    //1、根据许可证id查询席位id
                    //String assetId = "1"; //许可证id 先写死 todo
                    String assetId = (String) e.get("id");
                    String userId = "2"; //先写死被领用用户（张三） todo
                    String apiUrl = API_URL + "licenses/" + assetId + "/seats?sort=name&order=asc";
                    List<Map<String, Object>> mapList = callGetApi(apiUrl);//调用接口
                    Map<String, Object> seatMap = mapList.stream().filter(u -> {
                        Map<String,String> o = (HashMap)u.get("assigned_user");
                        if(!ObjectUtils.isEmpty(o)){
                            return userId.equals(String.valueOf(o.get("id")));//筛选出被领用用户的席位
                        }else {
                            return false;
                        }
                    }).findFirst().get();
                    String seatId = String.valueOf(seatMap.get("id"));//席位id
                    //2、根据席位id更换许可证领用人
                    String apiUrl2 = API_URL + "licenses/" + assetId + "/seats/" + seatId;
                    ObjectMapper objectMapperOut = new ObjectMapper();
                    Map<String, String> requestBodyMapOut = new HashMap<>();
                    requestBodyMapOut.put("assigned_to", "4"); //先写死用户（协同用户） todo
                    String requestBodyOut = null;
                    try {
                        requestBodyOut = objectMapperOut.writeValueAsString(requestBodyMapOut);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                    callPutApi(apiUrl2, requestBodyOut);//调用接口
                });
            }
            //三、附属品
            if(false){
//                licenseList.forEach(e ->{
                    //1、根据附属品id 查询 附属品用户关联id
                    String assetId = "6"; //附属品id 先写死 todo
//                    String assetId = e.get("id");
                    String userId = "2"; //先写死被领用用户（张三） todo
                    int num = 1; //借出的附属品数量 先写死 todo
                    String apiUrl = API_URL + "accessories/" + assetId + "/checkedout";
                    List<Map<String, Object>> mapList = callGetApi(apiUrl);//调用接口
                    List<String> idList = mapList.stream().filter(u -> { //附属品用户关联id
                        Map<String, String> o = (HashMap) u.get("assigned_to");
                            if (!ObjectUtils.isEmpty(o)) {
                                return userId.equals(String.valueOf(o.get("id")));//筛选出被领用用户的 附属品用户关联id
                            } else {
                                return false;
                            }
                    }).map(e -> String.valueOf(e.get("id")))
                            .limit(num).collect(Collectors.toList());
                    //2、根据附属品用户关联id 归还附属品
                    idList.forEach(id ->{
                        String apiUrl2 = API_URL + "accessories/" + id + "/checkin";
                        callPostApi(apiUrl2, "");//调用接口
                    });
                    //3、根据附属品id 借出附属品
                    String apiUrlOut = API_URL + "accessories/" + assetId + "/checkout";
                    ObjectMapper objectMapperOut = new ObjectMapper();
                    Map<String, String> requestBodyMapOut = new HashMap<>();
                    requestBodyMapOut.put("checkout_qty", String.valueOf(num));
                    requestBodyMapOut.put("assigned_user", "4"); //借出的用户（协同用户）先写死 todo
                    String requestBodyOut = null;
                    try {
                        requestBodyOut = objectMapperOut.writeValueAsString(requestBodyMapOut);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                    callPostApi(apiUrlOut, requestBodyOut);
//                });
            }
            //四、消耗品---无需更换领用人

            //五、组件---无需更换领用人



        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
            // 抛出BPMN错误，触发错误边界事件
            throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: " + e.getMessage());
        }
    }


    public void callPostApi(String apiUrl, String requestBody) {
        log.info("Calling the external system for Assets with URL: {}", apiUrl);

        MediaType mediaType = MediaType.parse("application/json");
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
                log.info("External system response: {}", responseBody);
            } else {
                log.error("External system call failed with status code: {}", response.code());
                // 抛出BPMN错误，触发错误边界事件
                throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: "+response.toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String,Object>> callGetApi(String apiUrl) {
        log.info("Calling the external system for Assets with URL: {}", apiUrl);
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
                log.info("External system response: {}", responseBody);
                ObjectMapper objectMapper = new ObjectMapper();
                Map map = objectMapper.readValue(responseBody, Map.class);
                List<Map<String,Object>> rows = (List<Map<String,Object>>)map.get("rows");
                if(rows.isEmpty()){
                    throw new ServiceException( "查询失败");
                }
                return rows;
            } else {
                log.error("External system call failed with status code: {}", response.code());
                // 抛出BPMN错误，触发错误边界事件
                throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: "+response.toString());
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void callPutApi(String apiUrl, String requestBody) {
        log.info("Calling the external system for Assets with URL: {}", apiUrl);

        MediaType mediaType = MediaType.parse("application/json");
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
                log.info("External system response: {}", responseBody);
            } else {
                log.error("External system call failed with status code: {}", response.code());
                // 抛出BPMN错误，触发错误边界事件
                throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: "+response.toString());
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
