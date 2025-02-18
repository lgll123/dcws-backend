package com.formssi.generator.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 索引表
 *
 * @author Shen Tao
 */
@Data
@TableName("dm_table_index")
public class DmTableIndex {

    /**
     * 索引ID
     */
    @TableId(value = "index_id")
    private Long indexId;

    /**
     * 表编号
     */
    private Long tableId;

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

}
