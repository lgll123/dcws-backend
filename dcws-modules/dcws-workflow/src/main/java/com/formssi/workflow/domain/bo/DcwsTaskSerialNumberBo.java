package com.formssi.workflow.domain.bo;

import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.DcwsTaskSerialNumber;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsTaskSerialNumber.class, reverseConvertGenerate = false)
public class DcwsTaskSerialNumberBo extends BaseEntity {

    /**
     * 系统名称
     */
    private String systemName;

    /**
     * '流程类型'
     */
    private String taskType;

    /**
     * '流程日期'
     */
    private String taskDate;

    /**
     * 流程序号
     */
    private Long taskNum;

}
