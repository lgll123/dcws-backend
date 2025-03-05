package com.formssi.web.domain.vo;

import lombok.Data;

/**
 * 获取token
 *
 * @author Michelle.Chung
 */
@Data
public class LoginTokenResVo {

    /**
     * 成功标识
     */
    private String succ;

    /**
     * 通过RSA加密后的字符串
     */
    private LoginTokenVo data;

}
