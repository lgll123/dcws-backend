package com.formssi.workflow.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.formssi.workflow.domain.DcwsBaseEntity;
import com.formssi.workflow.domain.DcwsProjectTask;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsProjectTask.class, reverseConvertGenerate = false)
public class DcwsProjectTaskBo extends DcwsBaseEntity {

    /**
     * 项目任务ID
     */
    @TableId(value = "project_task_id")
    private Long projectTaskId;

    /**
     * 任务ID
     */
    private String taskId;

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
     * 业务id
     */
    private String businessKey;

}
