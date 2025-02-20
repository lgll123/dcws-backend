package com.formssi.workflow.domain.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @description: 流程定义查询
 * @author: gssong
 * @date: 2021/10/07 11:15
 */
@Data
public class NextNodeBo implements Serializable {

    private static final long serialVersionUID=1L;
    /**
     * 任务id
     */
    private String taskId;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;

    public Map<String, Object> getVariables() {
        if(variables == null){
            return new HashMap<>(16);
        }
        variables.entrySet().removeIf(entry -> Objects.isNull(entry.getValue()));
        return variables;
    }
}
