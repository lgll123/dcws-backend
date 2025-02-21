package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.DcwsProjectCustomerRef;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsProjectCustomerRef.class)
public class DcwsProjectCustomerRefVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目id
     */
    @ExcelProperty(value = "项目id")
    private Long projectId;

    /**
     * 客户id
     */
    @ExcelProperty(value = "客户id")
    private Long customerId;

    /**
     * 项目名称
     */
    @ExcelProperty(value = "项目名称")
    private String customerName;

    /**
     * 项目类型
     */
    @ExcelProperty(value = "项目类型")
    private String customerType;

}
