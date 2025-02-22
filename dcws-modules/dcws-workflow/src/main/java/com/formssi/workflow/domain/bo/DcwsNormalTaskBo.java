package com.formssi.workflow.domain.bo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.DcwsNormalTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsNormalTask.class, reverseConvertGenerate = false)
public class DcwsNormalTaskBo extends BaseEntity {

    /**
     * 任务ID
     */
    @TableId(value = "task_id")
    private Long taskId;

    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 父级任务id
     */
    private Long parentTaskId;

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

}
