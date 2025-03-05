package com.formssi.web.domain.vo;

import lombok.Data;

/**
 * 获取token
 *
 * @author Michelle.Chung
 */
@Data
public class LoginUserVo {

    /**
     * 令牌权限
     */
    private String token;

    /**
     * 员工号
     */
    private Long empId;



}
