package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.formssi.workflow.domain.DcwsBaseEntity;
import com.formssi.workflow.domain.TaskNodeDataHis;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = TaskNodeDataHis.class)
public class TaskNodeDataHisVo extends DcwsBaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;
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
     * 申请类型--申请类型--19:物料申请
     */
    private String applyType;

    /**
     * 表单内容
     */
    private String applyDetail;
    /**
     * 申请页面输入的物料信息Json(自定义输入)
     */
    private String customApplyDetail;

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
     * 需求日期类型 1:在某月某日下班前须到位 2: 其他（请描述） 3:尽快
     */
    private String requiredDateType;
    /**
     * 需求日期 1:在某月某日下班前须到位
     * 完成日期
     */
    private Date completeDate;
    /**
     * 需求日期 2: 其他（请描述）
     * 需求描述
     */
    private String requiredDesc;
}
