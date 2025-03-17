package com.formssi.workflow.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.workflow.domain.DcwsBaseEntity;
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
public class TaskNodeDataBo extends DcwsBaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private String id;

    /**
     * 任务节点数据表Id
     */
    private String taskNodeDataId;

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
     * 申请类型--申请类型--19:物料申请
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
     * 申请页面输入的物料信息Json(自定义输入)
     */
    private String customApplyDetail;
    /**
     * 需求日期类型 1:在某月某日下班前须到位 2: 其他（请描述） 3:尽快
     */
    private String requiredDateType;
    /**
     * 需求日期/完成日期 1:在某月某日下班前须到位
     * 完成日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date completeDate;
    /**
     * 需求日期/完成日期 2: 其他（请描述）
     * 需求描述
     */
    private String requiredDesc;
    /**
     * 状态
     */
    private String status;
    /**
     * 生成申请单PDF到minio/档案系统服务器状态 1-成功 0-失败 2-minio成功 3-档案系统成功 4-待处理
     *
     */
    private Integer storageFileStatus;
    /**
     * 申请内容类型
     * 服务申请：1-需求(非物料类需求，如开通网络、申请VPN等) 2-事件(故障排查或其它需IT支持事宜) 3-设备维修
     */
    private String applyContentType;
}
