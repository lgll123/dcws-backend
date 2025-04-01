package com.formssi.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.system.domain.SysProject;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 项目视图对象 project
 *
 * @author admin
 * @date 2025-01-15
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysProject.class)
public class ProjectVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long id;

    /**
     * 项目名称
     */
    @ExcelProperty(value = "项目名称")
    private String name;

    /**
     * 项目描述
     */
    @ExcelProperty(value = "项目描述")
    private String description;

    /**
     * 所属空间，space.id
     */
    @ExcelProperty(value = "所属空间，space.id")
    private Long spaceId;

    /**
     * 是否私有项目，1：是，0：否
     */
    @ExcelProperty(value = "是否私有项目，1：是，0：否")
    private Long isPrivate;

    /**
     * 创建者userid
     */
    @ExcelProperty(value = "创建者userid")
    private Long creatorId;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String creatorName;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long modifierId;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private String modifierName;

    /**
     * 排序索引
     */
    @ExcelProperty(value = "排序索引")
    private Long orderIndex;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Long isDeleted;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Date gmtCreate;

    /**
     * 
     */
    @ExcelProperty(value = "")
    private Date gmtModified;

}
