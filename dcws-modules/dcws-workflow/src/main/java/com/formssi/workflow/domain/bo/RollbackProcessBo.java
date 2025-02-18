package com.formssi.workflow.domain.bo;

import com.formssi.common.core.validate.AddGroup;
import jakarta.validation.constraints.NotBlank;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * 撤回参数请求
 *
 * @author may
 */
@Data
public class RollbackProcessBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "业务id不能为空", groups = AddGroup.class)
    private String businessKey;

}
