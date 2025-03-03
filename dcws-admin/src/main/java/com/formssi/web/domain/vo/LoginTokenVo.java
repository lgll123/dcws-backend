package com.formssi.web.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 获取token
 *
 * @author Michelle.Chung
 */
@Data
public class LoginTokenVo {

    /**
     * 令牌权限
     */
    private String token;

    /**
     * 员工号
     */
    private Long empId;

}
