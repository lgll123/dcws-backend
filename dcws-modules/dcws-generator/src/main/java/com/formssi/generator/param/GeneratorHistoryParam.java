package com.formssi.generator.param;

import lombok.Data;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/15 14:08
 */
@Data
public class GeneratorHistoryParam {

    /** datasource_config主键 */
    private List<Integer> datasourceConfigIds;

    /**
     * 表id列表
     */
    private List<Long> tableIdList;

    /** 表名 */
    private List<String> tableNames;

    /** template_config主键 */
    private List<Integer> templateConfigIdList;

    private String charset = "UTF-8";

}
