package com.formssi.system.enums;

import lombok.Getter;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/22 10:53
 */
@Getter
public enum BooleanEnum {

    TRUE((byte) 1, "true"),

    FALSE((byte) 0, "false");

    private final byte type;

    private final String desc;

    BooleanEnum(byte type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 是否为true，当val为null，返回false
     * @param val 值
     * @return 返回true/false
     */
    public static boolean isTrue(Byte val) {
        return isTrue(val, false);
    }

    /**
     * 是否为true
     * @param val 值
     * @param whenNull 当val为null时，返回whenNull指定的值
     * @return 返回true/false
     */
    public static boolean isTrue(Byte val, boolean whenNull) {
        if (val == null) {
            return whenNull;
        }
        return val == TRUE.getType();
    }

    public static boolean isTrue(Long b) {
        return b != null && b == TRUE.getType();
    }

    public static byte toValue(Boolean b) {
        if (b == null) {
            return FALSE.getType();
        }
        return b ? TRUE.getType() : FALSE.getType();
    }

}
