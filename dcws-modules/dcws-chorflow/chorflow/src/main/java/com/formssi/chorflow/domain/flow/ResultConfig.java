package com.formssi.chorflow.domain.flow;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResultConfig {

  @NotBlank
  // 任务节点的id
  private String id;

  // 是否处理分布式事务
  private Boolean transactional = false;

  // 是否红线
  @JsonProperty("transactional-red")
  private Boolean transactionalRed = false;

  private String target;

  private String converter;

  private String type;
}
