package com.formssi.workflow.domain.bo;

import lombok.Data;

/**
 * 目录类型查询对象
 */
@Data
public class CategoryBo {


    /**
     * 目录Id
     */
    private Integer id;
    /**
     * 目录类型
     */
    private String categoryType;

    /**
     * 目录名称
     */
    private String name;

}
