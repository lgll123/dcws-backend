package com.formssi.workflow.domain.bo;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import com.formssi.common.core.validate.AddGroup;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 启动流程对象
 *
 * @author may
 */
@Data
public class StartProcessBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 表名路由
     */
    @NotBlank(message = "表名路由", groups = {AddGroup.class})
    private String router;

    /**
     * 业务唯一值id
     */
    @NotBlank(message = "业务ID不能为空", groups = {AddGroup.class})
    private String businessKey;


    /**
     * 流程变量，前端会提交一个元素{'entity': {业务详情数据对象}}
     */
    private Map<String, Object> variables;

    public Map<String, Object> getVariables() {
        if (variables == null) {
            return new HashMap<>(16);
        }
        variables.entrySet().removeIf(entry -> Objects.isNull(entry.getValue()));
        return variables;
    }
}
