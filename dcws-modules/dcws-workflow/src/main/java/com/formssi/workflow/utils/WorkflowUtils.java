package com.formssi.workflow.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.workflow.common.CommonField;
import com.formssi.workflow.domain.vo.ProcessNode;
import com.formssi.workflow.domain.vo.WfFormPermissionVo;
import com.formssi.workflow.flowable.cmd.ExpressCmd;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.formssi.common.core.domain.dto.RoleDTO;
import com.formssi.common.core.domain.dto.UserDTO;
import com.formssi.common.core.service.UserService;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mail.utils.MailUtils;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.common.tenant.helper.TenantHelper;
import com.formssi.common.websocket.dto.WebSocketMessageDto;
import com.formssi.common.websocket.utils.WebSocketUtils;
import com.formssi.workflow.common.constant.FlowConstant;
import com.formssi.workflow.common.enums.MessageTypeEnum;
import com.formssi.workflow.common.enums.TaskStatusEnum;
import com.formssi.workflow.domain.ActHiTaskinst;
import com.formssi.workflow.domain.vo.MultiInstanceVo;
import com.formssi.workflow.domain.vo.ParticipantVo;
import com.formssi.workflow.flowable.cmd.UpdateHiTaskInstCmd;
import com.formssi.workflow.mapper.ActHiTaskinstMapper;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

import java.util.*;

