package com.formssi.common.core.service;

/**
 * 通用 部门服务
 *
 * @author Lion Li
 */
public interface DeptService {

    /**
     * 通过部门ID查询部门名称
     *
     * @param deptIds 部门ID串逗号分隔
     * @return 部门名称串逗号分隔
     */
    String selectDeptNameByIds(String deptIds);
    /**
     * 通过部门ID查询部门Leader Id
     *
     * @param deptId 部门ID
     * @return 部门Leader Id
     */
    Long selectDeptLeaderById(String deptId);
    /**
     * 通过部门ID查询分管部门Leader Id
     *
     * @param deptId 部门ID
     * @return 分管部门Leader Id
     */
    Long selectDeptRespLeaderById(String deptId);
}
