package com.formssi.workflow.domain.vo;

import lombok.Data;
import com.formssi.common.translation.annotation.Translation;
import com.formssi.common.translation.constant.TransConstant;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 任务视图
 *
 * @author may
 */
@Data
public class DcwsTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务id
     */
    private String id;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 负责此任务的人员的用户id
     */
    private String owner;

    /**
     * 办理人id
     */
    private Long assignee;

    /**
     * 办理人
     */
    @Translation(type = TransConstant.USER_ID_TO_NICKNAME, mapper = "assignee")
    private String assigneeName;


    /**
     * 流程实例id
     */
    private String processInstanceId;

    /**
     * 执行id
     */
    private String executionId;

    /**
     * 无用
     */
    private String taskDefinitionId;

    /**
     * 流程定义id
     */
    private String processDefinitionId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 已办任务-创建时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 节点id
     */
    private String taskDefinitionKey;

    /**
     * 任务截止日期
     */
    private Date dueDate;

    /**
     * 流程类别
     */
    private String category;

    /**
     * 父级任务id
     */
    private String parentTaskId;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 认领时间
     */
    private Date claimTime;

    /**
     * 流程状态
     */
    private String businessStatus;

    /**
     * 流程状态
     */
    private String businessStatusName;

    /**
     * 流程定义名称
     */
    private String processDefinitionName;

    /**
     * 流程定义key
     */
    private String processDefinitionKey;

    /**
     * 流程定义版本
     */
    private Integer processDefinitionVersion;

    /**
     * 参与者
     */
    private ParticipantVo participantVo;

    /**
     * 是否会签
     */
    private Boolean multiInstance;

    /**
     * 业务id
     */
    private String businessKey;

    /**
     * 流程定义配置
     */
    private WfDefinitionConfigVo wfDefinitionConfigVo;

    /**
     * 节点配置
     */
    private WfNodeConfigVo wfNodeConfigVo;

    /**
     * 用户任务表单设置
     */
    private WfUserTaskButtonVo wfTaskFormVo;
    /**
     * 用户任务按钮设置 add by qyh
     */
    List<WfUserTaskButtonVo> userTaskButtonVos;

    /**
     * 流程分类
     */
    private String wfType;

    /**
     * 任务是否归属本人
     */
    private String taskBelonging;

    /**
     * 申请原因
     */
    private String applyReason;


}
