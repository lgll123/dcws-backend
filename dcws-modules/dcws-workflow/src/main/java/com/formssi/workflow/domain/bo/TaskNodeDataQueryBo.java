package com.formssi.workflow.domain.bo;

import lombok.Data;

/**
 * 申请业务查询条件对象
 */
@Data
public class TaskNodeDataQueryBo{

    /**
     * 申请部门
     */
    private String applyDept;

    /**
     * 申请人
     */
    private String applicant;


    /**
     * 申请类型
     * 19:IT物料申请
     * 21:非IT物料申请
     */
    private String applyType;

    /**
     * 申请日期
     */
    private String applyDate;


}
