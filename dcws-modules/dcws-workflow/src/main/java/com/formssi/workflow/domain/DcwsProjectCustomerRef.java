package com.formssi.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dcws_project_customer_ref")
public class DcwsProjectCustomerRef extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

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
