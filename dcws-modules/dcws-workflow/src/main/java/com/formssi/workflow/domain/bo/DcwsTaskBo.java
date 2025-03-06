package com.formssi.workflow.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 任务请求对象
 *
 * @author may
 */
@Data
public class DcwsTaskBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务名称
     */
    private String name;

    /**
     * 流程定义名称
     */
    private String processDefinitionName;

    /**
     * 流程定义key
     */
    private String processDefinitionKey;

    /**
     * 流程分类
     */
    private String wfType;

    /**
     * 申请号
     */
    private String businessKey;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}
