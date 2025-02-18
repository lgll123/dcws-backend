package com.formssi.generator.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 用户状态
 *
 * @author LionLi
 */
@Getter
@AllArgsConstructor
public enum DictConstants {

    /**
     * 停用
     */
    DISABLED(0, "停用"),

    /**
     * 正常
     */
    OK(1, "正常"),

    /**
     * 删除
     */
    DELETED(4, "删除");

    private final Integer code;
    private final String message;

}
