package com.formssi.chorflow.domain.flow;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public class VarSetVo {

  // 请求数据
  private HttpRequestVo request;

  // 响应数据
  private Object response;
}
