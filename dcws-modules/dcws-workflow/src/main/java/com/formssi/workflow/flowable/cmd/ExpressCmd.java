package com.formssi.workflow.flowable.cmd;

import com.formssi.common.core.exception.ServiceException;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.impl.util.condition.ConditionUtil;

/**
 * @description: 校验流程变量
 * @author: gssong
 * @date: 2022/4/14 20:26
 */
public class ExpressCmd implements Command<Boolean> {

    private final SequenceFlow sequenceFlow;
    private final ExecutionEntityImpl executionEntity;

    public ExpressCmd(SequenceFlow sequenceFlow, ExecutionEntityImpl executionEntity) {
        this.sequenceFlow = sequenceFlow;
        this.executionEntity = executionEntity;
    }

    @Override
    public Boolean execute(CommandContext commandContext) {
        try {
            return ConditionUtil.hasTrueCondition(sequenceFlow, executionEntity);
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if(cause.getMessage().contains("网关条件为空，请检查配置")){
                throw new ServiceException(cause.getMessage());
            }else{
                throw new ServiceException(e.getMessage());
            }
        }
    }
}
