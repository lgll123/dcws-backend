package com.formssi.workflow.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.DcwsInvoiceInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsInvoiceInfo.class, reverseConvertGenerate = false)
public class DcwsInvoiceInfoBo extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 发票号
     */
    private String invoiceId;

    /**
     * 发票名称
     */
    private String fileName;

    /**
     * URL地址
     */
    private String fileUrl;

    /**
     * 业务id
     */
    private String businessKey;

    /**
     * 业务类型
     */
    private String applyType;

    /**
     * 发票金额
     */
    private BigDecimal amount;

    /**
     * 申请人
     */
    private String applicant;

    /**
     * 申请部门
     */
    private String applyDept;

    /**
     * 申请时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date applyDate;

}
