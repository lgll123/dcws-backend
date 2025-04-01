package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


/**
 * 组件证信息导入
 * @author yqh
 */
@Data
@NoArgsConstructor
public class ComponentsInfoImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 名称
     */
    @NotBlank(message = "名称不能为空")
    @ExcelProperty(value = "名称")
    private String name;

    /**
     * 	类别
     */
    @NotBlank(message = "类别不能为空")
    @ExcelProperty(value = "类别")
    private String category;

    /**
     * 数量
     */
    @NotBlank(message = "数量不能为空")
    @ExcelProperty(value = "数量")
    private String num;

    /**
     * 	序列号
     */
    @ExcelProperty(value = "序列号")
    private String serial;

    /**
     * 	位置
     */
    @ExcelProperty(value = "位置")
    private String location;
    /**
     * 	供应商
     */
    @ExcelProperty(value = "供应商")
    private String supplier;
    /**
     * 	采购价格
     */
    @ExcelProperty(value = "采购价格")
    private String purchaseCost;

    /**
     * 	备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

}
