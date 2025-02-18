package com.formssi.system.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/27 14:53
 */
@Data
public class ModuleDocInfoVo {

    /**
     * 模块id
     */
    private Long id;

    /** 模块名称, 数据库字段：name */
    private String name;

    /** project.id, 数据库字段：project_id */
    private Long projectId;

    /** 模块类型，0：自定义添加，1：swagger导入，2：postman导入, 数据库字段：type */
    private Byte type;

    /**
     * 导入路径
     */
    private String importUrl;

    /**
     * 文档列表
     */
    private List<DocInfoVo> docInfoVoList;
}
