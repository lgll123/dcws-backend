package com.formssi.system.domain.vo;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户对象 sys_user
 *
 * @author Lion Li
 */

@Data
public class HrUserVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 人事系统用户工号 FHxxx
     */
    private String empNo;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 部门ID
     */
    private String deptId;

    /**
     * 用户账号
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;


    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phonenumber;

    /**
     * 用户性别
     */
    private String sex;

    /**
     * 部门名称
     */
    private String orgName;

    /**
     * 帐号状态（0正常 1停用）
     */
    private String status;

    /**
     * 删除标志（0代表存在 1代表删除）
     */
    private String delFlag;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    public HrUserVo() {
    }

}
