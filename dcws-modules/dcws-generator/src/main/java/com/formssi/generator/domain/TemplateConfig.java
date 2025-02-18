package com.formssi.generator.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 模板表
 */
@Data
@TableName("template_config")
public class TemplateConfig {
    private Integer id;
    private Integer groupId;
    private String groupName;
    /** 模板名称 */
    private String name;
    /**
     * 目录
     */
    private String folder;
    /** 文件名称 */
    private String fileName;
    /** 内容 */
    private String content;
    /** 是否删除，1：已删除，0：未删除 */
    private Integer isDeleted;

}
