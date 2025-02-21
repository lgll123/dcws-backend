package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.DcwsProjectTaskRef;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsProjectTaskRef.class)
public class DcwsProjectTaskRefVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目类型
     */
    @ExcelProperty(value = "项目类型")
    private String projectType;

    /**
     * 项目类型名称
     */
    @ExcelProperty(value = "项目类型名称")
    private String projectTypeName;

    /**
     * 项目类型版本号
     */
    @ExcelProperty(value = "项目类型版本号")
    private String projectTypeVer;

    /**
     * 流程名称
     */
    @ExcelProperty(value = "流程名称")
    private String taskName;

    /**
     * 流程提交路径
     */
    @ExcelProperty(value = "流程提交路径")
    private String taskRouteUrl;

    /**
     * 流程类型
     */
    @ExcelProperty(value = "流程类型")
    private String taskType;

    /**
     * 是否启用
     */
    @ExcelProperty(value = "是否启用")
    private String isOpen;

}
