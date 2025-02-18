package com.formssi.generator.model.request;

import lombok.Data;

import java.util.List;

@Data
public class DmTableIndexRequest {

    /**
     * 索引名称
     */
    private String indexName;

    /**
     * 索引类型
     */
    private String indexType;

    /**
     * 是否唯一
     */
    private Boolean isUnique;

    private List<String> columnNameList;
}
