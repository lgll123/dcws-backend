package com.formssi.system.domain.bo;

import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.system.domain.SysModule;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 项目模块业务对象 module
 *
 * @author admin
 * @date 2025-01-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysModule.class, reverseConvertGenerate = false)
public class ModuleBo extends BaseEntity {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 模块名称
     */
    @NotBlank(message = "模块名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * project.id
     */
    private Long projectId;

    /**
     * 模块类型，0：自定义添加，1：swagger导入，2：postman导入
     */
    private Long type;

    /**
     * 导入url
     */
    private String importUrl;

    /**
     * basic认证用户名
     */
    private String basicAuthUsername;

    /**
     * basic认证密码
     */
    private String basicAuthPassword;

    /**
     * 开放接口调用token
     */
    private String token;

    /**
     * 新增操作方式，0：人工操作，1：开放平台推送
     */
    private Long createMode;

    /**
     * 修改操作方式，0：人工操作，1：开放平台推送
     */
    private Long modifyMode;

    /**
     * 
     */
    private Long creatorId;

    /**
     * 
     */
    private Long modifierId;

    /**
     * 排序索引
     */
    @NotNull(message = "排序索引不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long orderIndex;

    /**
     * 
     */
    private Long isDeleted;

    /**
     * 
     */
    private Date gmtCreate;

    /**
     * 
     */
    private Date gmtModified;

}
