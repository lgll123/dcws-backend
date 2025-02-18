package com.formssi.chorflow.domain.flow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
@Schema(description = "流程请求对象")
public class FlowReq {

  @NotBlank
  @Schema(description = "请求流程的开始id")
  private String startId;

  @Schema(description = "中途请求流程的开始id")
  private String midwayStartId;

  @Schema(description = "请求数据")
  private Object data;

  @Schema(description = "响应数据集合")
  private Set<FlowResponse> responses;

  @Schema(description = "任务超时时间，为空时使用全局默认超时时间，单位 ms")
  private Integer timeout;


}
