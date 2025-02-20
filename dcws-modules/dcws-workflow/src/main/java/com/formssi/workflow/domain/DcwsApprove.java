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
@TableName("dcws_nonstandard_approve")
public class DcwsApprove extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(value = "task_id")
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 办理人id
     */
    private String userId;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 父级任务id
     */
    private String parentTaskId;

    /**
     * 业务id
     */
    private String businessKey;

    /**
     * 处理说明
     */
    private String remark;

    /**
     * 开始时间
     */
    private Date createTime;

    /**
     * 结束时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private Long createBy;



}
