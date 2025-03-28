package com.formssi.system.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
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
    @ExcelProperty(value = "序号")
    private String num;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "档案编号")
    private String documentId;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "档案日期")
    private String documentDate;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "档案名称")
    private String documentName;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "原件")
    private String documentOriginal;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "复印件")
    private String documentCopy;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "电子")
    private String documentElectronic;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "其他")
    private String other;

    /**
     * 用户ID
     */
    @ExcelProperty(value = "备注")
    private String remark;

}
