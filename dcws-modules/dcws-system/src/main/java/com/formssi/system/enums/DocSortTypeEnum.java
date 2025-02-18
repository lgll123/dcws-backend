package com.formssi.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author thc
 */
@AllArgsConstructor
@Getter
public enum DocSortTypeEnum {
    BY_ORDER("by_order"),
    BY_NAME("by_name"),
    BY_URL("by_url");

    private final String type;

    public static DocSortTypeEnum of(String type) {
        if (type == null) {
            return BY_ORDER;
        }
        for (DocSortTypeEnum value : DocSortTypeEnum.values()) {
            if (Objects.equals(type, value.getType())) {
                return value;
            }
        }
        return BY_ORDER;
    }
}
