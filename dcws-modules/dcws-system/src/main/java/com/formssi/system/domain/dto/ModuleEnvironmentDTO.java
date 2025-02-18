package com.formssi.system.domain.dto;

import lombok.Data;

/**
 * @author tanghc
 */
@Data
public class ModuleEnvironmentDTO {

    private Long id;

    /**
     * module.id
     */
    private Long moduleId;

    /** 
     * 环境名称
     */
    private String name;

    /** 
     * 调试路径
     */
    private String url;

    /** 
     * 是否公开
     */
    private Byte isPublic;

}