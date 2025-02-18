package com.formssi.chorflow.config;

import cn.hutool.core.util.StrUtil;
import com.formssi.chorflow.domain.exception.FlowException;
import com.formssi.common.json.utils.JsonUtils;
import jakarta.annotation.PostConstruct;
import java.io.Closeable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleRequestBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * 用户 异步 流程实现类
 *
 * @author joey
 * @date 2024.11.19
 */
@Slf4j
@Component
public class ApacheHttpClient5Helper {

  private final FlowResponseHandler flowResponseHandler;

  private CloseableHttpClient closeableHttpClient;

  private CloseableHttpAsyncClient closeableHttpAsyncClient;

  public ApacheHttpClient5Helper(FlowResponseHandler flowResponseHandler) {
    this.flowResponseHandler = flowResponseHandler;
  }

  /**
   * 初始化连接池
   */
  @PostConstruct
  public void init() {
    // init http client
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(1000);
    connectionManager.setDefaultMaxPerRoute(500);
    closeableHttpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

    // init async http client
    PoolingAsyncClientConnectionManager asyncConnectionManager = new PoolingAsyncClientConnectionManager();
    asyncConnectionManager.setMaxTotal(1000);
    asyncConnectionManager.setDefaultMaxPerRoute(500);
    IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setSoTimeout(Timeout.ofSeconds(10))
        .setIoThreadCount(10).build();
    closeableHttpAsyncClient = HttpAsyncClients.custom().setIOReactorConfig(ioReactorConfig)
        .setConnectionManager(asyncConnectionManager).build();
    closeableHttpAsyncClient.start();

  }

  /**
   * 关闭连接
   */
  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (Exception ex) {
        log.error("Resources encounter an exception when closing，ex：{}", ex.getMessage());
      }
    }
  }

  /**
   * 同步请求
   */
  public String httpRequest(String url, String method, String body, Map<String, Object> urlParams,
      Map<String, String> headers) {
    validateParam(url);
    String response = "";
    try {
      URIBuilder uriBuilder = new URIBuilder(url);

      if (null != urlParams) {
        urlParams.forEach((s, o) -> uriBuilder.addParameter(s, o.toString()));
      }
      BasicClassicHttpRequest basicClassicHttpRequest = new BasicClassicHttpRequest(method,
          uriBuilder.build());

      if (null != body) {
        basicClassicHttpRequest.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
      }

      if (MapUtils.isNotEmpty(headers)) {
        headers.forEach(basicClassicHttpRequest::setHeader);
      }

      response = closeableHttpClient.execute(basicClassicHttpRequest, flowResponseHandler);
      log.info(
          "ApacheHttpClient5Util execute success, \n url: {}, \n urlParams: {}, \n headers: {}, body: {}, response: {} ",
          url, urlParams, headers, body, response);
    } catch (Exception e) {
      log.error(
          "ApacheHttpClient5Util execute error \n url: {}, \n urlParams: {}, \n headers: {}, body: {}, response: {} ",
          url, urlParams, headers, body, response, e);
      throw new FlowException(e.getMessage(), e);
    }
    return response;
  }


  /**
   * 异步请求
   */
  public Mono<String> asyncHttpRequest(String url, String method, String body,
      Map<String, String> urlParams, Map<String, String> headers) {
    validateParam(url);

    try {
      URIBuilder uriBuilder = new URIBuilder(url);

      if (null != urlParams) {
        headers.forEach(uriBuilder::addParameter);
      }

      SimpleRequestBuilder simpleRequestBuilder = SimpleRequestBuilder.create(method);
      simpleRequestBuilder.setUri(uriBuilder.build());

      if (null != body) {
        simpleRequestBuilder.setBody(JsonUtils.toJsonString(body), ContentType.APPLICATION_JSON);
      }

      if (MapUtils.isNotEmpty(headers)) {
        headers.forEach(simpleRequestBuilder::setHeader);
      }

      SimpleHttpRequest request = simpleRequestBuilder.build();

      Pair<CompletableFuture<SimpleHttpResponse>, FutureCallback<SimpleHttpResponse>> futureCallbackPair = getFutureCallbackPair();
      closeableHttpAsyncClient.execute(request, futureCallbackPair.getValue());
      return Mono.fromFuture(futureCallbackPair.getKey()).mapNotNull(response -> {
        String r = null;
        try {
          r = response.getBodyText();
          log.info(
              "ApacheHttpClient5Util async execute success, \n url: {}, \n urlParams: {}, \n headers: {}, body: {}, response: {} ",
              url, urlParams, headers, body, response);
          if (StringUtils.isBlank(r)) {
            return null;
          }
          return null;
        } catch (Exception e) {
          log.error(
              "ApacheHttpClient5Util async execute success, \n url: {}, \n urlParams: {}, \n headers: {}, body: {}, response: {} ",
              url, urlParams, headers, body, response);
          throw new RuntimeException(e);
        }
      });
    } catch (Exception e) {
      log.error(
          "ApacheHttpClient5Util async execute success, \n url: {}, \n urlParams: {}, \n headers: {}, body: {}, response: {} ",
          url, urlParams, headers, body, null);
      throw new FlowException(e.getMessage(), e);
    }
  }


  private Pair<CompletableFuture<SimpleHttpResponse>, FutureCallback<SimpleHttpResponse>> getFutureCallbackPair() {
    CompletableFuture<SimpleHttpResponse> completableFuture = new CompletableFuture<>();
    return ImmutablePair.of(completableFuture, new FutureCallback<SimpleHttpResponse>() {

      @Override
      public void completed(SimpleHttpResponse response) {
        completableFuture.complete(response);
      }

      @Override
      public void failed(Exception ex) {
        completableFuture.completeExceptionally(ex);
      }

      @Override
      public void cancelled() {
        completableFuture.cancel(true);
      }
    });
  }

  /**
   * 验证URL
   */
  public void validateParam(String url) {
    if (StrUtil.isBlankIfStr(url)) {
      log.error("url is blank");
      throw new FlowException("url is blank");
    }
  }

}