package com.formssi.system.domain.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author tanghc
 */
@Data
public class DocInfoVo {

    private Long id;

    /** 文档名称, 数据库字段：name */
    private String name;

    /** 文档概述, 数据库字段：description */
    private String description;

    private String author;

    /** 访问URL, 数据库字段：url */
    private String url;

    /**
     * 版本号
     */
    private String version;

    /** http方法, 数据库字段：http_method */
    private String httpMethod;

    /** 0:http,1:dubbo, 数据库字段：type */
    private Byte type;

    /** contentType, 数据库字段：content_type */
    private String contentType;

    private String deprecated;

    /** 是否是分类，0：不是，1：是, 数据库字段：is_folder */
    private Byte isFolder;

    /** 父节点, 数据库字段：parent_id */
    private Long parentId;

    /** 模块id，module.id, 数据库字段：module_id */
    private Long moduleId;

    private String creatorName;

    private String modifierName;

    private Byte isShow;

    private Byte isDeleted;

    private Byte isLocked;

    private Byte status;

    private Integer orderIndex;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    @JSONField(serialize = false)
    private DocInfoVo parent;

    private int apiCount;

    public void addApiCount() {
        this.apiCount++;
        if (parent != null) {
            parent.addApiCount();
        }
    }

}
