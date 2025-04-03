package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import lombok.Data;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

@Data
@ExcelIgnoreUnannotated
public class DcwsInvoiceVo {
    @Serial
    private static final long serialVersionUID = 1L;

    /** 
     * 发票文件id
     */
    private String fileId;

    /**
     * 发票文件url
     */
    private String fileUrl;

    /** 
     * 发票名称
     */
    private String invoiceName;

    /** 
     * 发票类型
     */
    private String invoiceType;

    /** 
     * 校验
     */
    private String verify;

    /** 
     * 金额
     */
    private BigDecimal amount;

    /**
     * 发票号
     */
    private String invoiceId;

}