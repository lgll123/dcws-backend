package com.formssi.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.workflow.domain.DcwsNormalTask;
import com.formssi.workflow.mapper.DcwsNormalTaskMapper;
import com.formssi.workflow.utils.DcwsDateUtils;
import com.formssi.workflow.mapper.DcwsActTaskMapper;
import com.formssi.workflow.service.IWfNodeConfigService;
import com.formssi.workflow.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.formssi.common.core.domain.dto.RoleDTO;
import com.formssi.common.core.service.UserService;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.common.tenant.helper.TenantHelper;
import com.formssi.workflow.common.constant.FlowConstant;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.workflow.domain.bo.*;
import com.formssi.workflow.domain.vo.*;
import com.formssi.workflow.service.DcwsIActTaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 任务 服务层实现
 *
 * @author may
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DcwsActTaskServiceImpl implements DcwsIActTaskService {

    private final DcwsActTaskMapper actTaskMapper;
    private final IWfNodeConfigService wfNodeConfigService;
    private final UserService userService;
    private final DcwsNormalTaskMapper dcwsNormalTaskMapper;


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
        if (StringUtils.isNotBlank(taskBo.getBusinessKey())) {
            queryWrapper.eq("t.BUSINESS_KEY_", taskBo.getBusinessKey());
        }
        if (!Objects.isNull(taskBo.getStartTime())) {
            queryWrapper.gt("t.CREATE_TIME_", DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,taskBo.getStartTime()));
        }
        if (!Objects.isNull(taskBo.getEndTime())) {
            queryWrapper.lt("t.CREATE_TIME_", DcwsDateUtils.plusDays(DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,taskBo.getEndTime()),1));
        }
        if (!Objects.isNull(taskBo.getWfType())) {
            queryWrapper.apply("t.BUSINESS_KEY_ in (select LINK.id from dcws_task_node_data LINK where LINK.apply_type = {0})",taskBo.getWfType());
        }
        if (!Objects.isNull(taskBo.getCompanyId())) {
            queryWrapper.apply("t.BUSINESS_KEY_ in (select LINK.id from dcws_task_node_data LINK where LINK.company_Id = {0})",taskBo.getCompanyId());
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
            queryWrapper.gt("t.START_TIME_", DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,taskBo.getStartTime()));
        }
        if (!Objects.isNull(taskBo.getEndTime())) {
            queryWrapper.lt("t.START_TIME_", DcwsDateUtils.plusDays(DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,taskBo.getEndTime()),1));
        }
        if (!Objects.isNull(taskBo.getWfType())) {
            queryWrapper.apply("t.BUSINESS_KEY_ in (select LINK.id from dcws_task_node_data LINK where LINK.apply_type = {0})",taskBo.getWfType());
        }
        if (!Objects.isNull(taskBo.getCompanyId())) {
            queryWrapper.apply("t.BUSINESS_KEY_ in (select LINK.id from dcws_task_node_data LINK where LINK.company_Id = {0})",taskBo.getCompanyId());
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
            queryWrapper.gt("t.START_TIME_", DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,taskBo.getStartTime()));
        }
        if (!Objects.isNull(taskBo.getEndTime())) {
            queryWrapper.lt("t.START_TIME_", DcwsDateUtils.plusDays(DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,taskBo.getEndTime()),1));
        }
        if (!Objects.isNull(taskBo.getCompanyId())) {
            queryWrapper.apply("t.BUSINESS_KEY_ in (select LINK.id from dcws_task_node_data LINK where LINK.company_Id = {0})",taskBo.getCompanyId());
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

    @Override
    public DcwsTaskCountVo getUserTaskCount() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageSize(1);
        pageQuery.setPageNum(1);
        DcwsTaskCountVo dcwsTaskCountVo = new DcwsTaskCountVo();
        QueryWrapper<DcwsTaskVo> taskQueryWrapper = new QueryWrapper<>();
        List<RoleDTO> roles = LoginHelper.getLoginUser().getRoles();
        List<String> roleIds = StreamUtils.toList(roles, e -> String.valueOf(e.getRoleId()));
        String userId = String.valueOf(LoginHelper.getUserId());
        taskQueryWrapper.eq("t.business_status_", BusinessStatusEnum.WAITING.getStatus());
        taskQueryWrapper.eq(TenantHelper.isEnable(), "t.tenant_id_", TenantHelper.getTenantId());
        String ids = StreamUtils.join(roleIds, x -> "'" + x + "'");
        taskQueryWrapper.and(w1 -> w1.eq("t.assignee_", userId).or(w2 -> w2.isNull("t.assignee_").apply("exists ( select LINK.ID_ from ACT_RU_IDENTITYLINK LINK where LINK.TASK_ID_ = t.ID_ and LINK.TYPE_ = 'candidate' and (LINK.USER_ID_ = {0} or ( LINK.GROUP_ID_ IN (" + ids + ") ) ))", userId)));
        Page<DcwsTaskVo> taskWaitPage = actTaskMapper.getTaskWaitByPage(pageQuery.build(), taskQueryWrapper);
        QueryWrapper<DcwsNormalTaskVo> normalTaskqueryWrapper = new QueryWrapper<>();
        normalTaskqueryWrapper.eq("t.status", BusinessStatusEnum.WAITING.getStatus());
        normalTaskqueryWrapper.eq("t.user_Id", LoginHelper.getUserId());
        Page<DcwsNormalTaskVo> normalTaskPage = dcwsNormalTaskMapper.getTaskWaitByPage(pageQuery.build(), normalTaskqueryWrapper);
        //待办总数
        dcwsTaskCountVo.setTaskWaitingCount(taskWaitPage.getTotal() + normalTaskPage.getTotal());

        HistoricProcessInstanceQuery query = DcwsQueryUtils.hisInstanceQuery();
        query.startedBy(String.valueOf(LoginHelper.getUserId()));
        query.listPage(pageQuery.getFirstNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<DcwsNormalTask> lqw = Wrappers.lambdaQuery();
        lqw.eq(DcwsNormalTask::getCreateBy, LoginHelper.getUserId());
        Page<DcwsNormalTaskVo> result = dcwsNormalTaskMapper.selectVoPage(pageQuery.build(), lqw);
        //发起总数
        dcwsTaskCountVo.setMyDocumentCount(query.count() + result.getTotal());

        taskQueryWrapper = new QueryWrapper<>();
        taskQueryWrapper.eq("t.assignee_", String.valueOf(LoginHelper.getUserId()));
        Page<DcwsTaskVo> taskFinishPage = actTaskMapper.getTaskFinishByPage(pageQuery.build(), taskQueryWrapper);
        normalTaskqueryWrapper = new QueryWrapper<>();
        normalTaskqueryWrapper.eq("t.user_Id", LoginHelper.getUserId());
        normalTaskPage = dcwsNormalTaskMapper.getTaskFinishByPage(pageQuery.build(), normalTaskqueryWrapper);
        //已办总数
        dcwsTaskCountVo.setTaskFinishCount(taskFinishPage.getTotal() + normalTaskPage.getTotal());

        taskQueryWrapper = new QueryWrapper<>();
        taskQueryWrapper.eq("t.assignee_", String.valueOf(LoginHelper.getUserId()));
        Page<DcwsTaskVo> taskCopyPage = actTaskMapper.getTaskCopyByPage(pageQuery.build(), taskQueryWrapper);
        //抄送总数
        dcwsTaskCountVo.setTaskCopyListCount(taskCopyPage.getTotal());

        log.info("待办汇总:\n" + JSON.toJSONString(dcwsTaskCountVo) + "\n");
        return dcwsTaskCountVo;
    }
}
