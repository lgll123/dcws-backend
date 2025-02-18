package com.formssi.workflow.domain.bo;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 表单状态参数请求
 *
 * @author may
 */
@Data
public class FormStatusBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String taskId;

    private String router;

}
