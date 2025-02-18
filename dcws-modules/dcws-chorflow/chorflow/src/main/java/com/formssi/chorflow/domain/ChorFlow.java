package com.formssi.chorflow.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * 流程编排
 *
 * @author lijun
 * @date 2024-12-17
 */
@Data
@TableName("chor_flow")
@FieldNameConstants
public class ChorFlow {

  /**
   * 主键
   */
  private Long id;

  /**
   * 对应的API的id
   */
  @NotBlank(message = "apiId must be not blank")
  private Long apiId;

  /**
   * 流程名称
   */
  @NotBlank(message = "flowName must be not blank")
  private String flowName;

  /**
   * 描述信息
   */
  private String description;

  /**
   * 文件数据，bpmn数据等
   */
  private String fileContent;

  /**
   * 流程缩略图，Base64数据
   */
  private String img;

  /**
   * 系统id，对应sys_dict_type数据
   */
  private String systemId;

  /**
   * 模块id，对应sys_dict_data数据
   */
  private String moduleId;

  /**
   * 创建时间
   */
  private LocalDateTime createTime;

  /**
   * 创建人
   */
  private Long createBy;

  /**
   * 更新时间
   */
  private LocalDateTime updateTime;

  /**
   * 更新人
   */
  private Long updateBy;

  /**
   * 逻辑删除: 1-已删除 0-未删除
   */
  private Integer deleted;


}