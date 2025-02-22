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
@TableName("dcws_project_task")
public class DcwsProjectTask extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目任务ID
     */
    @TableId(value = "project_task_id")
    private Long projectTaskId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 任务处理状态
     */
    private String taskStatus;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 流程提交路径
     */
    private String taskRouteUrl;

    /**
     * 创建人工号
     */
    private String createEmpNo;

    /**
     * 创建人姓名
     */
    private String createEmpName;

    /**
     * 开始时间
     */
    private Date createTime;

    /**
     * 结束时间
     */
    private Date updateTime;

    /**
     * 业务id
     */
    private String businessKey;


}
