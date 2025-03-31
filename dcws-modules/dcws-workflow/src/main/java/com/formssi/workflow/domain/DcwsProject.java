package com.formssi.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dcws_project")
public class DcwsProject extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目id
     */
    @TableId(value = "project_id")
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目类型
     */
    private String projectType;

    /**
     * 项目类型版本号
     */
    private String projectTypeVer;

    /**
     * 项目处理状态
     */
    private String projectStatus;

    /**
     * 创建人工号
     */
    private String empNo;

    /**
     * 开始时间
     */
    private Date beginTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 项目负责人
     */
    private String projectLeader;

    /**
     * 重点任务
      */
    private String keyTasks;

    /**
     * 风险分级
     */
    private String riskClassification;

    /**
     * 任务总数
     */
    private Long taskCount;

    /**
     * 待分配数
     */
    private Long draftCount;

    /**
     * 进行中数
     */
    private Long inprogressCount;

    /**
     * 已完成数
     */
    private Long finishCount;



}
