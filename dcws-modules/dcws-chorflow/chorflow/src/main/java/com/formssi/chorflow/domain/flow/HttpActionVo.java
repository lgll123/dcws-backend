package com.formssi.chorflow.domain.flow;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public class HttpActionVo {

  private HttpRequestVo request;

  private ResultConfig config;

  // 事务处理 todo
  private Object transactional;
}
