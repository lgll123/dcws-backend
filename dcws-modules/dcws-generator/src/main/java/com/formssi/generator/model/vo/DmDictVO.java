package com.formssi.generator.model.vo;

import lombok.Data;

@Data
public class DmDictVO {

    /**
     * 字典编号
     */
    private Long dictId;

    /**
     * 列名称
     */
    private String columnName;

    /**
     * 列描述
     */
    private String columnComment;

    /**
     * 列类型
     */
    private String columnType;

    /**
     * 长度
     */
    private Integer length;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 描述
     */
    private String description;

    /**
     * JAVA类型
     */
    private String javaType;

    /**
     * 版本号
     */
    private String version;

}


