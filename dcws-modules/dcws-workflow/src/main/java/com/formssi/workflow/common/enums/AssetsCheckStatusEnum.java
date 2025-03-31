package com.formssi.workflow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 资产借入借出状态 0-失败 1-成功 2-待处理（网络异常） 3-其他（部分失败或全部失败）
 *
 * @author yqh
 */
@Getter
@AllArgsConstructor
public enum AssetsCheckStatusEnum {
    /**
     * 0-失败
     */
    CHECK_STATUS_0("01", "失败"),
    /**
     *  1-成功
     */
    CHECK_STATUS_1("1", "成功"),
    /**
     * 2-待处理（网络异常）
     */
    CHECK_STATUS_2("2", "待处理（网络异常）"),
    /**
     *  3-其他（部分失败或全部失败）
     */
    CHECK_STATUS_3("3", "其他（部分失败或全部失败）");


    private final String code;
    private final String desc;
    public static AssetsCheckStatusEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (AssetsCheckStatusEnum value : AssetsCheckStatusEnum.values()) {
            if (Objects.equals(code, value.getCode())) {
                return value;
            }
        }
        return null;
    }
}
