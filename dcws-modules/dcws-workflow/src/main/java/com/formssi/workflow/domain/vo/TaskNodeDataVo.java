package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.TaskNodeData;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.util.Date;


@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = TaskNodeData.class)
public class TaskNodeDataVo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

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
     * 申请人对应资产系统用户Id
     */
    private Long assetUserId;
    /**
     * 申请人Id
     */
    private Long applicantId;
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
