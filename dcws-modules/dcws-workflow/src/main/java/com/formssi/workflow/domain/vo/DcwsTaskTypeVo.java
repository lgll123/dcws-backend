package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.DcwsTaskType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsTaskType.class)
public class DcwsTaskTypeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务类型
     */
    @ExcelProperty(value = "任务类型")
    private Long taskType;

    /**
     * 任务名称
     */
    @ExcelProperty(value = "任务名称")
    private String taskName;

}
