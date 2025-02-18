package com.formssi.chorflow.domain.flow;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public class FlowResponse {

  // 响应的key
  private String key;

  // 响应数据的路径
  private String path;

}
