package com.formssi.system.domain.vo;


import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 部门视图对象 sys_dept
 *
 * @author Michelle.Chung
 */
@Data
public class HrDeptVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 部门id
     */
    private String deptId;

    /**
     * 父部门id
     */
    private String parentId;

    /**
     * 父部门名称
     */
    private String parentName;

    /**
     * 祖级列表
     */
    private String ancestors;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门类别编码
     */
//    private String deptCategory;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 负责人ID
     */
    private String leader;

    /**
     * 负责人
     */
//    private String leaderName;

    /**
     * 联系电话
     */
//    private String phone;

    /**
     * 邮箱
     */
//    private String email;

    /**
     * 部门状态（0正常 1停用）
     */

    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */

    private String delFlag;

    /**
     * 部门层级
     */
    private String level;
}
