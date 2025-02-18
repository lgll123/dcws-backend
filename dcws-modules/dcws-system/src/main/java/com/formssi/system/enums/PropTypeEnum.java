package com.formssi.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lizhangyu
 */
@AllArgsConstructor
@Getter
public enum PropTypeEnum {

    DOC_INFO_PROP((byte) 0),
    /**
     * 调试页面属性
     */
    DEBUG_PROPS((byte) 10);

    private final byte type;
}
