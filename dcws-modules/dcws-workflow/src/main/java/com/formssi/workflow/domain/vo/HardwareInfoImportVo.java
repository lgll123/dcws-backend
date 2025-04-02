package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


/**
 * 资产信息导入
 * @author yqh
 */
@Data
@NoArgsConstructor
public class HardwareInfoImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 资产名称
     */

    @ExcelProperty(value = "资产名称")
    private String name;

    /**
     * 	资产标签
     */
    @NotBlank(message = "资产标签不能为空")
    @ExcelProperty(value = "资产标签")
    private String assetTag;

    /**
     * 	序列号
     */
    @ExcelProperty(value = "序列号")
    private String serialNumber;

    /**
     * 	型号
     */
    @NotBlank(message = "型号不能为空")
    @ExcelProperty(value = "型号")
    private String modelNumber;

    /**
     * 	型号ID
     */
    @ExcelIgnore
    private String modelId;

    /**
     * 	类别
     */
/*    @ExcelProperty(value = "类别")
    private String category;*/

    /**
     * 位置
     */
    @NotBlank(message = "位置不能为空")
    @ExcelProperty(value = "位置")
    private String location;

    /**
     * 位置ID
     */
    @ExcelIgnore
    private String locationId;

    /**
     * 	采购价格
     */
    @ExcelProperty(value = "采购价格")
    private String purchaseCost;

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
     * 	备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

}
