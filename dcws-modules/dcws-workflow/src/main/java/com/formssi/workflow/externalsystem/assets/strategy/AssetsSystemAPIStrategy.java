package com.formssi.workflow.externalsystem.assets.strategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.exception.ApiCallException;
import com.formssi.workflow.utils.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

/**
 * 资产管理API接口策略
 *
 * @author yqh
 */
@Slf4j
@Service("assets" + IExternalSystemAPIStrategy.BASE_NAME)
@RequiredArgsConstructor
public class AssetsSystemAPIStrategy implements IExternalSystemAPIStrategy {
    @Value("${assets.api.url}")
    private String apiUrl;

    @Value("${assets.api.token}")
    private String bearerToken;
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> process(Map<String, String> params, String pathUrl,String requestType) {
        try {
            String url = HttpUtils.buildUrl(apiUrl, pathUrl, params,requestType);
            Request request = HttpUtils.buildRequest(url,bearerToken,params,requestType);
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    //"客户端id: {} 认证类型：{} 异常!."
                    log.info("资产管理API调用失败,url:{} 返回信息：{} " + url, response);
                    throw new ApiCallException(response.body().string(),response.code());
                }
                String responseBody = response.body().string();
                return objectMapper.readValue(responseBody, new TypeReference<>() {});
            }
    } catch (IOException e) {
        log.info("网络请求异常: " +e.getMessage(),e);
//        throw new ApiCallException("网络请求异常: " + e.getMessage());
        throw new ApiCallException(e.getMessage(),300);
    }catch (Exception e) {
            log.info("系统异常: " + e.getMessage(),e);
            throw new ApiCallException("系统异常: " + e.getMessage());
        }
    }
}
