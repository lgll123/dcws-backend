package com.formssi.workflow.domain.bo;

import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.DcwsInvoiceInfo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

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

}
