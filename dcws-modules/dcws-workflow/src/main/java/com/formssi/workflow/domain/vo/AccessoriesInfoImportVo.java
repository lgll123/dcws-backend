package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


/**
 * 附属品信息导入
 * @author yqh
 */
@Data
@NoArgsConstructor
public class AccessoriesInfoImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配件名称
     */
    @NotBlank(message = "配件名称不能为空")
    @ExcelProperty(value = "配件名称")
    private String name;

    /**
     * 	配件类别
     */
    @NotBlank(message = "配件类别不能为空")
    @ExcelProperty(value = "配件类别")
    private String category;

    /**
     * 	配件类别ID
     */
    @ExcelIgnore
    private String categoryId;

    /**
     * 	型号
     */
    @ExcelProperty(value = "型号")
    private String modelNumber;

    /**
     * 	位置
     */
    @ExcelProperty(value = "位置")
    private String location;

    /**
     * 	位置ID
     */
    @ExcelIgnore
    private String locationId;

    /**
     * 	数量
     */
    @NotBlank(message = "配件数量不能为空")
    @ExcelProperty(value = "数量")
    private String num;

    /**
     * 采购价格
     */
    @ExcelProperty(value = "采购价格")
    private String purchaseCost;

    /**
     * 	购买日期
     */
    @ExcelProperty(value = "购买日期")
    private String purchaseDate;

    /**
     * 	供应商
     */
    @ExcelProperty(value = "供应商")
    private String supplier;

    /**
     * 	供应商Id
     */
    @ExcelIgnore
    private String supplierId;


}
