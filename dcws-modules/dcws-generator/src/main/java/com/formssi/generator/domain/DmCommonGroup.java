package com.formssi.generator.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 常用字段分组表
 *
 * @author Shen Tao
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dm_common_group")
public class DmCommonGroup extends BaseEntity {

    /**
     * 分组编号
     */
    @TableId(value = "common_group_id")
    private Long commonGroupId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 排序
     */
    private Integer sort;

}
