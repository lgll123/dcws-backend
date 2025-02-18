package com.formssi.chorflow.domain.flow;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
public class HttpRequestVo {

  @NotBlank
  private String url;
  @NotBlank
  private String method;
  private Object body;
  private Map<String, Object> params;
  private Map<String, String> headers;

  // 用于处理分布式事务的回滚接口
  private String undoUrl;

  /* 响应成功码 */
  private String successCode;

  /* 响应成功码路径 */
  private String successJsonPath;

  /* 错误信息路径 */
  private String errorMessageJsonPath;


}
