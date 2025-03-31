package com.formssi.workflow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 资产借入借出类型 1-借出 2-借入 3-查询 4-其他
 *
 * @author yqh
 */
@Getter
@AllArgsConstructor
public enum AssetsCheckTypeEnum {
    /**
     * 借出
     */
    CHECK_TYPE_1("1", "借出"),
    /**
     * 借入
     */
    CHECK_TYPE_2("2", "借入"),
    /**
     * 查询
     */
    CHECK_TYPE_3("3", "查询"),
    /**
     * 其他
     */
    CHECK_TYPE_4("4", "其他");


    private final String code;
    private final String desc;
    public static AssetsCheckTypeEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (AssetsCheckTypeEnum value : AssetsCheckTypeEnum.values()) {
            if (Objects.equals(code, value.getCode())) {
                return value;
            }
        }
        return null;
    }
}
