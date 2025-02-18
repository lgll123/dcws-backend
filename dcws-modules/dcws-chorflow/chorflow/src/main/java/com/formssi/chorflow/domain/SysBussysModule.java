package com.formssi.chorflow.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * 应用系统模块表
 *
 * @author lijun
 */
@Data
@FieldNameConstants
@TableName(SysBussysModule.TABLE_NAME)
@Schema(name = "SysBussysModule", description = "应用系统模块表")
public class SysBussysModule {

  public static final String TABLE_NAME = "sys_bussys_module";

  @Schema(description = "模块代码")
  @TableField("module_code")
  private String moduleCode;

  @Schema(description = "系统代码,关联sys_system")
  @TableField("system_code")
  private String systemCode;

  @Schema(description = "模块名称")
  @TableField("module_name")
  private String moduleName;

  @Schema(description = "描述信息")
  @TableField("description")
  private String description;

  @Schema(description = "创建时间")
  @TableField("create_time")
  private LocalDateTime createTime;

  @Schema(description = "创建人")
  @TableField("create_by")
  private Long createBy;

  @Schema(description = "更新时间")
  @TableField("update_time")
  private LocalDateTime updateTime;

  @Schema(description = "更新人")
  @TableField("update_by")
  private Long updateBy;

  @Schema(description = "逻辑删除: 1-已删除 0-未删除")
  @TableField("deleted")
  private Boolean deleted;

}
