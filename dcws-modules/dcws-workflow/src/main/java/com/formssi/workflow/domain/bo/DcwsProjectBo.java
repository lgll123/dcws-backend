package com.formssi.workflow.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.formssi.workflow.domain.DcwsBaseEntity;
import com.formssi.workflow.domain.DcwsProject;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsProject.class, reverseConvertGenerate = false)
public class DcwsProjectBo extends DcwsBaseEntity {

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

}
