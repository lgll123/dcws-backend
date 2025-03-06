package com.formssi.workflow.utils;

import com.formssi.common.core.exception.ServiceException;
import com.formssi.workflow.common.constant.DcwsFlowConstant;
import com.formssi.workflow.domain.vo.ProcessNode;
import com.formssi.workflow.flowable.cmd.ExpressCmd;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.common.core.utils.StringUtils;
import org.flowable.bpmn.model.*;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;

import java.util.*;

/**
 * 工作流工具
 *
 * @author may
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DcwsWorkflowUtils {

    private static final ProcessEngine PROCESS_ENGINE = SpringUtils.getBean(ProcessEngine.class);

    /**
     * @description: 获取下一审批节点信息
     * @param: flowElements 全部节点
     * @param: flowElement 当前节点信息
     * @param: nextNodes 下一节点信息
     * @param: tempNodes 保存没有表达式的节点信息
     * @param: taskId 任务id
     * @param: gateway 网关
     * @return: void
     * @author: add by yqh
     */
    public static void getNextNodeList(Collection<FlowElement> flowElements, FlowElement flowElement, ExecutionEntityImpl executionEntity, List<ProcessNode> nextNodes, List<ProcessNode> tempNodes, String taskId, String gateway) {
        // 获取当前节点的连线信息
        List<SequenceFlow> outgoingFlows = ((FlowNode) flowElement).getOutgoingFlows();
        // 当前节点的所有下一节点出口
        for (SequenceFlow sequenceFlow : outgoingFlows) {
            // 下一节点的目标元素
            ProcessNode processNode = new ProcessNode();
            ProcessNode tempNode = new ProcessNode();
            FlowElement outFlowElement = sequenceFlow.getTargetFlowElement();
            if (outFlowElement instanceof UserTask) {
                nextNodeBuild(executionEntity, nextNodes, tempNodes, taskId, gateway, sequenceFlow, processNode, tempNode, outFlowElement);
                // ServiceTask
            } else if (outFlowElement instanceof ServiceTask) {
//                continue;
//                nextNodeBuild(executionEntity, nextNodes, tempNodes, taskId, gateway, sequenceFlow, processNode, tempNode, outFlowElement);
                //排他网关
            } else if (outFlowElement instanceof ExclusiveGateway) {
                Boolean condition = false;
                //如果是从包容网关分支到排他网关，包容网关条件成立才走排他网关
                if(DcwsFlowConstant.INCLUSIVE_GATEWAY.equals(gateway)){
                    String conditionExpression = sequenceFlow.getConditionExpression();
                    //判断是否有条件
                    if (StringUtils.isNotBlank(conditionExpression)) {
                        ExpressCmd expressCmd = new ExpressCmd(sequenceFlow, executionEntity);
                        condition = PROCESS_ENGINE.getManagementService().executeCommand(expressCmd);
                    }
                }
                if(condition){
                    getNextNodeList(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, DcwsFlowConstant.EXCLUSIVE_GATEWAY);
                }
                //并行网关
            } else if (outFlowElement instanceof ParallelGateway) {
                getNextNodeList(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, DcwsFlowConstant.PARALLEL_GATEWAY);
                //包含网关
            } else if (outFlowElement instanceof InclusiveGateway) {
                getNextNodeList(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, DcwsFlowConstant.INCLUSIVE_GATEWAY);
            } else if (outFlowElement instanceof EndEvent) {
                FlowElement subProcess = getSubProcess(flowElements, outFlowElement);
                if (subProcess == null) {
                    continue;
                }
                getNextNodeList(flowElements, subProcess, executionEntity, nextNodes, tempNodes, taskId, DcwsFlowConstant.END_EVENT);
            } else if (outFlowElement instanceof SubProcess) {
                Collection<FlowElement> subFlowElements = ((SubProcess) outFlowElement).getFlowElements();
                for (FlowElement element : subFlowElements) {
                    if (element instanceof StartEvent) {
                        List<SequenceFlow> startOutgoingFlows = ((StartEvent) element).getOutgoingFlows();
                        for (SequenceFlow outgoingFlow : startOutgoingFlows) {
                            FlowElement targetFlowElement = outgoingFlow.getTargetFlowElement();
                            if (targetFlowElement instanceof UserTask) {
                                nextNodeBuild(executionEntity, nextNodes, tempNodes, taskId, gateway, sequenceFlow, processNode, tempNode, targetFlowElement);
                                break;
                            }
                        }
                    }
                }
            } else {
                throw new ServiceException("未识别出节点类型");
            }
        }
    }
    /**
     * @description: 构建下一审批节点
     * @param: executionEntity
     * @param: nextNodes 下一节点信息
     * @param: tempNodes 保存没有表达式的节点信息(用于排他网关)
     * @param: taskId 任务id
     * @param: gateway 网关
     * @param: sequenceFlow  节点
     * @param: processNode 下一节点的目标元素
     * @param: tempNode  保存没有表达式的节点
     * @param: outFlowElement 目标节点
     * @return: void
     * @author: add by yqh
     */
    private static void nextNodeBuild(ExecutionEntityImpl executionEntity, List<ProcessNode> nextNodes, List<ProcessNode> tempNodes, String taskId, String gateway, SequenceFlow sequenceFlow, ProcessNode processNode, ProcessNode tempNode, FlowElement outFlowElement) {
        // 判断是否为排它网关
        if (DcwsFlowConstant.EXCLUSIVE_GATEWAY.equals(gateway)) {
            String conditionExpression = sequenceFlow.getConditionExpression();
            //判断是否有条件
            if (StringUtils.isNotBlank(conditionExpression)) {
                ExpressCmd expressCmd = new ExpressCmd(sequenceFlow, executionEntity);
                Boolean condition = PROCESS_ENGINE.getManagementService().executeCommand(expressCmd);
                processNodeBuildList(processNode, outFlowElement, DcwsFlowConstant.EXCLUSIVE_GATEWAY, taskId, condition, nextNodes);
            } else {
                tempNodeBuildList(tempNodes, taskId, tempNode, outFlowElement);
            }
            //包含网关
        } else if (DcwsFlowConstant.INCLUSIVE_GATEWAY.equals(gateway)) {
            String conditionExpression = sequenceFlow.getConditionExpression();
            if (StringUtils.isBlank(conditionExpression)) {
                processNodeBuildList(processNode, outFlowElement, DcwsFlowConstant.INCLUSIVE_GATEWAY, taskId, true, nextNodes);
            } else {
                ExpressCmd expressCmd = new ExpressCmd(sequenceFlow, executionEntity);
                Boolean condition = PROCESS_ENGINE.getManagementService().executeCommand(expressCmd);
                processNodeBuildList(processNode, outFlowElement, DcwsFlowConstant.INCLUSIVE_GATEWAY, taskId, condition, nextNodes);
            }
        }else if (DcwsFlowConstant.PARALLEL_GATEWAY.equals(gateway)) {// 并行网关
          //并行网关‌必须默认执行所有分支，其设计强制忽略条件表达式以保证严格并行性‌
          //即使显式设置了条件（如 ${condition}），也会被引擎强制忽略‌。所有外出分支都会被‌无条件执行‌，生成多个并发任务实例‌
          processNodeBuildList(processNode, outFlowElement, DcwsFlowConstant.PARALLEL_GATEWAY, taskId, true, nextNodes);

        } else {
            processNodeBuildList(processNode, outFlowElement, DcwsFlowConstant.USER_TASK, taskId, true, nextNodes);
        }
    }

    /**
     * @description: 临时节点信息(排他网关)
     * @param: tempNodes 临时节点集合
     * @param: taskId 任务id
     * @param: tempNode 节点对象
     * @param: outFlowElement 节点信息
     * @return: void
     * @author: add by yqh
     */
    private static void tempNodeBuildList(List<ProcessNode> tempNodes, String taskId, ProcessNode tempNode, FlowElement outFlowElement) {
        tempNode.setNodeId(outFlowElement.getId());
        tempNode.setNodeName(outFlowElement.getName());
        tempNode.setNodeType(DcwsFlowConstant.EXCLUSIVE_GATEWAY);
        tempNode.setTaskId(taskId);
        tempNode.setExpression(true);
        tempNode.setChooseWay(DcwsFlowConstant.WORKFLOW_ASSIGNEE);
        tempNode.setAssignee(((UserTask) outFlowElement).getAssignee());
        tempNode.setAssigneeId(((UserTask) outFlowElement).getAssignee());
        tempNodes.add(tempNode);
    }
    /**
     * @description: 保存节点信息
     * @param: processNode 节点对象
     * @param: outFlowElement 节点信息
     * @param: exclusiveGateway 网关
     * @param: taskId 任务id
     * @param: condition 条件
     * @param: nextNodes 节点集合
     * @return: void
     * @author: add by yqh
     */
    private static void processNodeBuildList(ProcessNode processNode, FlowElement outFlowElement, String exclusiveGateway, String taskId, Boolean condition, List<ProcessNode> nextNodes) {
        processNode.setNodeId(outFlowElement.getId());
        processNode.setNodeName(outFlowElement.getName());
        processNode.setNodeType(exclusiveGateway);
        processNode.setTaskId(taskId);
        processNode.setExpression(condition);
        processNode.setChooseWay(DcwsFlowConstant.WORKFLOW_ASSIGNEE);
        if(outFlowElement instanceof UserTask){
            processNode.setAssignee(((UserTask) outFlowElement).getAssignee());
            processNode.setCandidateUsers(((UserTask) outFlowElement).getCandidateUsers());
            processNode.setCandidateGroups(((UserTask) outFlowElement).getCandidateGroups());
            processNode.setAssigneeId(((UserTask) outFlowElement).getAssignee());
        } else {
            processNode.setAssignee(null);
            processNode.setCandidateUsers(new ArrayList());
            processNode.setCandidateGroups(new ArrayList());
            processNode.setAssigneeId(null);
        }

        nextNodes.add(processNode);
    }
    /**
     * @description: 判断是否为主流程结束节点
     * @param: flowElements全部节点
     * @param: endElement 结束节点
     * @return: org.flowable.bpmn.model.FlowElement
     * @author: add by yqh
     */
    public static FlowElement getSubProcess(Collection<FlowElement> flowElements, FlowElement endElement) {
        for (FlowElement mainElement : flowElements) {
            if (mainElement instanceof SubProcess) {
                for (FlowElement subEndElement : ((SubProcess) mainElement).getFlowElements()) {
                    if (endElement.equals(subEndElement)) {
                        return mainElement;
                    }
                }
            }
        }
        return null;
    }


}
