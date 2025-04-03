package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.DcwsInvoiceInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsInvoiceInfo.class)
public class DcwsInvoiceInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 发票号
     */
    @ExcelProperty(value = "发票号")
    private String invoiceId;

    /**
     * 发票名称
     */
    @ExcelProperty(value = "发票名称")
    private String fileName;

    /**
     * URL地址
     */
    @ExcelProperty(value = "URL地址")
    private String fileUrl;

    /**
     * 业务id
     */
    @ExcelProperty(value = "业务id")
    private String businessKey;

    /**
     * 业务类型
     */
    @ExcelProperty(value = "业务类型")
    private String applyType;

    /**
     * 发票金额
     */
    @ExcelProperty(value = "发票金额")
    private BigDecimal amount;

}
