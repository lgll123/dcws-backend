package com.formssi.system.domain.dto;

import lombok.Data;

/**
 * @author tanghc
 */
@Data
public class EnumItemDTO {
    private Long id;

    /** enum_info.id, 数据库字段：enum_id */
    private Long enumId;

    /** 名称，字面值, 数据库字段：name */
    private String name;

    /** 类型, 数据库字段：type */
    private String type;

    /** 枚举值, 数据库字段：value */
    private String value;

    /** 枚举描述, 数据库字段：description */
    private String description;
}