/**
 * 工作流工具
 *
 * @author may
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkflowUtils {

    private static final ProcessEngine PROCESS_ENGINE = SpringUtils.getBean(ProcessEngine.class);
    private static final ActHiTaskinstMapper ACT_HI_TASKINST_MAPPER = SpringUtils.getBean(ActHiTaskinstMapper.class);

    /**
     * 创建一个新任务
     *
     * @param currentTask 参数
     */
    public static TaskEntity createNewTask(Task currentTask) {
        TaskEntity task = null;
        if (ObjectUtil.isNotEmpty(currentTask)) {
            task = (TaskEntity) PROCESS_ENGINE.getTaskService().newTask();
            task.setCategory(currentTask.getCategory());
            task.setDescription(currentTask.getDescription());
            task.setAssignee(currentTask.getAssignee());
            task.setName(currentTask.getName());
            task.setProcessDefinitionId(currentTask.getProcessDefinitionId());
            task.setProcessInstanceId(currentTask.getProcessInstanceId());
            task.setTaskDefinitionKey(currentTask.getTaskDefinitionKey());
            task.setPriority(currentTask.getPriority());
            task.setCreateTime(new Date());
            task.setTenantId(TenantHelper.getTenantId());
            PROCESS_ENGINE.getTaskService().saveTask(task);
        }
        if (ObjectUtil.isNotNull(task)) {
            UpdateHiTaskInstCmd updateHiTaskInstCmd = new UpdateHiTaskInstCmd(Collections.singletonList(task.getId()), task.getProcessDefinitionId(), task.getProcessInstanceId());
            PROCESS_ENGINE.getManagementService().executeCommand(updateHiTaskInstCmd);
        }
        return task;
    }

    /**
     * 抄送任务
     *
     * @param parentTaskList 父级任务
     * @param userIds        人员id
     */
    public static void createCopyTask(List<Task> parentTaskList, List<Long> userIds) {
        List<Task> list = new ArrayList<>();
        String tenantId = TenantHelper.getTenantId();
        for (Task parentTask : parentTaskList) {
            for (Long userId : userIds) {
                TaskEntity newTask = (TaskEntity) PROCESS_ENGINE.getTaskService().newTask();
                newTask.setParentTaskId(parentTask.getId());
                newTask.setAssignee(userId.toString());
                newTask.setName("【抄送】-" + parentTask.getName());
                newTask.setProcessDefinitionId(parentTask.getProcessDefinitionId());
                newTask.setProcessInstanceId(parentTask.getProcessInstanceId());
                newTask.setTaskDefinitionKey(parentTask.getTaskDefinitionKey());
                newTask.setTenantId(tenantId);
                list.add(newTask);
            }
        }
        PROCESS_ENGINE.getTaskService().bulkSaveTasks(list);
        if (CollUtil.isNotEmpty(list) && CollUtil.isNotEmpty(parentTaskList)) {
            String processInstanceId = parentTaskList.get(0).getProcessInstanceId();
            String processDefinitionId = parentTaskList.get(0).getProcessDefinitionId();
            List<String> taskIds = StreamUtils.toList(list, Task::getId);
            ActHiTaskinst actHiTaskinst = new ActHiTaskinst();
            actHiTaskinst.setProcDefId(processDefinitionId);
            actHiTaskinst.setProcInstId(processInstanceId);
            actHiTaskinst.setScopeType(TaskStatusEnum.COPY.getStatus());
            actHiTaskinst.setTenantId(tenantId);
            LambdaUpdateWrapper<ActHiTaskinst> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.in(ActHiTaskinst::getId, taskIds);
            ACT_HI_TASKINST_MAPPER.update(actHiTaskinst, updateWrapper);
            for (Task task : list) {
                PROCESS_ENGINE.getTaskService().addComment(task.getId(), task.getProcessInstanceId(), TaskStatusEnum.COPY.getStatus(), StrUtil.EMPTY);
            }
        }
    }

    /**
     * 获取当前任务参与者
     *
     * @param taskId 任务id
     */
    public static ParticipantVo getCurrentTaskParticipant(String taskId, UserService userService) {
        ParticipantVo participantVo = new ParticipantVo();
        List<HistoricIdentityLink> linksForTask = PROCESS_ENGINE.getHistoryService().getHistoricIdentityLinksForTask(taskId);
        Task task = QueryUtils.taskQuery().taskId(taskId).singleResult();
        if (task != null && CollUtil.isNotEmpty(linksForTask)) {
            List<HistoricIdentityLink> groupList = StreamUtils.filter(linksForTask, e -> StringUtils.isNotBlank(e.getGroupId()));
            if (CollUtil.isNotEmpty(groupList)) {
                List<Long> groupIds = StreamUtils.toList(groupList, e -> Long.valueOf(e.getGroupId()));
                List<Long> userIds = userService.selectUserIdsByRoleIds(groupIds);
                if (CollUtil.isNotEmpty(userIds)) {
                    participantVo.setGroupIds(groupIds);
                    List<UserDTO> userList = userService.selectListByIds(userIds);
                    if (CollUtil.isNotEmpty(userList)) {
                        List<Long> userIdList = StreamUtils.toList(userList, UserDTO::getUserId);
                        List<String> nickNames = StreamUtils.toList(userList, UserDTO::getNickName);
                        participantVo.setCandidate(userIdList);
                        participantVo.setCandidateName(nickNames);
                        participantVo.setClaim(!StringUtils.isBlank(task.getAssignee()));
                    }
                }
            } else {
                List<HistoricIdentityLink> candidateList = StreamUtils.filter(linksForTask, e -> FlowConstant.CANDIDATE.equals(e.getType()));
                List<Long> userIdList = new ArrayList<>();
                for (HistoricIdentityLink historicIdentityLink : linksForTask) {
                    try {
                        userIdList.add(Long.valueOf(historicIdentityLink.getUserId()));
                    } catch (NumberFormatException ignored) {

                    }
                }
                List<UserDTO> userList = userService.selectListByIds(userIdList);
                if (CollUtil.isNotEmpty(userList)) {
                    List<Long> userIds = StreamUtils.toList(userList, UserDTO::getUserId);
                    List<String> nickNames = StreamUtils.toList(userList, UserDTO::getNickName);
                    participantVo.setCandidate(userIds);
                    participantVo.setCandidateName(nickNames);
                    // 判断当前任务是否具有多个办理人
                    if (CollUtil.isNotEmpty(candidateList) && candidateList.size() > 1) {
                        // 如果 assignee 存在，则设置当前任务已经被认领
                        participantVo.setClaim(StringUtils.isNotBlank(task.getAssignee()));
                    }
                }
            }
        }
        return participantVo;
    }

    /**
     * 判断当前节点是否为会签节点
     *
     * @param processDefinitionId 流程定义id
     * @param taskDefinitionKey   流程定义id
     */
    public static MultiInstanceVo isMultiInstance(String processDefinitionId, String taskDefinitionKey) {
        BpmnModel bpmnModel = PROCESS_ENGINE.getRepositoryService().getBpmnModel(processDefinitionId);
        FlowNode flowNode = (FlowNode) bpmnModel.getFlowElement(taskDefinitionKey);
        MultiInstanceVo multiInstanceVo = new MultiInstanceVo();
        //判断是否为并行会签节点
        if (flowNode.getBehavior() instanceof ParallelMultiInstanceBehavior behavior && behavior.getCollectionExpression() != null) {
            Expression collectionExpression = behavior.getCollectionExpression();
            String assigneeList = collectionExpression.getExpressionText();
            String assignee = behavior.getCollectionElementVariable();
            multiInstanceVo.setType(behavior);
            multiInstanceVo.setAssignee(assignee);
            multiInstanceVo.setAssigneeList(assigneeList);
            return multiInstanceVo;
            //判断是否为串行会签节点
        } else if (flowNode.getBehavior() instanceof SequentialMultiInstanceBehavior behavior && behavior.getCollectionExpression() != null) {
            Expression collectionExpression = behavior.getCollectionExpression();
            String assigneeList = collectionExpression.getExpressionText();
            String assignee = behavior.getCollectionElementVariable();
            multiInstanceVo.setType(behavior);
            multiInstanceVo.setAssignee(assignee);
            multiInstanceVo.setAssigneeList(assigneeList);
            return multiInstanceVo;
        }
        return null;
    }

    /**
     * 获取当前流程状态
     *
     * @param taskId 任务id
     */
    public static String getBusinessStatusByTaskId(String taskId) {
        HistoricTaskInstance historicTaskInstance = QueryUtils.hisTaskInstanceQuery().taskId(taskId).singleResult();
        HistoricProcessInstance historicProcessInstance = QueryUtils.hisInstanceQuery(historicTaskInstance.getProcessInstanceId()).singleResult();
        return historicProcessInstance.getBusinessStatus();
    }

    /**
     * 获取当前流程状态
     *
     * @param businessKey 业务id
     */
    public static String getBusinessStatus(String businessKey) {
        HistoricProcessInstance historicProcessInstance = QueryUtils.hisBusinessKeyQuery(businessKey).singleResult();
        return historicProcessInstance.getBusinessStatus();
    }

    /**
     * 发送消息
     *
     * @param list        任务
     * @param name        流程名称
     * @param messageType 消息类型
     * @param message     消息内容，为空则发送默认配置的消息内容
     */
    public static void sendMessage(List<Task> list, String name, List<String> messageType, String message, UserService userService) {
        Set<Long> userIds = new HashSet<>();
        if (StringUtils.isBlank(message)) {
            message = "有新的【" + name + "】单据已经提交至您的待办，请您及时处理。";
        }
        for (Task t : list) {
            ParticipantVo taskParticipant = WorkflowUtils.getCurrentTaskParticipant(t.getId(), userService);
            if (CollUtil.isNotEmpty(taskParticipant.getGroupIds())) {
                List<Long> userIdList = userService.selectUserIdsByRoleIds(taskParticipant.getGroupIds());
                if (CollUtil.isNotEmpty(userIdList)) {
                    userIds.addAll(userIdList);
                }
            }
            List<Long> candidate = taskParticipant.getCandidate();
            if (CollUtil.isNotEmpty(candidate)) {
                userIds.addAll(candidate);
            }
        }
        if (CollUtil.isNotEmpty(userIds)) {
            List<UserDTO> userList = userService.selectListByIds(new ArrayList<>(userIds));
            for (String code : messageType) {
                MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByCode(code);
                if (ObjectUtil.isNotEmpty(messageTypeEnum)) {
                    switch (messageTypeEnum) {
                        case SYSTEM_MESSAGE:
                            WebSocketMessageDto dto = new WebSocketMessageDto();
                            dto.setSessionKeys(new ArrayList<>(userIds));
                            dto.setMessage(message);
                            WebSocketUtils.publishMessage(dto);
                            break;
                        case EMAIL_MESSAGE:
                            MailUtils.sendText(StreamUtils.join(userList, UserDTO::getEmail), "单据审批提醒", message);
                            break;
                        case SMS_MESSAGE:
                            break;
                    }
                }
            }
        }
    }

    /**
     * 根据任务id查询 当前用户的任务，检查 当前人员 是否是该 taskId 的办理人
     *
     * @param taskId 任务id
     * @return 结果
     */
    public static Task getTaskByCurrentUser(String taskId) {
        TaskQuery taskQuery = QueryUtils.taskQuery();
        taskQuery.taskId(taskId).taskCandidateOrAssigned(String.valueOf(LoginHelper.getUserId()));

        List<RoleDTO> roles = LoginHelper.getLoginUser().getRoles();
        if (CollUtil.isNotEmpty(roles)) {
            List<String> groupIds = StreamUtils.toList(roles, e -> String.valueOf(e.getRoleId()));
            taskQuery.taskCandidateGroupIn(groupIds);
        }
        return taskQuery.singleResult();
    }

    public static CommonField getCommonField(String processDefinitionKey) {
        CommonField commonField = null;
        // 获取流程模型
        ProcessDefinition processDefinition = PROCESS_ENGINE.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .latestVersion()
            .singleResult();
        // 获取流程模型
        BpmnModel bpmnModel = PROCESS_ENGINE.getRepositoryService()
            .getBpmnModel(processDefinition.getId());
        Process process = bpmnModel.getMainProcess();
        for (FlowElement flowElement : process.getFlowElements()) {
            if (flowElement instanceof StartEvent startEvent) {
                List<FlowableListener> executionListeners = startEvent.getExecutionListeners();
                if (!executionListeners.isEmpty()) {
                    for (FlowableListener listener : executionListeners) {
                        // 公用过滤器
                        if ("start".equals(listener.getEvent())
                            && "${commonEventListener.init()}".equals(listener.getImplementation())) {
                            commonField = new CommonField();
                            for (FieldExtension extension : listener.getFieldExtensions()) {
                                if (FieldUtil.getFieldName(CommonField::getTable)
                                    .equals(extension.getFieldName())) {
                                    commonField.setTable(extension.getStringValue());
                                } else if (FieldUtil.getFieldName(CommonField::getPrimaryKey)
                                    .equals(extension.getFieldName())) {
                                    commonField.setPrimaryKey(extension.getStringValue());
                                } else if (FieldUtil.getFieldName(CommonField::getStatus)
                                    .equals(extension.getFieldName())) {
                                    commonField.setStatus(extension.getStringValue());
                                }
                            }
                        }
                    }
                }
            }
        }
        return commonField;
    }

    public static List<WfFormPermissionVo> getFormPermissionByProcessKey(String processDefinitionKey) {
        ProcessDefinition processDefinition = PROCESS_ENGINE.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .latestVersion()
            .singleResult();
        List<WfFormPermissionVo> wfFormPermissionVoList = null;
        // 获取流程模型
        BpmnModel bpmnModel = PROCESS_ENGINE.getRepositoryService()
            .getBpmnModel(processDefinition.getId());
        Process process = bpmnModel.getMainProcess();
        for (FlowElement flowElement : process.getFlowElements()) {
            if (flowElement instanceof UserTask userTask) { //加载第一个表单
                List<FormProperty> formProperties = userTask.getFormProperties();
                if (!formProperties.isEmpty()) {
                    wfFormPermissionVoList = new ArrayList<>();
                    for (FormProperty property : formProperties) {
                        wfFormPermissionVoList.add(new WfFormPermissionVo(property.getId(),
                            property.isReadable(), property.isWriteable()));
                    }
                }
                break;
            }
        }
        return wfFormPermissionVoList;
    }

    public static List<WfFormPermissionVo> getFormPermissionByTaskId(String taskId) {
        Task task = PROCESS_ENGINE.getTaskService().createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return null;
        }
        List<WfFormPermissionVo> wfFormPermissionVoList = null;
        // 获取流程模型
        BpmnModel bpmnModel = PROCESS_ENGINE.getRepositoryService()
            .getBpmnModel(task.getProcessDefinitionId());
        Process process = bpmnModel.getMainProcess();
        for (FlowElement flowElement : process.getFlowElements()) {
            if (flowElement instanceof UserTask userTask) {
                List<FormProperty> formProperties = userTask.getFormProperties();
                if (task.getTaskDefinitionKey().equals(userTask.getId()) && !formProperties.isEmpty()) {
                    wfFormPermissionVoList = new ArrayList<>();
                    for (FormProperty property : formProperties) {
                        wfFormPermissionVoList.add(new WfFormPermissionVo(property.getId(),
                            property.isReadable(), property.isWriteable()));
                    }
                    break;
                }
            }
        }
        return wfFormPermissionVoList;
    }


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
                //并行网关
            } else if (outFlowElement instanceof ExclusiveGateway) {
                getNextNodeList(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, FlowConstant.EXCLUSIVE_GATEWAY);
                //并行网关
            } else if (outFlowElement instanceof ParallelGateway) {
                getNextNodeList(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, FlowConstant.PARALLEL_GATEWAY);
                //包含网关
            } else if (outFlowElement instanceof InclusiveGateway) {
                getNextNodeList(flowElements, outFlowElement, executionEntity, nextNodes, tempNodes, taskId, FlowConstant.INCLUSIVE_GATEWAY);
            } else if (outFlowElement instanceof EndEvent) {
                FlowElement subProcess = getSubProcess(flowElements, outFlowElement);
                if (subProcess == null) {
                    continue;
                }
                getNextNodeList(flowElements, subProcess, executionEntity, nextNodes, tempNodes, taskId, FlowConstant.END_EVENT);
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
        if (FlowConstant.EXCLUSIVE_GATEWAY.equals(gateway)) {
            String conditionExpression = sequenceFlow.getConditionExpression();
            //判断是否有条件
            if (StringUtils.isNotBlank(conditionExpression)) {
                ExpressCmd expressCmd = new ExpressCmd(sequenceFlow, executionEntity);
                Boolean condition = PROCESS_ENGINE.getManagementService().executeCommand(expressCmd);
                processNodeBuildList(processNode, outFlowElement, FlowConstant.EXCLUSIVE_GATEWAY, taskId, condition, nextNodes);
            } else {
                tempNodeBuildList(tempNodes, taskId, tempNode, outFlowElement);
            }
            //包含网关
        } else if (FlowConstant.INCLUSIVE_GATEWAY.equals(gateway)) {
            String conditionExpression = sequenceFlow.getConditionExpression();
            if (StringUtils.isBlank(conditionExpression)) {
                processNodeBuildList(processNode, outFlowElement, FlowConstant.INCLUSIVE_GATEWAY, taskId, true, nextNodes);
            } else {
                ExpressCmd expressCmd = new ExpressCmd(sequenceFlow, executionEntity);
                Boolean condition = PROCESS_ENGINE.getManagementService().executeCommand(expressCmd);
                processNodeBuildList(processNode, outFlowElement, FlowConstant.INCLUSIVE_GATEWAY, taskId, condition, nextNodes);
            }
        }else if (FlowConstant.PARALLEL_GATEWAY.equals(gateway)) {// 并行网关
            String conditionExpression = sequenceFlow.getConditionExpression();
            if (StringUtils.isBlank(conditionExpression)) {
                processNodeBuildList(processNode, outFlowElement, FlowConstant.PARALLEL_GATEWAY, taskId, true, nextNodes);
            } else {
                ExpressCmd expressCmd = new ExpressCmd(sequenceFlow, executionEntity);
                Boolean condition = PROCESS_ENGINE.getManagementService().executeCommand(expressCmd);
                processNodeBuildList(processNode, outFlowElement, FlowConstant.PARALLEL_GATEWAY, taskId, condition, nextNodes);
            }
        } else {
            processNodeBuildList(processNode, outFlowElement, FlowConstant.USER_TASK, taskId, true, nextNodes);
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
        tempNode.setNodeType(FlowConstant.EXCLUSIVE_GATEWAY);
        tempNode.setTaskId(taskId);
        tempNode.setExpression(true);
        tempNode.setChooseWay(FlowConstant.WORKFLOW_ASSIGNEE);
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
        processNode.setChooseWay(FlowConstant.WORKFLOW_ASSIGNEE);
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
