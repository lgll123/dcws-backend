package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.DcwsTaskSerialNumber;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsTaskSerialNumber.class)
public class DcwsTaskSerialNumberVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 系统名称
     */
    @ExcelProperty(value = "系统名称")
    private String systemName;

    /**
     * '流程类型'
     */
    @ExcelProperty(value = "流程类型")
    private String taskType;

    /**
     * '流程日期'
     */
    @ExcelProperty(value = "流程日期")
    private String taskDate;

    /**
     * 流程序号
     */
    @ExcelProperty(value = "流程序号")
    private Long taskNum;

}
