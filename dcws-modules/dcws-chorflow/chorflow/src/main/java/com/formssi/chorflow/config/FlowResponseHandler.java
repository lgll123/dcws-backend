package com.formssi.chorflow.config;

import com.formssi.chorflow.domain.exception.FlowException;
import java.io.IOException;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.stereotype.Component;

/**
 * 结果处理器
 *
 * @author lijun
 * @date 2025-1-7
 */
@Component
public class FlowResponseHandler implements HttpClientResponseHandler<String> {


  @Override
  public String handleResponse(ClassicHttpResponse classicHttpResponse)
      throws HttpException, IOException {
    int statusCode = classicHttpResponse.getCode();
    if (statusCode == HttpStatus.SC_OK) {
      return EntityUtils.toString(classicHttpResponse.getEntity());
    } else {
      throw new FlowException("请求失败，状态码：" + statusCode);
    }
  }
}