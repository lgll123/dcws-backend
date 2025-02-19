package com.formssi.system.service.impl;

import com.formssi.system.domain.AjaxResult;
import com.formssi.system.service.ISysHrService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static cn.dev33.satoken.SaManager.log;


@Service
@RequiredArgsConstructor
public class SysHrServiceImpl implements ISysHrService {

    @Value("${hr-system.api-url-user}")
    private String apiUrlUser;

    @Value("${hr-system.api-url-org}")
    private String apiUrlOrg;



    /**
     * 调用人事系统用户列表接口
     * @return AjaxResult
     */
    public AjaxResult selectUserList() {
        try {
            // 1. 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("Authorization", "Bearer " + appSecret);

            // 2. 构造请求体（根据第三方接口文档定义）
//            String requestBody = String.format(
//                "{\"app_key\":\"%s\",\"phone\":\"%s\",\"content\":\"%s\"}",
//                appKey, phone, content
//            );
            String requestBody = "{\"paramsStr\":{}}";

            // 3. 发送POST请求
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrlUser, request, String.class);
            // 3. 发送get请求
//            HttpEntity<String> request = new HttpEntity<>(headers);
//            RestTemplate restTemplate = new RestTemplate();
//            ResponseEntity<String> response = restTemplate.getForEntity(
//                "http://10.101.136.104:8099/thirdPlatformForeign/call/v2/getUserList",
//                 String.class);

            // 4. 处理响应
            if (response.getStatusCode() == HttpStatus.OK) {
                // 根据第三方接口返回的JSON解析结果
                return AjaxResult.success("查询用户信息成功", response.getBody());
            } else {
                return AjaxResult.error("查询用户信息失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // 记录日志
            log.error("查询人事系统用户列表异常 => {}", e.getMessage());
            return AjaxResult.error("查询人事系统用户列表异常: " + e.getMessage());
        }
    }

    /**
     * 调用人事系统组织列表接口
     * @return AjaxResult
     */
    public AjaxResult selectOrgList() {
        try {
            // 1. 构造请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 2. 构造请求体（根据第三方接口文档定义）
            String requestBody = "{\"paramsStr\":{}}";

            // 3. 发送POST请求
            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrlOrg, request, String.class);

            // 4. 处理响应
            if (response.getStatusCode() == HttpStatus.OK) {
                // 根据第三方接口返回的JSON解析结果
                return AjaxResult.success("查询组织信息成功", response.getBody());
            } else {
                return AjaxResult.error("查询组织信息失败: " + response.getStatusCode());
            }
        } catch (Exception e) {
            // 记录日志
            log.error("查询人事系统组织列表异常 => {}", e.getMessage());
            return AjaxResult.error("查询人事系统组织列表异常: " + e.getMessage());
        }
    }
}
