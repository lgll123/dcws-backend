package com.formssi.chorflow.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * 应用系统表
 *
 * @author lijun
 */
@Data
@FieldNameConstants
@TableName(SysBussys.TABLE_NAME)
@Schema(name = "SysBussys", description = "应用系统表")
public class SysBussys {

  public static final String TABLE_NAME = "sys_bussys";

  @Schema(description = "系统代码")
  @TableId("system_code")
  private String systemCode;

  @Schema(description = "系统名称")
  @TableField("system_name")
  private String systemName;

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