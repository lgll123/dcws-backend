package com.formssi.workflow.domain.bo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.DcwsApprove;
import com.formssi.workflow.domain.TestLeave;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsApprove.class, reverseConvertGenerate = false)
public class DcwsApproveBo extends BaseEntity {

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
