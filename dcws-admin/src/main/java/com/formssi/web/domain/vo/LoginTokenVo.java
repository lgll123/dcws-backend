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
     * 通过RSA加密后的字符串
     */
    private String data;

    /**
     * 签名
     */
    private String sign;

}
