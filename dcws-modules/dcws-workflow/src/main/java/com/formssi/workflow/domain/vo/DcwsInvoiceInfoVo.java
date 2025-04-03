package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.formssi.workflow.domain.DcwsInvoiceInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

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

    /**
     * 申请人
     */
    @ExcelProperty(value = "申请人")
    private String applicant;

    /**
     * 申请部门
     */
    @ExcelProperty(value = "申请部门")
    private String applyDept;

    /**
     * 申请时间
     */
    @ExcelProperty(value = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date applyDate;

}
