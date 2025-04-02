package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


/**
 * 消耗品信息导入
 * @author yqh
 */
@Data
@NoArgsConstructor
public class ConsumablesInfoImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 耗材名称
     */
    @NotBlank(message = "耗材名称不能为空")
    @ExcelProperty(value = "耗材名称")
    private String name;

    /**
     * 	类别
     */
    @NotBlank(message = "类别不能为空")
    @ExcelProperty(value = "类别")
    private String category;

    /**
     * 	类别Id
     */
    @ExcelIgnore
    private String categoryId;

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

    /**
     * 	位置
     */
    @ExcelProperty(value = "位置")
    private String location;
    /**
     * 	位置Id
     */
    @ExcelIgnore
    private String locationId;

    /**
     * 	型号
     */
    @ExcelProperty(value = "型号")
    private String modelNumber;

    /**
     * 	采购价格
     */
    @ExcelProperty(value = "采购价格")
    private String purchaseCost;
    /**
     * 数量
     */
    @NotBlank(message = "数量不能为空")
    @ExcelProperty(value = "数量")
    private String num;
    /**
     * 	备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

}
