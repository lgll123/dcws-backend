package com.formssi.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.system.domain.SysModule;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 项目模块视图对象 module
 *
 * @author admin
 * @date 2025-01-16
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = SysModule.class)
public class ModuleVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @ExcelProperty(value = "主键id")
    private Long id;

    /**
     * 模块名称
     */
    @ExcelProperty(value = "模块名称")
    private String name;

    /**
     * project.id
     */
    @ExcelProperty(value = "项目id")
    private Long projectId;

    /**
     * 模块类型，0：自定义添加，1：swagger导入，2：postman导入
     */
    @ExcelProperty(value = "模块类型，0：自定义添加，1：swagger导入，2：postman导入")
    private Long type;

    /**
     * 导入url
     */
    @ExcelProperty(value = "导入url")
    private String importUrl;

    /**
     * basic认证用户名
     */
    @ExcelProperty(value = "basic认证用户名")
    private String basicAuthUsername;

    /**
     * basic认证密码
     */
    @ExcelProperty(value = "basic认证密码")
    private String basicAuthPassword;

    /**
     * 开放接口调用token
     */
    @ExcelProperty(value = "开放接口调用token")
    private String token;

    /**
     * 新增操作方式，0：人工操作，1：开放平台推送
     */
    @ExcelProperty(value = "新增操作方式，0：人工操作，1：开放平台推送")
    private Long createMode;

    /**
     * 修改操作方式，0：人工操作，1：开放平台推送
     */
    @ExcelProperty(value = "修改操作方式，0：人工操作，1：开放平台推送")
    private Long modifyMode;

    /**
     * 创建者id
     */
    @ExcelProperty(value = "创建者id")
    private Long creatorId;

    /**
     * 修改者id
     */
    @ExcelProperty(value = "修改者id")
    private Long modifierId;

    /**
     * 排序索引
     */
    @ExcelProperty(value = "排序索引")
    private Long orderIndex;

    /**
     * 是否删除，0：未删除，1：已删除
     */
    @ExcelProperty(value = "是否删除，0：未删除，1：已删除")
    private Long isDeleted;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date gmtCreate;

    /**
     * 修改时间
     */
    @ExcelProperty(value = "修改时间")
    private Date gmtModified;


}
