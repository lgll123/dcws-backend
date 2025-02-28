package com.formssi.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.domain.dto.RoleDTO;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.common.tenant.helper.TenantHelper;
import com.formssi.workflow.domain.*;
import com.formssi.workflow.domain.bo.DcwsProjectBo;
import com.formssi.workflow.domain.bo.DcwsProjectTaskBo;
import com.formssi.workflow.domain.vo.*;
import com.formssi.workflow.mapper.*;
import com.formssi.workflow.service.ProjectManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectManagementServiceImpl implements ProjectManagementService {

    private final DcwsProjectMapper dcwsProjectMapper;
    private final DcwsProjectTaskRefMapper dcwsProjectTaskRefMapper;
    private final DcwsProjectTaskMapper dcwsProjectTaskMapper;
    private final ActTaskMapper actTaskMapper;
    private final DcwsNormalTaskMapper dcwsNormalTaskMapper;

    /**
     * 查询非标准流程
     */
    @Override
    public DcwsProjectVo queryById(Long id) {
        DcwsProjectVo dcwsProjectVo = dcwsProjectMapper.selectVoById(id);
        return dcwsProjectVo;
    }

    /**
     * 查询项目列表
     */
    @Override
    public List<DcwsProjectVo> queryProjectList(DcwsProjectBo bo) {
        LambdaQueryWrapper<DcwsProject> lqw = Wrappers.lambdaQuery();
        lqw.eq(DcwsProject::getCreateBy, LoginHelper.getUserId());
        if(!Objects.isNull(bo.getProjectStatus())){
            lqw.eq(DcwsProject::getProjectStatus, bo.getProjectStatus());
        }
        lqw.orderByDesc(BaseEntity::getCreateTime);
        return dcwsProjectMapper.selectVoList(lqw);
    }

    /**
     * 查询项目下任务列表
     */
    @Override
    public List<DcwsProjectTaskVo> queryProjectTaskList(DcwsProjectTaskBo bo) {
        LambdaQueryWrapper<DcwsProjectTask> lqw = Wrappers.lambdaQuery();
        lqw.eq(DcwsProjectTask::getProjectId, bo.getProjectId());
        lqw.orderByDesc(BaseEntity::getCreateTime);
        return dcwsProjectTaskMapper.selectVoList(lqw);
    }

    /**
     * 新增项目
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DcwsProjectVo insertByBo(DcwsProjectBo bo) {
        DcwsProject dcwsProject = MapstructUtils.convert(bo, DcwsProject.class);
        dcwsProject.setProjectStatus(BusinessStatusEnum.WAITING.getStatus());
        dcwsProject.setProjectLeader(LoginHelper.getUsername());
        dcwsProject.setCreateBy(LoginHelper.getUserId());
        dcwsProject.setCreateTime(new Date());
        //新增项目表
        boolean flag = dcwsProjectMapper.insert(dcwsProject) > 0;
        if (flag) {
            LambdaQueryWrapper<DcwsProjectTaskRef> lqw = Wrappers.lambdaQuery();
            lqw.eq(DcwsProjectTaskRef::getProjectType, bo.getProjectType());
            lqw.eq(DcwsProjectTaskRef::getIsOpen, "Y");
            lqw.orderByDesc(DcwsProjectTaskRef::getCreateTime);
            List<DcwsProjectTaskRefVo> list = dcwsProjectTaskRefMapper.selectVoList(lqw);
            if (!Objects.isNull(list)){
                for (DcwsProjectTaskRefVo dcwsProjectTaskRefVo : list){
                    DcwsProjectTask dcwsProjectTask = new DcwsProjectTask();
                    dcwsProjectTask.setTaskType(dcwsProjectTaskRefVo.getTaskType());
                    dcwsProjectTask.setProjectId(dcwsProject.getProjectId());
                    dcwsProjectTask.setTaskName(dcwsProjectTaskRefVo.getTaskName());
                    dcwsProjectTask.setTaskStatus(BusinessStatusEnum.DRAFT.getStatus());
                    dcwsProjectTask.setTaskRouteUrl(dcwsProjectTaskRefVo.getTaskRouteUrl());
                    dcwsProjectTask.setCreateBy(LoginHelper.getUserId());
                    dcwsProjectTask.setCreateTime(new Date());
                    dcwsProjectTask.setCreateEmpName(LoginHelper.getUsername());
                    dcwsProjectTaskMapper.insert(dcwsProjectTask);
                }
            }
        }
        return MapstructUtils.convert(dcwsProject, DcwsProjectVo.class);
    }

    /**
     * 修改项目
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DcwsProjectVo updateByBo(DcwsProjectBo bo) {
        DcwsProject update = MapstructUtils.convert(bo, DcwsProject.class);
        dcwsProjectMapper.updateById(update);
        //修改项目为已完成时判断项目下所有任务是否已完成
        if (BusinessStatusEnum.FINISH.getStatus().equals(update.getProjectStatus())){
            LambdaQueryWrapper<DcwsProjectTask> lqw = Wrappers.lambdaQuery();
            lqw.eq(DcwsProjectTask::getProjectId, bo.getProjectId());
            List<DcwsProjectTaskVo> list = dcwsProjectTaskMapper.selectVoList(lqw);
            for (DcwsProjectTaskVo dcwsProjectTaskVo : list){
                if (BusinessStatusEnum.DRAFT.getStatus().equals(dcwsProjectTaskVo.getTaskStatus())||BusinessStatusEnum.INPROGRESS.getStatus().equals(dcwsProjectTaskVo.getTaskStatus())){
                    throw new ServiceException("该项目下还有任务未完成");
                }
            }
        }
        return MapstructUtils.convert(update, DcwsProjectVo.class);
    }

    @Override
    public DcwsProjectTaskVo updateByTaskBo(DcwsProjectTaskBo bo) {
        DcwsProjectTask update = MapstructUtils.convert(bo, DcwsProjectTask.class);
        if (StringUtils.isNotEmpty(update.getBusinessKey())){
            update.setTaskId(null);
        }
        dcwsProjectTaskMapper.updateById(update);
        return MapstructUtils.convert(update, DcwsProjectTaskVo.class);
    }

    @Override
    public TaskVo querytaskbelonging(DcwsProjectTaskBo bo) {
        if (!"20".equals(bo.getTaskType())){
            QueryWrapper<TaskVo> queryWrapper = new QueryWrapper<>();
            List<RoleDTO> roles = LoginHelper.getLoginUser().getRoles();
            List<String> roleIds = StreamUtils.toList(roles, e -> String.valueOf(e.getRoleId()));
            String userId = String.valueOf(LoginHelper.getUserId());
            queryWrapper.eq("t.business_status_", BusinessStatusEnum.WAITING.getStatus());
            queryWrapper.eq(TenantHelper.isEnable(), "t.tenant_id_", TenantHelper.getTenantId());
            String ids = StreamUtils.join(roleIds, x -> "'" + x + "'");
            queryWrapper.and(w1 -> w1.eq("t.assignee_", userId).or(w2 -> w2.isNull("t.assignee_").apply("exists ( select LINK.ID_ from ACT_RU_IDENTITYLINK LINK where LINK.TASK_ID_ = t.ID_ and LINK.TYPE_ = 'candidate' and (LINK.USER_ID_ = {0} or ( LINK.GROUP_ID_ IN (" + ids + ") ) ))", userId)));
            queryWrapper.eq("t.BUSINESS_KEY_", bo.getBusinessKey());
            PageQuery pageQuery = new PageQuery();
            pageQuery.setPageNum(1);
            pageQuery.setPageSize(1);
            Page<TaskVo> page = actTaskMapper.getTaskWaitByPage(pageQuery.build(), queryWrapper);
            List<TaskVo> taskList = page.getRecords();
            if (CollUtil.isNotEmpty(taskList)) {
                TaskVo taskVo = taskList.get(0);
                taskVo.setTaskBelonging("true");
                return taskVo;
            }
        }else{
            QueryWrapper<DcwsNormalTaskVo> wrapper = new QueryWrapper<>();
            wrapper.eq("t.status", BusinessStatusEnum.WAITING.getStatus());
            wrapper.eq("t.user_Id", LoginHelper.getUserId());
            wrapper.eq("t.task_id", bo.getTaskId());
            PageQuery pageQuery = new PageQuery();
            pageQuery.setPageNum(1);
            pageQuery.setPageSize(1);
            Page<DcwsNormalTaskVo> pageTemp = dcwsNormalTaskMapper.getTaskWaitByPage(pageQuery.build(), wrapper);
            List<DcwsNormalTaskVo> taskTempList = pageTemp.getRecords();
            if (CollUtil.isNotEmpty(taskTempList)) {
                TaskVo taskVo = new TaskVo();
                taskVo.setTaskBelonging("true");
                return taskVo;
            }
        }
        TaskVo taskVo = new TaskVo();
        taskVo.setTaskBelonging("false");
        return taskVo;
    }
}
