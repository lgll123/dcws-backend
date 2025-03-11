package com.formssi.workflow.domain.bo;

import com.formssi.workflow.domain.DcwsBaseEntity;
import com.formssi.workflow.domain.DcwsProjectTaskRef;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsProjectTaskRef.class, reverseConvertGenerate = false)
public class DcwsProjectTaskRefBo extends DcwsBaseEntity {

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
