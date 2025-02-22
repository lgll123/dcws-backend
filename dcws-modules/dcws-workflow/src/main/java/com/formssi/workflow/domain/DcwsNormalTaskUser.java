package com.formssi.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dcws_normal_task_user")
public class DcwsNormalTaskUser extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 办理人id
     */
    private String userId;

    /**
     * 办理人名称
     */
    private String userName;

}
