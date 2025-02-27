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
@TableName("task_node_data_his")
public class TaskNodeDataHis extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
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
    private String applyDept;

    /**
     * 申请人
     */
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
     * 预计使用人
     */
    private String checkTo;
    /**
     * 申请日期
     */
    private Date applyDate;

    /**
     * 申请类型--M:物料申请 R:资产入库
     */
    private String applyType;

    /**
     * 表单内容
     */
    private String applyDetail;

    /**
     * 状态
     */
    private String status;
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
     * 需求日期/完成日期
     */
    private String requiredDate;

}
