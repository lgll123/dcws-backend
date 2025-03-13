package com.formssi.system.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.system.domain.AjaxResult;
import com.formssi.system.domain.vo.HrDeptVo;
import com.formssi.system.domain.vo.HrUserVo;
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
    @Value("${assets.api.url}")
    private String url;
    @Value("${assets.api.token}")
    private String token;
    private final OkHttpClient client = new OkHttpClient();


    /**
     * 查询部门列表
     * @return
     */
    public List<Map<String, Object>> selectDeptList() {
        String apiUrl = url + "departments?order=asc" ;
        List<Map<String, Object>> deptList = callGetApi(apiUrl);//调用接口
        return deptList;
    }


    /**
     * 新增部门信息
     * @return
     */
    public void insertDeptFromOa(List<HrDeptVo> oaDeptList ) {
        String apiUrl = url + "departments";
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

    /**
     * 查询用户列表
     * @return
     */
    public List<Map<String, Object>> selectUserList() {
        String apiUrl = url + "users?order=asc&deleted=false" ;
        List<Map<String, Object>> userList = callGetApi(apiUrl);//调用接口
        return userList;
    }

    /**
     * 新增用户信息
     * @return
     */
    public void insertUserFromOa(List<HrUserVo> oaUserList ) {
        String apiUrl = url + "users";
        ObjectMapper objectMapper = new ObjectMapper();
        oaUserList.forEach(e ->{
            Map<String, String> requestBodyMap = new HashMap<>();
            requestBodyMap.put("first_name", e.getUserName());
            requestBodyMap.put("username", e.getEmpNo());
            requestBodyMap.put("password", "0123456789");
            requestBodyMap.put("password_confirmation", "0123456789");
            requestBodyMap.put("department_id", e.getDeptId());
            requestBodyMap.put("email", e.getEmail());
//            requestBodyMap.put("phone", e.getPhonenumber());
            requestBodyMap.put("activated", "1");
            requestBodyMap.put("locale", "zh-CN");

            String requestBody = null;
            try {
                requestBody = objectMapper.writeValueAsString(requestBodyMap);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
            }
            callPostApi(apiUrl, requestBody);//调用接口
        });
    }

    /**
     * 更新用户信息
     * @return
     */
    public void updateUserFromOa(List<HrUserVo> oaUserList ) {
        oaUserList.forEach(i ->{
            String apiUrl = url + "users/" + i.getAssetUserId();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> requestBodyMap = new HashMap<>();
            requestBodyMap.put("first_name", i.getUserName());
            requestBodyMap.put("department_id", i.getDeptId());
            requestBodyMap.put("email", i.getEmail());
//            requestBodyMap.put("phone", i.getPhonenumber());
            String requestBody = null;
            try {
                requestBody = objectMapper.writeValueAsString(requestBodyMap);
            } catch (JsonProcessingException ex) {
                throw new RuntimeException(ex);
                }
            callPatchApi(apiUrl, requestBody);//调用接口

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
                .addHeader("Authorization", token)
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

    public void callPatchApi(String apiUrl, String requestBody) {
        log.info("调用资产系统URL: {}", apiUrl);

        okhttp3.MediaType mediaType = okhttp3.MediaType.parse("application/json");
        RequestBody body = RequestBody.create(requestBody, mediaType);
        Request request = new Request.Builder()
                .url(apiUrl)
                .patch(body)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", token)
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
                .addHeader("Authorization", token)
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
                .addHeader("Authorization", token)
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
