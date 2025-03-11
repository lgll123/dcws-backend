package com.formssi.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dcws_project_task_ref")
public class DcwsProjectTaskRef extends DcwsBaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目类型
     */
    private String projectType;

    /**
     * 项目类型名称
     */
    private String projectTypeName;

    /**
     * 项目类型版本号
     */
    private String projectTypeVer;

    /**
     * 流程名称
     */
    private String taskName;

    /**
     * 流程提交路径
     */
    private String taskRouteUrl;

    /**
     * 流程类型
     */
    private String taskType;

    /**
     * 是否启用
     */
    private String isOpen;


}
