package com.formssi.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/12/6 14:29
 */
@AllArgsConstructor
@Getter
public enum FileStatusEnum {

    /**
     * 生效
     */
    EFFECTIVE(0, "生效"),

    /**
     * 失效
     */
    EXPIRE(1, "失效");

    /**
     * 状态
     */
    private final Integer status;

    /**
     * 描述
     */
    private final String desc;
}
