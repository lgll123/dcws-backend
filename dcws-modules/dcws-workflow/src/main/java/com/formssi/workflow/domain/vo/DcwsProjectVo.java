package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.DcwsProject;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsProject.class)
public class DcwsProjectVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目id
     */
    @ExcelProperty(value = "任务ID")
    private Long projectId;

    /**
     * 项目名称
     */
    @ExcelProperty(value = "项目名称")
    private String projectName;

    /**
     * 项目类型
     */
    @ExcelProperty(value = "项目类型")
    private String projectType;

    /**
     * 项目类型版本号
     */
    @ExcelProperty(value = "项目类型版本号")
    private String projectTypeVer;

    /**
     * 项目处理状态
     */
    @ExcelProperty(value = "项目处理状态")
    private String projectStatus;

    /**
     * 创建人工号
     */
    @ExcelProperty(value = "创建人工号")
    private String empNo;

    /**
     * 开始时间
     */
    @ExcelProperty(value = "开始时间")
    private Date beginTime;

    /**
     * 结束时间
     */
    @ExcelProperty(value = "结束时间")
    private Date endTime;

    /**
     * 项目负责人
     */
    @ExcelProperty(value = "项目负责人")
    private String projectLeader;

    /**
     * 重点任务
     */
    @ExcelProperty(value = "重点任务")
    private String keyTasks;

    /**
     * 风险分级
     */
    @ExcelProperty(value = "风险分级")
    private String riskClassification;

    /**
     * 任务总数
     */
    @ExcelProperty(value = "任务总数")
    private Long taskCount;

    /**
     * 待分配数
     */
    @ExcelProperty(value = "待分配总数")
    private Long draftCount;

    /**
     * 进行中数
     */
    @ExcelProperty(value = "进行中总数")
    private Long inprogressCount;

    /**
     * 已完成数
     */
    @ExcelProperty(value = "已完成总数")
    private Long finishCount;


}
