package com.formssi.generator.model.request;

import lombok.Data;

/**
 * 表 请求参数
 *
 * @author Shen Tao
 */
@Data
public class DmTableColumnRequest {

    private Long DictId;
    private String columnName;
    private String columnComment;
    private String columnType;
    private Boolean isPk;
    private Boolean isRequired;
    private String defaultValue;

}
