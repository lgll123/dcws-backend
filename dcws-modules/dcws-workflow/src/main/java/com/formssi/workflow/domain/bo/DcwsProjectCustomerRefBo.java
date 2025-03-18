package com.formssi.workflow.domain.bo;

import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.DcwsProjectCustomerRef;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsProjectCustomerRef.class, reverseConvertGenerate = false)
public class DcwsProjectCustomerRefBo extends BaseEntity {

    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 客户id
     */
    private Long customerId;

    /**
     * 项目名称
     */
    private String customerName;

    /**
     * 项目类型
     */
    private String customerType;

}
