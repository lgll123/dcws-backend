package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


/**
 * 许可证信息导入
 * @author yqh
 */
@Data
@NoArgsConstructor
public class LicensesInfoImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 软件名称
     */
    @NotBlank(message = "软件名称不能为空")
    @ExcelProperty(value = "软件名称")
    private String name;

    /**
     * 	分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @ExcelProperty(value = "分类名称")
    private String category;

    /**
     * 	分类ID
     */
    @ExcelIgnore
    private String categoryId;

    /**
     * 允许使用次数
     */
    @NotBlank(message = "允许使用次数不能为空")
    @ExcelProperty(value = "允许使用次数")
    private String seats;

    /**
     * 	产品序列号
     */
    @ExcelProperty(value = "产品序列号")
    private String serial;

    /**
     * 	制造商
     */
    @ExcelProperty(value = "制造商")
    private String manufacturer;
    /**
     * 	制造商ID
     */
    @ExcelIgnore
    private String manufacturerId;
    /**
     * 	许可人名字
     */
    @ExcelProperty(value = "许可人名字")
    private String licenseName;

    /**
     * 	许可电子邮件
     */
    @ExcelProperty(value = "许可电子邮件")
    private String licenseEmail;

    /**
     * 	到期日期
     */
    @NotBlank(message = "到期日期")
    @ExcelProperty(value = "到期日期")
    private String expirationDate;
    /**
     * 	备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

}
