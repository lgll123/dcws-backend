package com.formssi.generator.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 索引字段关系表
 *
 * @author Shen Tao
 */
@Data
@TableName("dm_table_index_column")
public class DmTableIndexColumn {

    /**
     * ID
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 表编号
     */
    private Long tableId;

    /**
     * 索引ID
     */
    private Long indexId;

    /**
     * 字段编号
     */
    private Long columnId;
}
