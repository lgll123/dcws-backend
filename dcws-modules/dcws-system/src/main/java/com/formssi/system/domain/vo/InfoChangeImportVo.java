package com.formssi.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


/**
 * @author Michelle.Chung
 */
@Data
@NoArgsConstructor
public class InfoChangeImportVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotBlank(message = "序号不能为空")
    @ExcelProperty(value = "序号")
    private String num;

    /**
     * 档案编号
     */
    @ExcelProperty(value = "档案编号")
    private String documentId;

    /**
     * 档案日期
     */
    @ExcelProperty(value = "档案日期")
    private String documentDate;

    /**
     * 档案名称
     */
    @NotBlank(message = "档案名称不能为空")
    @ExcelProperty(value = "档案名称")
    private String documentName;

    /**
     * 原件
     */
    @NotBlank(message = "原件不能为空")
    @ExcelProperty(value = "原件")
    private String documentOriginal;

    /**
     * 复印件
     */
    @NotBlank(message = "复印件不能为空")
    @ExcelProperty(value = "复印件")
    private String documentCopy;

    /**
     * 电子
     */
    @NotBlank(message = "电子不能为空")
    @ExcelProperty(value = "电子")
    private String documentElectronic;

    /**
     * 其他
     */
    @ExcelProperty(value = "其他")
    private String other;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注")
    private String remark;

}
