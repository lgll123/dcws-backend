package com.formssi.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dcws_task_serial_number")
public class DcwsTaskSerialNumber extends DcwsBaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

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
