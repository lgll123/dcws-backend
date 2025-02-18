package com.formssi.generator.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 表名：dm_sub_table
 * 备注：代码生成业务子表
 *
 * @author lizhangyu
 */
@Data
@TableName("dm_sub_table")
public class DmSubTable {

    /** 
     * 编号
     */
    private Long subTableId;

    /** 
     * 主表编号
     */
    private Long tableId;

    /** 
     * 子表名称
     */
    private String subTableName;

    /** 
     * 子表描述
     */
    private String subTableComment;

    /** 
     * 子表外键
     */
    private String subTableForeignKey;

    /** 
     * 与主表关系（0：一对一，1：一对多）
     */
    private Integer mainRelation;

    /** 
     * 排序
     */
    private Integer sort;

    /** 
     * 创建时间
     */
    private Date createTime;

    /** 
     * 创建人
     */
    private Long createBy;

    /** 
     * 更新时间
     */
    private Date updateTime;

    /** 
     * 更新人
     */
    private Long updateBy;

    /** 
     * 逻辑删除: 1-已删除 0-未删除
     */
    private Integer deleted;

    /**
     * 表列信息
     */
    @Valid
    @TableField(exist = false)
    private List<DmTableColumn> columns;

}