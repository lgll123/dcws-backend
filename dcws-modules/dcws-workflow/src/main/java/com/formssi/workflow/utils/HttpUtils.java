package com.formssi.workflow.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Map;

import static com.formssi.workflow.externalsystem.assets.constant.AssetsConstant.*;

public class HttpUtils {
    public static String buildUrl(String baseUrl, String path, Map<String, String> params,String requestType) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseUrl + path).newBuilder();
        if(!CollectionUtil.isEmpty(params)&& REQUEST_TYPE_GET.equals(requestType)){
            params.forEach(urlBuilder::addQueryParameter);
        }
        return urlBuilder.build().toString();
    }

    public static Request buildRequest(String url, String token,Map<String,String> requestBodyMap, String requestType) throws Exception {
        return switch (requestType){
            case REQUEST_TYPE_GET-> buildGetRequest(url,token);
            case REQUEST_TYPE_POST-> buildPostRequest(url,requestBodyMap,token);
            case REQUEST_TYPE_PUT-> buildPutRequest(url,requestBodyMap,token);
            default -> null;
        };

    }
    private static Request buildGetRequest(String url, String token) {
        return new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", token)
                .build();
    }

    public static Request buildPostRequest(String url,Map<String,String> requestBodyMap, String token) throws Exception{
        MediaType mediaType = MediaType.parse("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(requestBodyMap);
        RequestBody body = RequestBody.create(requestBody, mediaType);
        return new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", token)
                .addHeader("content-type", "application/json")
                .build();
    }
    public static Request buildPutRequest(String url,Map<String,String> requestBodyMap, String token) throws Exception {
        MediaType mediaType = MediaType.parse("application/json");
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(requestBodyMap);
        RequestBody body = RequestBody.create(requestBody, mediaType);
        return new Request.Builder()
                .url(url)
                .put(body)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", token)
                .addHeader("content-type", "application/json")
                .build();
    }
}
