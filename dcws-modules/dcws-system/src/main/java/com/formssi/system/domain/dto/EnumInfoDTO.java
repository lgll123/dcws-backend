package com.formssi.system.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * @author tanghc
 */
@Data
public class EnumInfoDTO {

    private Long id;

    /** 枚举名称, 数据库字段：name */
    private String name;

    /** 枚举说明, 数据库字段：description */
    private String description;

    /** module.id, 数据库字段：module_id */
    private Long moduleId;

    private List<EnumItemDTO> items;

}
