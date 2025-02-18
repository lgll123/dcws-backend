package com.formssi.generator.model.request;

import lombok.Data;

@Data
public class DmDictQueryRequest {

    private String keyword;

    /**
     * 列名称
     */
    private String columnName;

    /**
     * 列描述
     */
    private String columnComment;

}


