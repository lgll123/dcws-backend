package com.formssi.web.domain.bo;

import lombok.Data;

/**
 * 获取token
 *
 * @author Michelle.Chung
 */
@Data
public class LoginTokenBo {

    /**
     * 通过RSA加密后的字符串
     */
    private String data;

    /**
     * 签名
     */
    private String sign;

    /**
     * 员工姓名
     */
    private String empName;

    /**
     * 员工工号
     */
    private String empNo;



}
