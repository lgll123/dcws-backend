package com.formssi.system.service;

import com.formssi.system.domain.vo.DcwsFinanceApprovalVo;

public interface IFinanceApprovalService {

    /**
     * 根据部门ID查询信息
     *
     * @param deptId 部门ID
     * @return 部门信息
     */
    DcwsFinanceApprovalVo selectFinanceApprovalByDeptId(Long deptId);

}
