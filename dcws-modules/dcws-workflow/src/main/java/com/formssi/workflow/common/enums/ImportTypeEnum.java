package com.formssi.workflow.common.enums;

import com.formssi.workflow.domain.vo.AccessoriesInfoImportVo;
import com.formssi.workflow.domain.vo.HardwareInfoImportVo;
import com.formssi.workflow.domain.vo.LicensesInfoImportVo;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资产导入类型枚举
 *
 * @author yqh
 */
@Getter
@AllArgsConstructor
public enum ImportTypeEnum {
    HARDWARE("hardware","资产信息", HardwareInfoImportVo.class),
    LICENSES("licenses","许可证信息", LicensesInfoImportVo.class),
    ACCESSORIES("accessories","配件信息", AccessoriesInfoImportVo.class);

    private final String type;
    private final String name;
    private final Class voClass;

    public static ImportTypeEnum fromType(String type) {
        for (ImportTypeEnum importType : values()) {
            if (importType.type.equalsIgnoreCase(type)) {
                return importType;
            }
        }
        throw new IllegalArgumentException("不支持的类型: " + type);
    }
}
