package com.formssi.workflow.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.DcwsTaskType;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsTaskType.class, reverseConvertGenerate = false)
public class DcwsTaskTypeBo extends BaseEntity {

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
