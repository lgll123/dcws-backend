package com.formssi.generator.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 布局表
 *
 * @author Shen Tao
 */
@Data
@TableName("dm_table_layout")
public class DmTableLayout {

    /**
     * 表编号
     */
    @TableId(value = "table_id")
    private Long tableId;

    /**
     * 配置数据
     */
    private String layoutData;
}
