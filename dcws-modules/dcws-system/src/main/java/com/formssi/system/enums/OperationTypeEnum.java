package com.formssi.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/22 14:55
 */
@AllArgsConstructor
@Getter
public enum OperationTypeEnum {

    MANUAL_OPERATION((byte) 0, "人工操作"),

    PLATFORM_OPERATION((byte) 1, "开放平台推送");

    private final byte type;

    private final String desc;

}
