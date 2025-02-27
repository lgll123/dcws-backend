package com.formssi.workflow.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.TaskNodeData;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 申请业务对象 assets
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = TaskNodeData.class, reverseConvertGenerate = false)
public class TaskNodeDataBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 任务节点数据表Id
     */
    private Long taskNodeDataId;

    /**
     * 工作流标准任务ID
     */
    private String taskId;
    /**
     * 申请部门
     */
    @NotBlank(message = "申请部门不能为空", groups = {AddGroup.class, EditGroup.class})
    private String applyDept;

    /**
     * 申请人
     */
    @NotBlank(message = "申请人不能为空", groups = {AddGroup.class, EditGroup.class})
    private String applicant;
    /**
     * 申请人Id
     */
    private Long applicantId;
    /**
     * 申请人对应资产系统用户Id
     */
    private Long assetUserId;
    /**
     * 申请类型--M:物料申请 R:资产入库
     */
    private String applyType;
    /**
     * 预计使用人
     */
    private String checkTo;


    /**
     * 租户id
     */
    private String  tenantId;
    /**
     * 申请原因
     */
    private String  applyReson;
    /**
     * 备注
     */
    private String  applyRemarks;
    /**
     * 申请日期
     */
    @NotNull(message = "申请日期不能为空", groups = {AddGroup.class, EditGroup.class})
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date applyDate;
    /**
     * 申请详细信息Json
     */
    private String applyDetail;
    /**
     * 需求日期/完成日期
     */
    private String requiredDate;
    /**
     * 状态
     */
    private String status;

}
