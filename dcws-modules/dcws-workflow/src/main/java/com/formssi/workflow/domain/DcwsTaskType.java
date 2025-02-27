package com.formssi.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dcws_task_type")
public class DcwsTaskType extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务类型
     */
    @TableId(value = "task_type")
    private Long taskType;

    /**
     * 任务名称
     */
    private String taskName;

}
