package com.formssi.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.satoken.utils.LoginHelper;
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
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class ProjectManagementServiceImpl implements ProjectManagementService {

    private final DcwsProjectMapper projectMapper;
    private final DcwsProjectTaskRefMapper dcwsProjectTaskRefMapper;
    private final DcwsProjectTaskMapper dcwsProjectTaskMapper;

    /**
     * 查询非标准流程
     */
    @Override
    public DcwsProjectVo queryById(Long id) {
        DcwsProjectVo dcwsProjectVo = projectMapper.selectVoById(id);
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
        return projectMapper.selectVoList(lqw);
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
        //新增项目表
        boolean flag = projectMapper.insert(dcwsProject) > 0;
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
        projectMapper.updateById(update);
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
        dcwsProjectTaskMapper.updateById(update);
        return MapstructUtils.convert(update, DcwsProjectTaskVo.class);
    }
}
