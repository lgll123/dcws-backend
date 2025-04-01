package com.formssi.system.domain.bo;

import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.system.domain.SysProject;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 项目业务对象 project
 *
 * @author admin
 * @date 2025-01-15
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysProject.class, reverseConvertGenerate = false)
public class ProjectBo extends BaseEntity {

    /**
     *
     */
    private Long id;

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String name;

    /**
     * 项目描述
     */
    @NotBlank(message = "项目描述不能为空", groups = { AddGroup.class, EditGroup.class })
    private String description;

    /**
     * 所属空间，space.id
     */
    private Long spaceId;

    /**
     * 是否私有项目，1：是，0：否
     */
    @NotNull(message = "是否私有项目，1：是，0：否不能为空", groups = { AddGroup.class, EditGroup.class })
    private Long isPrivate;

    /**
     * 创建者userid
     */
    private Long creatorId;

    /**
     *
     */
    private String creatorName;

    /**
     *
     */
    private Long modifierId;

    /**
     *
     */
    private String modifierName;

    /**
     * 排序索引
     */
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
