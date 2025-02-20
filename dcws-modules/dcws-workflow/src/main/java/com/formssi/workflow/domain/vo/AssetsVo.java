package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.Assets;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


/**
 * 物料申请视图对象 assets
 *
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = Assets.class)
public class AssetsVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @ExcelProperty(value = "主键")
    private Long id;
    /**
     * 申请部门
     */
    @ExcelProperty(value = "申请部门")
    private String applyDept;

    /**
     * 申请人
     */
    @ExcelProperty(value = "申请人")
    private String applicant;

    /**
     * 申请日期
     */
    @ExcelProperty(value = "申请日期")
    private Date applyDate;

    /**
     * 物料使用人
     */
    @ExcelProperty(value = "物料使用人")
    private String checkTo;

    /**
     * 申请原因
     */
    @ExcelProperty(value = "申请原因")
    private String applyReson;

    /**
     * 需求日期
     */
    @ExcelProperty(value = "需求日期")
    private String requiredDate;
    /**
     * 物料名称
     */
    @ExcelProperty(value = "物料名称")
    private String name;

    /**
     * 物料规格型号
     */
    @ExcelProperty(value = "物料规格型号")
    private String modelNo;

    /**
     * 物料数量
     */
    @ExcelProperty(value = "物料数量")
    private Integer number;

    /**
     * 物料使用地点
     */
    @ExcelProperty(value = "物料使用地点")
    private String localtion;



}
