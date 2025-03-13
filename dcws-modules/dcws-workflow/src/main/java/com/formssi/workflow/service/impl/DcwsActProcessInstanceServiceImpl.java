package com.formssi.workflow.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.formssi.common.core.service.UserService;
import com.formssi.workflow.utils.DcwsDateUtils;
import com.formssi.workflow.common.enums.TaskStatusEnum;
import com.formssi.workflow.domain.TaskNodeData;
import com.formssi.workflow.mapper.TaskNodeDataMapper;
import com.formssi.workflow.service.DcwsIActProcessInstanceService;
import com.formssi.workflow.service.IWfNodeConfigService;
import com.formssi.workflow.utils.QueryUtils;
import com.formssi.workflow.utils.WorkflowUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.workflow.common.constant.FlowConstant;
import com.formssi.workflow.domain.bo.DcwsProcessInstanceBo;
import com.formssi.workflow.domain.vo.*;
import com.formssi.workflow.utils.DcwsQueryUtils;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.task.Attachment;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.*;

/**
 * 流程实例 服务层实现
 *
 * @author may
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DcwsActProcessInstanceServiceImpl implements DcwsIActProcessInstanceService {

    private final IWfNodeConfigService wfNodeConfigService;
    private final TaskNodeDataMapper taskNodeDataMapper;
    @Autowired(required = false)
    private TaskService taskService;
    private final UserService userService;

    /**
     * 获取审批记录
     *
     * @param businessKey 业务id
     */
    @Override
    public List<DcwsActHistoryInfoVo> getHistoryRecord(String businessKey) {
        // 查询任务办理记录
        List<HistoricTaskInstance> list = QueryUtils.hisTaskBusinessKeyQuery(businessKey).orderByHistoricTaskInstanceEndTime().desc().list();
        list = StreamUtils.sorted(list, Comparator.comparing(HistoricTaskInstance::getEndTime, Comparator.nullsFirst(Date::compareTo)).reversed());
        HistoricProcessInstance historicProcessInstance = QueryUtils.hisBusinessKeyQuery(businessKey).singleResult();
        String processInstanceId = historicProcessInstance.getId();
        List<DcwsActHistoryInfoVo> actHistoryInfoVoList = new ArrayList<>();
        List<Comment> processInstanceComments = taskService.getProcessInstanceComments(processInstanceId);
        //附件
        List<Attachment> attachmentList = taskService.getProcessInstanceAttachments(processInstanceId);
        for (HistoricTaskInstance historicTaskInstance : list) {
            DcwsActHistoryInfoVo actHistoryInfoVo = new DcwsActHistoryInfoVo();
            BeanUtils.copyProperties(historicTaskInstance, actHistoryInfoVo);
            if (actHistoryInfoVo.getEndTime() == null) {
                actHistoryInfoVo.setStatus(TaskStatusEnum.WAITING.getStatus());
                actHistoryInfoVo.setStatusName(TaskStatusEnum.WAITING.getDesc());
            }
            if (CollUtil.isNotEmpty(processInstanceComments)) {
                processInstanceComments.stream().filter(e -> e.getTaskId().equals(historicTaskInstance.getId())).findFirst().ifPresent(e -> {
                    actHistoryInfoVo.setComment(e.getFullMessage());
                    actHistoryInfoVo.setStatus(e.getType());
                    actHistoryInfoVo.setStatusName(TaskStatusEnum.findByStatus(e.getType()));
                });
            }
            if (ObjectUtil.isNotEmpty(historicTaskInstance.getDurationInMillis())) {
                actHistoryInfoVo.setRunDuration(getDuration(historicTaskInstance.getDurationInMillis()));
            }
            //附件
            if (CollUtil.isNotEmpty(attachmentList)) {
                List<Attachment> attachments = StreamUtils.filter(attachmentList, e -> e.getTaskId().equals(historicTaskInstance.getId()));
                if (CollUtil.isNotEmpty(attachments)) {
                    actHistoryInfoVo.setAttachmentList(attachments);
                }
            }
            //设置人员id
            if (ObjectUtil.isEmpty(historicTaskInstance.getAssignee())) {
                ParticipantVo participantVo = WorkflowUtils.getCurrentTaskParticipant(historicTaskInstance.getId(), userService);
                if (ObjectUtil.isNotEmpty(participantVo) && CollUtil.isNotEmpty(participantVo.getCandidate())) {
                    actHistoryInfoVo.setAssignee(StreamUtils.join(participantVo.getCandidate(), Convert::toStr));
                }
            }
            actHistoryInfoVoList.add(actHistoryInfoVo);
        }
        // 审批记录
        Map<String, List<DcwsActHistoryInfoVo>> groupByKey = StreamUtils.groupByKey(actHistoryInfoVoList, DcwsActHistoryInfoVo::getTaskDefinitionKey);
        for (Map.Entry<String, List<DcwsActHistoryInfoVo>> entry : groupByKey.entrySet()) {
            DcwsActHistoryInfoVo actHistoryInfoVo = BeanUtil.toBean(entry.getValue().get(0), DcwsActHistoryInfoVo.class);
            actHistoryInfoVoList.stream().filter(e -> e.getTaskDefinitionKey().equals(entry.getKey()) && e.getEndTime() != null).findFirst()
                    .ifPresent(e -> {
                        actHistoryInfoVo.setStatus("已处理");
                        actHistoryInfoVo.setStartTime(e.getStartTime());
                    });
            actHistoryInfoVoList.stream().filter(e -> e.getTaskDefinitionKey().equals(entry.getKey()) && e.getEndTime() == null).findFirst()
                    .ifPresent(e -> {
                        actHistoryInfoVo.setStatus("待处理");
                        actHistoryInfoVo.setStartTime(e.getStartTime());
                        actHistoryInfoVo.setEndTime(null);
                        actHistoryInfoVo.setRunDuration(null);
                    });
        }
        List<DcwsActHistoryInfoVo> recordList = new ArrayList<>();
        // 待办理
        recordList.addAll(StreamUtils.filter(actHistoryInfoVoList, e -> e.getEndTime() == null));
        // 已办理
        recordList.addAll(StreamUtils.filter(actHistoryInfoVoList, e -> e.getEndTime() != null));

        return recordList;
    }



    /**
     * 分页查询当前登录人单据
     *
     * @param bo 参数
     */
    @Override
    public TableDataInfo<DcwsProcessInstanceVo> getPageByCurrent(DcwsProcessInstanceBo bo, PageQuery pageQuery) {
        List<DcwsProcessInstanceVo> list = new ArrayList<>();
        HistoricProcessInstanceQuery query = DcwsQueryUtils.hisInstanceQuery();
        query.startedBy(String.valueOf(LoginHelper.getUserId()));
        if (StringUtils.isNotBlank(bo.getName())) {
            query.processInstanceNameLikeIgnoreCase("%" + bo.getName() + "%");
        }
        if (StringUtils.isNotBlank(bo.getKey())) {
            query.processDefinitionKey(bo.getKey());
        }
        if (StringUtils.isNotBlank(bo.getBusinessKey())) {
            query.processInstanceBusinessKey(bo.getBusinessKey());
        }
        if (StringUtils.isNotBlank(bo.getName())) {
            query.processDefinitionName(bo.getName());
        }
        if (StringUtils.isNotBlank(bo.getCategoryCode())) {
            query.processDefinitionCategory(bo.getCategoryCode());
        }
        if (!Objects.isNull(bo.getStartTime())) {
            query.startedAfter(DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,bo.getStartTime()));
        }
        if (!Objects.isNull(bo.getEndTime())) {
            query.startedBefore(DcwsDateUtils.plusDays(DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,bo.getEndTime()),1));
        }
        if (!Objects.isNull(bo.getWfType())) {
            query.processDefinitionCategory(bo.getWfType());
        }
        query.orderByProcessInstanceStartTime().desc();
        List<HistoricProcessInstance> historicProcessInstanceList = query.listPage(pageQuery.getFirstNum(), pageQuery.getPageSize());
        List<DcwsTaskVo> taskVoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(historicProcessInstanceList)) {
            List<String> processInstanceIds = StreamUtils.toList(historicProcessInstanceList, HistoricProcessInstance::getId);
            List<Task> taskList = DcwsQueryUtils.taskQuery(processInstanceIds).list();
            for (Task task : taskList) {
                taskVoList.add(BeanUtil.toBean(task, DcwsTaskVo.class));
            }
        }
        for (HistoricProcessInstance processInstance : historicProcessInstanceList) {
            DcwsProcessInstanceVo processInstanceVo = BeanUtil.toBean(processInstance, DcwsProcessInstanceVo.class);
            processInstanceVo.setBusinessStatusName(BusinessStatusEnum.findByStatus(processInstance.getBusinessStatus()));
            if (CollUtil.isNotEmpty(taskVoList)) {
                List<DcwsTaskVo> collect = StreamUtils.filter(taskVoList, e -> e.getProcessInstanceId().equals(processInstance.getId()));
                processInstanceVo.setTaskVoList(CollUtil.isNotEmpty(collect) ? collect : Collections.emptyList());
            }
            list.add(processInstanceVo);
        }
        if (CollUtil.isNotEmpty(list)) {
            List<String> processDefinitionIds = StreamUtils.toList(list, DcwsProcessInstanceVo::getProcessDefinitionId);
            List<WfNodeConfigVo> wfNodeConfigVoList = wfNodeConfigService.selectByDefIds(processDefinitionIds);
            List<String> ids = new ArrayList<>();
            for (DcwsProcessInstanceVo processInstanceVo : list) {
                if (CollUtil.isNotEmpty(wfNodeConfigVoList)) {
                    wfNodeConfigVoList.stream().filter(e -> e.getDefinitionId().equals(processInstanceVo.getProcessDefinitionId()) && FlowConstant.TRUE.equals(e.getApplyUserTask())).findFirst().ifPresent(processInstanceVo::setWfNodeConfigVo);
                }
                ids.add(processInstanceVo.getBusinessKey());
            }
            LambdaQueryWrapper<TaskNodeData> lqw = Wrappers.lambdaQuery();
            lqw.in(TaskNodeData::getId,ids);
            List<TaskNodeDataVo> taskNodeDataVoList = taskNodeDataMapper.selectVoList(lqw);
            for (TaskNodeDataVo taskNodeDataVo : taskNodeDataVoList){
                for(DcwsProcessInstanceVo processInstanceVo : list){
                    if(taskNodeDataVo.getId().equals(processInstanceVo.getBusinessKey())){
                        processInstanceVo.setApplyReason(taskNodeDataVo.getApplyReson());
                    }
                }
            }
        }
        long count = query.count();
        TableDataInfo<DcwsProcessInstanceVo> build = TableDataInfo.build();
        build.setRows(list);
        build.setTotal(count);
        return build;
    }

    /**
     * 任务完成时间处理
     *
     * @param time 时间
     */
    private String getDuration(long time) {

        long day = time / (24 * 60 * 60 * 1000);
        long hour = (time / (60 * 60 * 1000) - day * 24);
        long minute = ((time / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long second = (time / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60);

        if (day > 0) {
            return day + "天" + hour + "小时" + minute + "分钟";
        }
        if (hour > 0) {
            return hour + "小时" + minute + "分钟";
        }
        if (minute > 0) {
            return minute + "分钟";
        }
        if (second > 0) {
            return second + "秒";
        } else {
            return 0 + "秒";
        }
    }
}
