package com.formssi.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.utils.DateUtils;
import com.formssi.workflow.mapper.DcwsActTaskMapper;
import com.formssi.workflow.service.IWfNodeConfigService;
import com.formssi.workflow.service.IWfTaskBackNodeService;
import com.formssi.workflow.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.formssi.common.core.domain.dto.RoleDTO;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.service.OssService;
import com.formssi.common.core.service.UserService;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.common.tenant.helper.TenantHelper;
import com.formssi.workflow.common.constant.FlowConstant;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.workflow.common.enums.TaskStatusEnum;
import com.formssi.workflow.domain.bo.*;
import com.formssi.workflow.domain.vo.*;
import com.formssi.workflow.flowable.cmd.*;
import com.formssi.workflow.flowable.handler.DcwsFlowProcessEventHandler;
import com.formssi.workflow.service.DcwsIActTaskService;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.*;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.api.history.HistoricIdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务 服务层实现
 *
 * @author may
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DcwsActTaskServiceImpl implements DcwsIActTaskService {

    @Autowired(required = false)
    private RuntimeService runtimeService;
    @Autowired(required = false)
    private TaskService taskService;
    @Autowired(required = false)
    private HistoryService historyService;
    @Autowired(required = false)
    private ManagementService managementService;
    @Autowired(required = false)
    private RepositoryService repositoryService;
    private final DcwsActTaskMapper actTaskMapper;
    private final IWfTaskBackNodeService wfTaskBackNodeService;
    private final IWfNodeConfigService wfNodeConfigService;
    private final DcwsFlowProcessEventHandler flowProcessEventHandler;
    private final UserService userService;
    private final OssService ossService;



    /**
     * 办理任务
     *
     * @param completeTaskBo 办理任务参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeTask(DcwsCompleteTaskBo completeTaskBo) {
        try {
            String userId = String.valueOf(LoginHelper.getUserId());
            Task task = WorkflowUtils.getTaskByCurrentUser(completeTaskBo.getTaskId());
            if (task == null) {
                throw new ServiceException(FlowConstant.MESSAGE_CURRENT_TASK_IS_NULL);
            }
            if (task.isSuspended()) {
                throw new ServiceException(FlowConstant.MESSAGE_SUSPENDED);
            }
            ProcessInstance processInstance = DcwsQueryUtils.instanceQuery(task.getProcessInstanceId()).singleResult();
            //办理委托任务
            if (ObjectUtil.isNotEmpty(task.getDelegationState()) && FlowConstant.PENDING.equals(task.getDelegationState().name())) {
                taskService.resolveTask(completeTaskBo.getTaskId());
                TaskEntity newTask = WorkflowUtils.createNewTask(task);
                taskService.addComment(newTask.getId(), task.getProcessInstanceId(), TaskStatusEnum.PASS.getStatus(), StringUtils.isNotBlank(completeTaskBo.getMessage()) ? completeTaskBo.getMessage() : StrUtil.EMPTY);
                taskService.complete(newTask.getId());
                return true;
            }
            //附件上传
            AttachmentCmd attachmentCmd = new AttachmentCmd(completeTaskBo.getFileId(), task.getId(), task.getProcessInstanceId(), ossService);
            managementService.executeCommand(attachmentCmd);
            String businessStatus = WorkflowUtils.getBusinessStatus(processInstance.getBusinessKey());
            //流程提交监听
            if (BusinessStatusEnum.DRAFT.getStatus().equals(businessStatus) || BusinessStatusEnum.BACK.getStatus().equals(businessStatus) || BusinessStatusEnum.CANCEL.getStatus().equals(businessStatus)) {
                flowProcessEventHandler.processHandler(processInstance.getProcessDefinitionKey(), processInstance.getBusinessKey(), businessStatus, true);
            }
            runtimeService.updateBusinessStatus(task.getProcessInstanceId(), BusinessStatusEnum.WAITING.getStatus());
            //办理监听
            flowProcessEventHandler.processTaskHandler(processInstance.getProcessDefinitionKey(), task.getTaskDefinitionKey(),
                task.getId(), processInstance.getBusinessKey(),completeTaskBo.getVariables());
            //办理意见
            taskService.addComment(completeTaskBo.getTaskId(), task.getProcessInstanceId(), TaskStatusEnum.PASS.getStatus(), StringUtils.isBlank(completeTaskBo.getMessage()) ? "同意" : completeTaskBo.getMessage());
            //办理任务
            taskService.setAssignee(task.getId(), userId);
            if (CollUtil.isNotEmpty(completeTaskBo.getVariables())) {
                taskService.complete(completeTaskBo.getTaskId(), completeTaskBo.getVariables());
            } else {
                taskService.complete(completeTaskBo.getTaskId());
            }
            //记录执行过的流程任务节点
            wfTaskBackNodeService.recordExecuteNode(task);
            ProcessInstance pi = DcwsQueryUtils.instanceQuery(task.getProcessInstanceId()).singleResult();
            if (pi == null) {
                UpdateBusinessStatusCmd updateBusinessStatusCmd = new UpdateBusinessStatusCmd(task.getProcessInstanceId(), BusinessStatusEnum.FINISH.getStatus());
                managementService.executeCommand(updateBusinessStatusCmd);
                flowProcessEventHandler.processHandler(processInstance.getProcessDefinitionKey(), processInstance.getBusinessKey(),
                    BusinessStatusEnum.FINISH.getStatus(), false);
            } else {
                List<Task> list = DcwsQueryUtils.taskQuery(task.getProcessInstanceId()).list();
                List<ProcessNode> nextNodeinfo = DcwsModelUtils.getNextNodeinfo((TaskEntity) task);//TODO yqh
                for (Task t : list) {
                    //办理监听
                    flowProcessEventHandler.processTaskHandler(processInstance.getProcessDefinitionKey(), t.getTaskDefinitionKey(),
                            t.getId(), processInstance.getBusinessKey(),completeTaskBo.getVariables());

                    if (ModelUtils.isUserTask(t.getProcessDefinitionId(), t.getTaskDefinitionKey())) {
                        List<HistoricIdentityLink> links = historyService.getHistoricIdentityLinksForTask(t.getId());
                        if (CollUtil.isEmpty(links) && StringUtils.isBlank(t.getAssignee())&& !CollectionUtil.isEmpty(nextNodeinfo)&&t.getTaskDefinitionKey().equals(nextNodeinfo.get(0).getNodeId())) {
//                        if (CollUtil.isEmpty(links) && StringUtils.isBlank(t.getAssignee())) {
//                            throw new ServiceException("下一节点【" + t.getName() + "】没有办理人!");
                            // 根据当前任务节点id获取办理人 TODO yqh
                            List<Long> assignees = new ArrayList<>();
                            String[] split = completeTaskBo.getAssignees().split(StringUtils.SEPARATOR);
                            for (String id : split) {
                                assignees.add(Long.valueOf(id));
                            }
                            if (CollectionUtil.isEmpty(assignees)) {
                                throw new ServiceException("【" + t.getName() + "】任务环节未配置审批人");
                            }
                            // 设置选人
                            if (assignees.size() == 1) {
                                taskService.setAssignee(t.getId(), assignees.get(0).toString());

                            } else {
                                // 多个作为候选人
                                for (Long assignee : assignees) {
                                    taskService.addCandidateUser(t.getId(), assignee.toString());
                                }
                            }
                            /*for (String candidateGroup: completeTaskBo.getCandidateGroups()) {
                                taskService.addCandidateGroup(t.getId(),candidateGroup);
                            }*/
                        }
                    }
                }

                if (CollUtil.isNotEmpty(list) && CollUtil.isNotEmpty(completeTaskBo.getWfCopyList())) {
                    TaskEntity newTask = WorkflowUtils.createNewTask(task);
                    taskService.addComment(newTask.getId(), task.getProcessInstanceId(), TaskStatusEnum.COPY.getStatus(), LoginHelper.getLoginUser().getNickname() + "【抄送】给" + String.join(",", StreamUtils.toList(completeTaskBo.getWfCopyList(), WfCopy::getUserName)));
                    taskService.complete(newTask.getId());
                    List<Task> taskList = DcwsQueryUtils.taskQuery(task.getProcessInstanceId()).list();
                    WorkflowUtils.createCopyTask(taskList, StreamUtils.toList(completeTaskBo.getWfCopyList(), WfCopy::getUserId));
                }
                sendMessage(list, processInstance.getName(), completeTaskBo.getMessageType(), null);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException(e.getMessage());
        }
    }


    /**
     * 发送消息
     *
     * @param list        任务
     * @param name        流程名称
     * @param messageType 消息类型
     * @param message     消息内容，为空则发送默认配置的消息内容
     */
    @Async
    public void sendMessage(List<Task> list, String name, List<String> messageType, String message) {
        WorkflowUtils.sendMessage(list, name, messageType, message, userService);
    }


    /**
     * 查询当前用户的待办任务
     *
     * @param taskBo 参数
     */
    @Override
    public TableDataInfo<DcwsTaskVo> getPageByTaskWait(DcwsTaskBo taskBo, PageQuery pageQuery) {
        QueryWrapper<DcwsTaskVo> queryWrapper = new QueryWrapper<>();
        List<RoleDTO> roles = LoginHelper.getLoginUser().getRoles();
        List<String> roleIds = StreamUtils.toList(roles, e -> String.valueOf(e.getRoleId()));
        String userId = String.valueOf(LoginHelper.getUserId());
        queryWrapper.eq("t.business_status_", BusinessStatusEnum.WAITING.getStatus());
        queryWrapper.eq(TenantHelper.isEnable(), "t.tenant_id_", TenantHelper.getTenantId());
        String ids = StreamUtils.join(roleIds, x -> "'" + x + "'");
        queryWrapper.and(w1 -> w1.eq("t.assignee_", userId).or(w2 -> w2.isNull("t.assignee_").apply("exists ( select LINK.ID_ from ACT_RU_IDENTITYLINK LINK where LINK.TASK_ID_ = t.ID_ and LINK.TYPE_ = 'candidate' and (LINK.USER_ID_ = {0} or ( LINK.GROUP_ID_ IN (" + ids + ") ) ))", userId)));
        if (StringUtils.isNotBlank(taskBo.getName())) {
            queryWrapper.like("t.name_", taskBo.getName());
        }
        if (StringUtils.isNotBlank(taskBo.getProcessDefinitionName())) {
            queryWrapper.like("t.processDefinitionName", taskBo.getProcessDefinitionName());
        }
        if (StringUtils.isNotBlank(taskBo.getProcessDefinitionKey())) {
            queryWrapper.eq("t.processDefinitionKey", taskBo.getProcessDefinitionKey());
        }
        if (StringUtils.isNotBlank(taskBo.getProcessDefinitionKey())) {
            queryWrapper.eq("t.BUSINESS_KEY_", taskBo.getBusinessKey());
        }
        if (!Objects.isNull(taskBo.getStartTime())) {
            queryWrapper.gt("t.CREATE_TIME_", DateUtils.dateTime(DateUtils.YYYY_MM_DD,taskBo.getStartTime()));
        }
        if (!Objects.isNull(taskBo.getEndTime())) {
            queryWrapper.lt("t.CREATE_TIME_", DateUtils.plusDays(DateUtils.dateTime(DateUtils.YYYY_MM_DD,taskBo.getEndTime()),1));
        }
        if (!Objects.isNull(taskBo.getWfType())) {
            queryWrapper.apply("t.BUSINESS_KEY_ in (select LINK.id from task_node_data LINK where LINK.apply_type = {0})",taskBo.getWfType());
        }
        queryWrapper.orderByDesc("t.CREATE_TIME_");
        Page<DcwsTaskVo> page = actTaskMapper.getTaskWaitByPage(pageQuery.build(), queryWrapper);

        List<DcwsTaskVo> taskList = page.getRecords();
        if (CollUtil.isNotEmpty(taskList)) {
            List<String> processDefinitionIds = StreamUtils.toList(taskList, DcwsTaskVo::getProcessDefinitionId);
            List<WfNodeConfigVo> wfNodeConfigVoList = wfNodeConfigService.selectByDefIds(processDefinitionIds);
            for (DcwsTaskVo task : taskList) {
                task.setBusinessStatusName(BusinessStatusEnum.findByStatus(task.getBusinessStatus()));
                task.setParticipantVo(WorkflowUtils.getCurrentTaskParticipant(task.getId(), userService));
                task.setMultiInstance(WorkflowUtils.isMultiInstance(task.getProcessDefinitionId(), task.getTaskDefinitionKey()) != null);
                if (CollUtil.isNotEmpty(wfNodeConfigVoList)) {
                    wfNodeConfigVoList.stream().filter(e -> e.getDefinitionId().equals(task.getProcessDefinitionId()) && FlowConstant.TRUE.equals(e.getApplyUserTask())).findFirst().ifPresent(task::setWfNodeConfigVo);
                    wfNodeConfigVoList.stream().filter(e -> e.getDefinitionId().equals(task.getProcessDefinitionId()) && e.getNodeId().equals(task.getTaskDefinitionKey()) && FlowConstant.FALSE.equals(e.getApplyUserTask())).findFirst().ifPresent(task::setWfNodeConfigVo);
                }
            }
        }
        return TableDataInfo.build(page);
    }



    /**
     * 查询当前用户的已办任务
     *
     * @param taskBo 参数
     */
    @Override
    public TableDataInfo<DcwsTaskVo> getPageByTaskFinish(DcwsTaskBo taskBo, PageQuery pageQuery) {
        String userId = String.valueOf(LoginHelper.getUserId());
        QueryWrapper<DcwsTaskVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(taskBo.getName()), "t.name_", taskBo.getName());
        queryWrapper.like(StringUtils.isNotBlank(taskBo.getProcessDefinitionName()), "t.processDefinitionName", taskBo.getProcessDefinitionName());
        queryWrapper.like(StringUtils.isNotBlank(taskBo.getBusinessKey()), "t.BUSINESS_KEY_", taskBo.getBusinessKey());
        queryWrapper.eq(StringUtils.isNotBlank(taskBo.getProcessDefinitionKey()), "t.processDefinitionKey", taskBo.getProcessDefinitionKey());
        queryWrapper.eq("t.assignee_", userId);
        if (!Objects.isNull(taskBo.getStartTime())) {
            queryWrapper.gt("t.START_TIME_", DateUtils.dateTime(DateUtils.YYYY_MM_DD,taskBo.getStartTime()));
        }
        if (!Objects.isNull(taskBo.getEndTime())) {
            queryWrapper.lt("t.START_TIME_", DateUtils.plusDays(DateUtils.dateTime(DateUtils.YYYY_MM_DD,taskBo.getEndTime()),1));
        }
        if (!Objects.isNull(taskBo.getWfType())) {
            queryWrapper.apply("t.BUSINESS_KEY_ in (select LINK.id from task_node_data LINK where LINK.apply_type = {0})",taskBo.getWfType());
        }
        queryWrapper.orderByDesc("t.START_TIME_");
        Page<DcwsTaskVo> page = actTaskMapper.getTaskFinishByPage(pageQuery.build(), queryWrapper);

        List<DcwsTaskVo> taskList = page.getRecords();
        if (CollUtil.isNotEmpty(taskList)) {
            List<String> processDefinitionIds = StreamUtils.toList(taskList, DcwsTaskVo::getProcessDefinitionId);
            List<WfNodeConfigVo> wfNodeConfigVoList = wfNodeConfigService.selectByDefIds(processDefinitionIds);
            for (DcwsTaskVo task : taskList) {
                task.setBusinessStatusName(BusinessStatusEnum.findByStatus(task.getBusinessStatus()));
                if (CollUtil.isNotEmpty(wfNodeConfigVoList)) {
                    wfNodeConfigVoList.stream().filter(e -> e.getDefinitionId().equals(task.getProcessDefinitionId()) && FlowConstant.TRUE.equals(e.getApplyUserTask())).findFirst().ifPresent(task::setWfNodeConfigVo);
                    wfNodeConfigVoList.stream().filter(e -> e.getDefinitionId().equals(task.getProcessDefinitionId()) && e.getNodeId().equals(task.getTaskDefinitionKey()) && FlowConstant.FALSE.equals(e.getApplyUserTask())).findFirst().ifPresent(task::setWfNodeConfigVo);
                }
            }
        }
        return TableDataInfo.build(page);
    }

    /**
     * 查询当前用户的抄送
     *
     * @param taskBo 参数
     */
    @Override
    public TableDataInfo<DcwsTaskVo> getPageByTaskCopy(DcwsTaskBo taskBo, PageQuery pageQuery) {
        QueryWrapper<DcwsTaskVo> queryWrapper = new QueryWrapper<>();
        String userId = String.valueOf(LoginHelper.getUserId());
        if (StringUtils.isNotBlank(taskBo.getName())) {
            queryWrapper.like("t.name_", taskBo.getName());
        }
        if (StringUtils.isNotBlank(taskBo.getProcessDefinitionName())) {
            queryWrapper.like("t.processDefinitionName", taskBo.getProcessDefinitionName());
        }
        if (StringUtils.isNotBlank(taskBo.getProcessDefinitionName())) {
            queryWrapper.like("t.BUSINESS_KEY_", taskBo.getBusinessKey());
        }
        if (StringUtils.isNotBlank(taskBo.getProcessDefinitionKey())) {
            queryWrapper.eq("t.processDefinitionKey", taskBo.getProcessDefinitionKey());
        }
        queryWrapper.eq("t.assignee_", userId);
        if (!Objects.isNull(taskBo.getStartTime())) {
            queryWrapper.gt("t.START_TIME_", DateUtils.dateTime(DateUtils.YYYY_MM_DD,taskBo.getStartTime()));
        }
        if (!Objects.isNull(taskBo.getEndTime())) {
            queryWrapper.lt("t.START_TIME_", DateUtils.plusDays(DateUtils.dateTime(DateUtils.YYYY_MM_DD,taskBo.getEndTime()),1));
        }
        queryWrapper.orderByDesc("t.START_TIME_");
        Page<DcwsTaskVo> page = actTaskMapper.getTaskCopyByPage(pageQuery.build(), queryWrapper);

        List<DcwsTaskVo> taskList = page.getRecords();
        if (CollUtil.isNotEmpty(taskList)) {
            List<String> processDefinitionIds = StreamUtils.toList(taskList, DcwsTaskVo::getProcessDefinitionId);
            List<WfNodeConfigVo> wfNodeConfigVoList = wfNodeConfigService.selectByDefIds(processDefinitionIds);
            for (DcwsTaskVo task : taskList) {
                task.setBusinessStatusName(BusinessStatusEnum.findByStatus(task.getBusinessStatus()));
                if (CollUtil.isNotEmpty(wfNodeConfigVoList)) {
                    wfNodeConfigVoList.stream().filter(e -> e.getDefinitionId().equals(task.getProcessDefinitionId()) && FlowConstant.TRUE.equals(e.getApplyUserTask())).findFirst().ifPresent(task::setWfNodeConfigVo);
                    wfNodeConfigVoList.stream().filter(e -> e.getDefinitionId().equals(task.getProcessDefinitionId()) && e.getNodeId().equals(task.getTaskDefinitionKey()) && FlowConstant.FALSE.equals(e.getApplyUserTask())).findFirst().ifPresent(task::setWfNodeConfigVo);
                }
            }
        }
        return TableDataInfo.build(page);
    }




    /**
     * @description: 获取目标节点（下一个节点） add by yqh
     * @param: nextNodeBo
     * @return: java.util.Map<java.lang.String, java.lang.Object>
     */
    @Override
    public Map<String, Object> getNextNodeInfo(NextNodeBo nextNodeBo) {
        Map<String, Object> map = new HashMap<>(16);
        TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(nextNodeBo.getTaskId()).singleResult();
        if (task.isSuspended()) {
            throw new ServiceException(FlowConstant.MESSAGE_SUSPENDED);
        }

        if (CollectionUtil.isNotEmpty(nextNodeBo.getVariables())) {
            taskService.setVariables(task.getId(), nextNodeBo.getVariables());
        }
        //流程定义
        String processDefinitionId = task.getProcessDefinitionId();
        //查询bpmn信息
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        //通过任务节点id，来获取当前节点信息
        FlowElement flowElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        //全部节点
        Collection<FlowElement> flowElements = bpmnModel.getProcesses().get(0).getFlowElements();
        //封装下一个用户任务节点信息
        List<ProcessNode> nextNodeList = new ArrayList<>();
        //保存没有表达式的节点
        List<ProcessNode> tempNodeList = new ArrayList<>();
        ExecutionEntityImpl executionEntity = (ExecutionEntityImpl) runtimeService.createExecutionQuery()
                .executionId(task.getExecutionId()).singleResult();
        DcwsWorkflowUtils.getNextNodeList(flowElements, flowElement, executionEntity, nextNodeList, tempNodeList, task.getId(), null);
        if (CollectionUtil.isNotEmpty(nextNodeList)) {
            nextNodeList.removeIf(node -> !node.getExpression());
        }
        if (CollectionUtil.isNotEmpty(nextNodeList) && CollectionUtil.isNotEmpty(nextNodeList.stream().filter(e -> e.getExpression() != null && e.getExpression()).collect(Collectors.toList()))) {
            List<ProcessNode> nodeList = nextNodeList.stream().filter(e -> e.getExpression() != null && e.getExpression()).collect(Collectors.toList());
            List<ProcessNode> processNodeList = getProcessNodeAssigneeList(nodeList, task.getProcessDefinitionId());
            map.put("list", processNodeList);
        } else if (CollectionUtil.isNotEmpty(tempNodeList)) {
            List<ProcessNode> processNodeList = getProcessNodeAssigneeList(tempNodeList, task.getProcessDefinitionId());
            map.put("list", processNodeList);
        } else {
            map.put("list", nextNodeList);
        }
        map.put("processInstanceId", task.getProcessInstanceId());
        return map;
    }

    /**
     * @description: 设置节点审批人员  add by yqh
     * @param: nodeList节点列表
     * @param: definitionId 流程定义id
     * @return: java.util.List<com.ruoyi.workflow.domain.vo.ProcessNode>
     */
    private List<ProcessNode> getProcessNodeAssigneeList(List<ProcessNode> nodeList, String definitionId) {
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
