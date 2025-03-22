package com.formssi.workflow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 申请类型枚举
 *
 * @author yqh
 */
@Getter
@AllArgsConstructor
public enum ApplyTypeEnum {
    /**
     * IT物料申请
     */
    MATERIAL_IT("19","material", "IT物料申请"),
    /**
     * 非IT物料申请
     */
    MATERIAL_NOT_IT("21","material", "非IT物料申请"),
    /**
     * 用印申请
     */
    SEAL("22", "seal","用印申请"),
    /**
     * 服务申请
     */
    SERVICE("23", "service","服务申请"),
    /**
     * 资料申请
     */
    INFO_APPLY("24", "infoApply","资料申请"),

    /**
     * 档案移交申请
     */
    INFO_CHANGE("28", "infoChange","档案移交申请");

    private final String code;
    private final String name;
    private final String desc;
    public static ApplyTypeEnum of(String type) {
        if (type == null) {
            return null;
        }
        for (ApplyTypeEnum value : ApplyTypeEnum.values()) {
            if (Objects.equals(type, value.getCode())) {
                return value;
            }
        }
        return null;
    }
}
