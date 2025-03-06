package com.formssi.workflow.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DcwsTaskTypeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务类型
     */
    private Long taskType;

    /**
     * 任务名称
     */
    private String taskName;

}
