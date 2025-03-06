package com.formssi.workflow.utils;

import cn.hutool.core.collection.CollectionUtil;
import com.formssi.common.core.exception.ServiceException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.vo.ProcessNode;
import com.formssi.workflow.common.constant.FlowConstant;
import org.flowable.bpmn.model.*;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 模型工具
 *
 * @author may
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DcwsModelUtils {

    private static final ProcessEngine PROCESS_ENGINE = SpringUtils.getBean(ProcessEngine.class);


    /**
     *
     * 根据当前节点任务ID获取下一个节点信息
     * add by yqh
     *
     * */

    public static List<ProcessNode>  getNextNodeinfo(TaskEntity task){
//        TaskEntity task = (TaskEntity) PROCESS_ENGINE.getTaskService().createTaskQuery().taskId(taskId).singleResult();
        if (task.isSuspended()) {
            throw new ServiceException(FlowConstant.MESSAGE_SUSPENDED);
        }
        //流程定义
        String processDefinitionId = task.getProcessDefinitionId();
        //查询bpmn信息
        BpmnModel bpmnModel = PROCESS_ENGINE.getRepositoryService().getBpmnModel(processDefinitionId);
        //通过任务节点id，来获取当前节点信息
        FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        //全部节点
        Collection<FlowElement> flowElements = bpmnModel.getProcesses().get(0).getFlowElements();
        //封装下一个用户任务节点信息
        List<ProcessNode> nextNodeList = new ArrayList<>();
        //保存没有表达式的节点
        List<ProcessNode> tempNodeList = new ArrayList<>();
        ExecutionEntityImpl executionEntity = (ExecutionEntityImpl) PROCESS_ENGINE.getRuntimeService().createExecutionQuery()
                .executionId(task.getExecutionId()).singleResult();
        DcwsWorkflowUtils.getNextNodeList(flowElements, flowElement, executionEntity, nextNodeList, tempNodeList, task.getId(), null);
        if (CollectionUtil.isNotEmpty(nextNodeList)) {
            nextNodeList.removeIf(node -> !node.getExpression());
        }
        if (CollectionUtil.isNotEmpty(nextNodeList) && CollectionUtil.isNotEmpty(nextNodeList.stream().filter(e -> e.getExpression() != null && e.getExpression()).collect(Collectors.toList()))) {
            List<ProcessNode> nodeList = nextNodeList.stream().filter(e -> e.getExpression() != null && e.getExpression()).collect(Collectors.toList());
            List<ProcessNode> processNodeList = getProcessNodeAssigneeList(nodeList, task.getProcessDefinitionId());
            return processNodeList;
        } else if (CollectionUtil.isNotEmpty(tempNodeList)) {
            List<ProcessNode> processNodeList = getProcessNodeAssigneeList(tempNodeList, task.getProcessDefinitionId());
            return processNodeList;
        } else {
            return nextNodeList;
        }
    }

    /**
     * @description: 设置节点审批人员
     * @param: nodeList节点列表
     * @param: definitionId 流程定义id
     * @return: java.util.List<com.ruoyi.workflow.domain.vo.ProcessNode>
     * @author: add by yqh
     */
    private static List<ProcessNode> getProcessNodeAssigneeList(List<ProcessNode> nodeList, String definitionId) {
       /* List<ActNodeAssignee> actNodeAssignees = iActNodeAssigneeService.getInfoByProcessDefinitionId(definitionId);
        if (CollUtil.isEmpty(actNodeAssignees)) {
            throw new ServiceException("当前流程定义未配置审批人，请联系管理员！");
        }
        for (ProcessNode processNode : nodeList) {
            if (CollectionUtil.isEmpty(actNodeAssignees)) {
                throw new ServiceException("该流程定义未配置，请联系管理员！");
            }
            ActNodeAssignee nodeAssignee = actNodeAssignees.stream().filter(e -> e.getNodeId().equals(processNode.getNodeId())).findFirst().orElse(null);

            //按角色 部门 人员id 等设置查询人员信息
            if (ObjectUtil.isNotNull(nodeAssignee) && StringUtils.isNotBlank(nodeAssignee.getAssigneeId())
                && nodeAssignee.getBusinessRuleId() == null && StringUtils.isNotBlank(nodeAssignee.getAssignee())) {
                processNode.setChooseWay(nodeAssignee.getChooseWay());
                processNode.setAssignee(nodeAssignee.getAssignee());
                processNode.setAssigneeId(nodeAssignee.getAssigneeId());
                processNode.setIsShow(nodeAssignee.getIsShow());
                if (nodeAssignee.getMultiple()) {
                    processNode.setNodeId(nodeAssignee.getMultipleColumn());
                }
                processNode.setMultiple(nodeAssignee.getMultiple());
                processNode.setMultipleColumn(nodeAssignee.getMultipleColumn());
                //按照业务规则设置查询人员信息
            } else if (ObjectUtil.isNotNull(nodeAssignee) && nodeAssignee.getBusinessRuleId() != null) {
                ActBusinessRuleVo actBusinessRuleVo = iActBusinessRuleService.queryById(nodeAssignee.getBusinessRuleId());
                List<String> ruleAssignList = WorkflowUtils.ruleAssignList(actBusinessRuleVo, processNode.getTaskId(), processNode.getNodeName());
                processNode.setChooseWay(nodeAssignee.getChooseWay());
                processNode.setAssignee(StrUtil.EMPTY);
                processNode.setAssigneeId(String.join(StringUtils.SEPARATOR, ruleAssignList));
                processNode.setIsShow(nodeAssignee.getIsShow());
                processNode.setBusinessRuleId(nodeAssignee.getBusinessRuleId());
                if (Boolean.TRUE.equals(nodeAssignee.getMultiple())) {
                    processNode.setNodeId(nodeAssignee.getMultipleColumn());
                }
                processNode.setMultiple(nodeAssignee.getMultiple());
                processNode.setMultipleColumn(nodeAssignee.getMultipleColumn());
            } else {
                throw new ServiceException(processNode.getNodeName() + "未配置审批人，请联系管理员！");
            }
        }*/
        /*if (CollectionUtil.isNotEmpty(nodeList)) {
            // 去除不需要弹窗选人的节点
            nodeList.removeIf(node -> !node.getIsShow());
        }*/
        return nodeList;
    }
}
