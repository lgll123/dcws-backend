package com.formssi.generator.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典表
 *
 * @author Shen Tao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_dict")
public class DmDict extends BaseEntity {

    /**
     * 字典编号
     */
    @TableId(value = "dict_id")
    private Long dictId;

    /**
     * 列名称
     */
    private String columnName;

    /**
     * 列描述
     */
    private String columnComment;

    /**
     * 列类型
     */
    private String columnType;

    /**
     * 长度
     */
    private Integer length;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 描述
     */
    private String description;

    /**
     * JAVA类型
     */
    private String javaType;

    /**
     * JAVA字段名
     */
    private String javaField;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 版本号
     */
    private String version;

}
