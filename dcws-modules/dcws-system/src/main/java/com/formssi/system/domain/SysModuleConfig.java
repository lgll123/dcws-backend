package com.formssi.system.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
/**
 * 表名：module_config
 * 备注：系统配置
 *
 * @author lizhangyu
 */
@Data
@TableName("module_config")
public class SysModuleConfig {

    /**  数据库字段：id */
    private Long id;

    /**  数据库字段：module_id */
    private Long moduleId;

    /** 配置类型，1：全局header, 数据库字段：type */
    private Byte type;

    /** 配置key, 数据库字段：config_key */
    private String configKey;

    /** 配置值, 数据库字段：config_value */
    private String configValue;

    /**  数据库字段：extend_id */
    private Long extendId;

    /** 描述, 数据库字段：description */
    private String description;

    /**  数据库字段：is_deleted */
    private Byte isDeleted;

    /**  数据库字段：gmt_create */
    private LocalDateTime gmtCreate;

    /**  数据库字段：gmt_modified */
    private LocalDateTime gmtModified;

}