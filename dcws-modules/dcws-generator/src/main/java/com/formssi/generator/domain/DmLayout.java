package com.formssi.generator.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * ER图设计表
 *
 * @author Shen Tao
 */
@Data
@TableName("dm_layout")
public class DmLayout {

    /**
     * 设计编号
     */
    @TableId(value = "layout_id")
    private Long layoutId;

    private String systemCode;

    private String moduleCode;

    /**
     * 设计标题
     */
    private String title;

    /**
     * 配置数据
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String layoutData;
}
