package com.formssi.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 表名：enum_item
 * 备注：枚举详情
 *
 * @author tanghc
 */
@Data
@TableName("enum_item")
public class SysEnumItem {

    /**  数据库字段：id */
    private Long id;

    /** enum_info.id, 数据库字段：enum_id */
    private Long enumId;

    /** 名称，字面值, 数据库字段：name */
    private String name;

    /** 类型, 数据库字段：type */
    private String type;

    /** 枚举值, 数据库字段：value */
    private String value;

    /** 枚举描述, 数据库字段：description */
    private String description;

    /** 排序索引, 数据库字段：order_index */
    private Integer orderIndex;

    /**  数据库字段：is_deleted */
    private Byte isDeleted;

    /**  数据库字段：gmt_create */
    private LocalDateTime gmtCreate;

    /**  数据库字段：gmt_modified */
    private LocalDateTime gmtModified;

}