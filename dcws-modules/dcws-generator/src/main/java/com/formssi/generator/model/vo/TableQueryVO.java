package com.formssi.generator.model.vo;

import lombok.Data;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/12 16:46
 */
@Data
public class TableQueryVO {

    /**
     * 数据源id
     */
    private Integer datasourceConfigId;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 表描述
     */
    private String comment;

}
