package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.DcwsProjectTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsProjectTask.class)
public class DcwsProjectTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目任务ID
     */
    @ExcelProperty(value = "项目任务ID")
    private Long projectTaskId;

    /**
     * 项目任务ID
     */
    @ExcelProperty(value = "项目任务ID")
    private Long taskId;

    /**
     * 任务类型
     */
    @ExcelProperty(value = "任务类型")
    private String taskType;

    /**
     * 项目ID
     */
    @ExcelProperty(value = "项目ID")
    private Long projectId;

    /**
     * 任务处理状态
     */
    @ExcelProperty(value = "任务处理状态")
    private String taskStatus;

    /**
     * 任务名称
     */
    @ExcelProperty(value = "任务名称")
    private String taskName;

    /**
     * 流程提交路径
     */
    @ExcelProperty(value = "流程提交路径")
    private String taskRouteUrl;

    /**
     * 创建人工号
     */
    @ExcelProperty(value = "创建人工号")
    private String createEmpNo;

    /**
     * 创建人姓名
     */
    @ExcelProperty(value = "创建人姓名")
    private String createEmpName;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 更新时间
     */
    @ExcelProperty(value = "更新时间")
    private Date updateTime;

    /**
     * 业务id
     */
    @ExcelProperty(value = "业务id")
    private String businessKey;

}
