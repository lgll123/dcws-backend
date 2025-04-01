package com.formssi.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dcws_normal_task")
public class DcwsNormalTask extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @TableId(value = "task_id")
    private String taskId;

    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 父级任务id
     */
    private String parentTaskId;

    /**
     * 业务id
     */
    private String businessKey;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 处理说明
     */
    private String remark;

    /**
     * 办理人id
     */
    private String userId;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 公司
     */
    private Long companyId;

}
