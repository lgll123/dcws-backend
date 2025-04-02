package com.formssi.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.system.domain.vo.SysDeptVo;
import com.formssi.workflow.domain.bo.DcwsProjectTaskBo;
import com.formssi.workflow.service.ProjectManagementService;
import com.formssi.workflow.utils.DcwsDateUtils;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.system.domain.vo.SysUserVo;
import com.formssi.system.service.ISysUserService;
import com.formssi.workflow.domain.*;
import com.formssi.workflow.domain.bo.DcwsNormalTaskBo;
import com.formssi.workflow.domain.vo.*;
import com.formssi.workflow.mapper.*;
import com.formssi.workflow.service.NormalTaskService;
import com.formssi.workflow.service.TaskSerialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class NormalTaskServiceImpl implements NormalTaskService {

    private final DcwsNormalTaskMapper dcwsNormalTaskMapper;
    private final DcwsNormalTaskHandleHisMapper dcwsHisMapper;
    private final DcwsNormalTaskUserMapper dcwsUserMapper;
    private final ISysUserService iSysUserService;
    private final DcwsProjectTaskMapper dcwsProjectTaskMapper;
    private final TaskSerialService taskSerialService;
    private final WfCategoryMapper wfCategoryMapper;
    private final ProjectManagementService projectService;


    /**
     * 查询非标准流程
     */
    @Override
    public DcwsNormalTaskVo queryById(String id) {
        DcwsNormalTaskVo dcwsApproveVo = dcwsNormalTaskMapper.selectVoById(id);
        LambdaQueryWrapper<DcwsNormalTaskUser> lqw = Wrappers.lambdaQuery();
        lqw.eq(DcwsNormalTaskUser::getTaskId, dcwsApproveVo.getTaskId());
        List<DcwsNormalTaskUserVo> list = dcwsUserMapper.selectVoList(lqw);
        dcwsApproveVo.setDcwsUserVoList(list);
        return dcwsApproveVo;
    }

    /**
     * 查询非标准流程列表
     */
    @Override
    public TableDataInfo<DcwsNormalTaskVo> queryPageList(DcwsNormalTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DcwsNormalTask> lqw = buildQueryWrapper(bo);
        if (bo.getTaskId() != null){
            lqw.eq(DcwsNormalTask::getTaskId, bo.getTaskId());
        }
        lqw.eq(DcwsNormalTask::getCreateBy, LoginHelper.getUserId());
        if (!Objects.isNull(bo.getStartTime())) {
            lqw.gt(DcwsNormalTask::getCreateTime,DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,bo.getStartTime()));
        }
        if (!Objects.isNull(bo.getEndTime())) {
            lqw.lt(DcwsNormalTask::getCreateTime,DcwsDateUtils.plusDays(DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,bo.getEndTime()),1));
        }
        lqw.orderByDesc(DcwsNormalTask::getCreateTime);
        Page<DcwsNormalTaskVo> result = dcwsNormalTaskMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public TableDataInfo<DcwsNormalTaskVo> getPageByTaskWait(DcwsNormalTaskBo dcwsNormalTaskBo, PageQuery pageQuery) {
        QueryWrapper<DcwsNormalTaskVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t.status", BusinessStatusEnum.WAITING.getStatus());
        queryWrapper.eq("t.user_Id", LoginHelper.getUserId());
        if (dcwsNormalTaskBo.getTaskId() != null){
            queryWrapper.eq("t.task_Id", dcwsNormalTaskBo.getTaskId());
        }
        if (dcwsNormalTaskBo.getTaskName() != null){
            queryWrapper.like("t.task_Name", dcwsNormalTaskBo.getTaskName());
        }
        if (!Objects.isNull(dcwsNormalTaskBo.getStartTime())) {
            queryWrapper.gt("t.create_time",DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,dcwsNormalTaskBo.getStartTime()));
        }
        if (!Objects.isNull(dcwsNormalTaskBo.getEndTime())) {
            queryWrapper.lt("t.create_time",DcwsDateUtils.plusDays(DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,dcwsNormalTaskBo.getEndTime()),1));
        }
        if (dcwsNormalTaskBo.getCompanyId() != null){
            queryWrapper.eq("t.company_Id", dcwsNormalTaskBo.getCompanyId());
        }
        queryWrapper.orderByDesc("t.create_time");
        Page<DcwsNormalTaskVo> page = dcwsNormalTaskMapper.getTaskWaitByPage(pageQuery.build(), queryWrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<DcwsNormalTaskVo> getPageByTaskFinish(DcwsNormalTaskBo dcwsNormalTaskBo, PageQuery pageQuery) {
        QueryWrapper<DcwsNormalTaskVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t.user_Id", LoginHelper.getUserId());
        if (dcwsNormalTaskBo.getTaskId() != null){
            queryWrapper.eq("t.task_Id", dcwsNormalTaskBo.getTaskId());
        }
        if (dcwsNormalTaskBo.getTaskName() != null){
            queryWrapper.like("t.task_Name", dcwsNormalTaskBo.getTaskName());
        }
        if (!Objects.isNull(dcwsNormalTaskBo.getStartTime())) {
            queryWrapper.gt("t.create_time",DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,dcwsNormalTaskBo.getStartTime()));
        }
        if (!Objects.isNull(dcwsNormalTaskBo.getEndTime())) {
            queryWrapper.lt("t.create_time",DcwsDateUtils.plusDays(DcwsDateUtils.dateTime(DcwsDateUtils.YYYY_MM_DD,dcwsNormalTaskBo.getEndTime()),1));
        }
        if (dcwsNormalTaskBo.getCompanyId() != null){
            queryWrapper.eq("t.company_Id", dcwsNormalTaskBo.getCompanyId());
        }
        queryWrapper.orderByDesc("t.create_time");
        Page<DcwsNormalTaskVo> page = dcwsNormalTaskMapper.getTaskFinishByPage(pageQuery.build(), queryWrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 查询非标准流程列表
     */
    @Override
    public List<DcwsNormalTaskVo> queryList(DcwsNormalTaskBo bo) {
        LambdaQueryWrapper<DcwsNormalTask> lqw = buildQueryWrapper(bo);
        return dcwsNormalTaskMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<DcwsNormalTask> buildQueryWrapper(DcwsNormalTaskBo bo) {
        LambdaQueryWrapper<DcwsNormalTask> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        return lqw;
    }

    /**
     * 新增非标准流程
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DcwsNormalTaskVo insertByBo(DcwsNormalTaskBo bo) {
        Long userId = LoginHelper.getUserId();
        DcwsNormalTask add = MapstructUtils.convert(bo, DcwsNormalTask.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus(BusinessStatusEnum.WAITING.getStatus());
            add.setCreateBy(userId);
        }
        String taskId = taskSerialService.getTaskSerial("20",DcwsDateUtils.dateTime());
        add.setTaskId(taskId);
        SysDeptVo sysDeptVo = iSysUserService.getCompany();
        add.setCompanyId(sysDeptVo.getDeptId());
        add.setCompanyName(sysDeptVo.getDeptName());
        //新增通用审批表
        boolean flag = dcwsNormalTaskMapper.insert(add) > 0;
        if (flag) {
             //项目下新增非标准流程
             if (!Objects.isNull(add.getProjectId()))  {
                 DcwsProjectTask dcwsProjectTask = new DcwsProjectTask();
                 dcwsProjectTask.setTaskType("20");//非标准流程
                 dcwsProjectTask.setProjectId(add.getProjectId());
                 dcwsProjectTask.setTaskName(add.getTaskName());
                 dcwsProjectTask.setTaskId(taskId);
                 dcwsProjectTask.setTaskStatus("inprogress");
                 dcwsProjectTask.setCreateBy(LoginHelper.getUserId());
                 dcwsProjectTask.setCreateEmpName(LoginHelper.getUsername());
                 dcwsProjectTaskMapper.insert(dcwsProjectTask);

                 DcwsProjectTaskBo dcwsProjectTaskBo = new DcwsProjectTaskBo();
                 dcwsProjectTaskBo.setProjectId(add.getProjectId());
                 dcwsProjectTaskBo.setTaskStatus("inprogress");
                 projectService.updateProjectTaskCount(dcwsProjectTaskBo);
             }
            //通用审批处理历史表
            DcwsNormalTaskHandleHis dcwsHis = new DcwsNormalTaskHandleHis();
            dcwsHis.setTaskId(taskId);
            dcwsHis.setComment("通过");
            dcwsHis.setStatus("pass");
            dcwsHis.setUserId(userId);
            SysUserVo sysUserVo = iSysUserService.selectUserById(userId);
            dcwsHis.setUserName(sysUserVo.getUserName());
            dcwsHis.setCreateTime(new Date());
            dcwsHis.setUpdateTime(new Date());
            dcwsHisMapper.insert(dcwsHis);
            if (!StringUtils.isBlank(add.getUserId())){
                String[] users = add.getUserId().split(",");
                for (String userIdTemp : users){
                    //新增通用审批处理人表
                    DcwsNormalTaskUser dcwsUser = new DcwsNormalTaskUser();
                    dcwsUser.setTaskId(taskId);
                    dcwsUser.setUserId(userIdTemp);
                    sysUserVo = iSysUserService.selectUserById(Long.valueOf(userIdTemp));
                    dcwsUser.setUserName(sysUserVo.getUserName());
                    dcwsUserMapper.insert(dcwsUser);
                    //通用审批处理历史表
                    DcwsNormalTaskHandleHis dcwsHisTemp = new DcwsNormalTaskHandleHis();
                    dcwsHisTemp.setTaskId(taskId);
                    dcwsHisTemp.setComment(BusinessStatusEnum.findByStatus(BusinessStatusEnum.WAITING.getStatus()));
                    dcwsHisTemp.setStatus(BusinessStatusEnum.WAITING.getStatus());
                    dcwsHisTemp.setUserId(Long.valueOf(userIdTemp));
                    dcwsHisTemp.setUserName(sysUserVo.getUserName());
                    dcwsHisTemp.setCreateTime(new Date());
                    dcwsHisMapper.insert(dcwsHisTemp);
                }
            }
        }
        return MapstructUtils.convert(add, DcwsNormalTaskVo.class);
    }

    /**
     * 修改非标准流程
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DcwsNormalTaskVo updateByBo(DcwsNormalTaskBo bo) {
        DcwsNormalTask update = MapstructUtils.convert(bo, DcwsNormalTask.class);
        dcwsNormalTaskMapper.update(null, new LambdaUpdateWrapper<DcwsNormalTask>()
                .set(DcwsNormalTask::getStatus, bo.getStatus())
                .eq(DcwsNormalTask::getTaskId, bo.getTaskId()));

        dcwsHisMapper.update(null, new LambdaUpdateWrapper<DcwsNormalTaskHandleHis>()
                .set(DcwsNormalTaskHandleHis::getStatus, bo.getStatus())
                .set(DcwsNormalTaskHandleHis::getComment,BusinessStatusEnum.findByStatus(bo.getStatus()))
                .set(DcwsNormalTaskHandleHis::getUpdateTime,new Date())
                .eq(DcwsNormalTaskHandleHis::getUserId, LoginHelper.getUserId())
                .isNull(DcwsNormalTaskHandleHis::getUpdateTime)
                .eq(DcwsNormalTaskHandleHis::getTaskId, bo.getTaskId()));

        if (BusinessStatusEnum.FINISH.getStatus().equals(bo.getStatus()) || "pass".equals(bo.getStatus())){
            dcwsHisMapper.update(null, new LambdaUpdateWrapper<DcwsNormalTaskHandleHis>()
                    .set(DcwsNormalTaskHandleHis::getIsDisplay, "N")
                    .isNull(DcwsNormalTaskHandleHis::getUpdateTime)
                    .eq(DcwsNormalTaskHandleHis::getTaskId, bo.getTaskId()));
        }
        return MapstructUtils.convert(update, DcwsNormalTaskVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelProcessApply(String id) {
        dcwsHisMapper.update(null, new LambdaUpdateWrapper<DcwsNormalTaskHandleHis>()
                .set(DcwsNormalTaskHandleHis::getStatus, BusinessStatusEnum.CANCEL.getStatus())
                .set(DcwsNormalTaskHandleHis::getComment,BusinessStatusEnum.CANCEL.getDesc())
                .set(DcwsNormalTaskHandleHis::getUpdateTime,new Date())
                .eq(DcwsNormalTaskHandleHis::getUserId, LoginHelper.getUserId())
                .isNull(DcwsNormalTaskHandleHis::getUpdateTime)
                .eq(DcwsNormalTaskHandleHis::getTaskId, id));

        dcwsHisMapper.update(null, new LambdaUpdateWrapper<DcwsNormalTaskHandleHis>()
                    .set(DcwsNormalTaskHandleHis::getIsDisplay, "N")
                    .isNull(DcwsNormalTaskHandleHis::getUpdateTime)
                    .eq(DcwsNormalTaskHandleHis::getTaskId, id));

        return dcwsNormalTaskMapper.update(null, new LambdaUpdateWrapper<DcwsNormalTask>()
                .set(DcwsNormalTask::getStatus, BusinessStatusEnum.CANCEL.getStatus())
                .eq(DcwsNormalTask::getTaskId, id)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRunAndHisInstance(String id) {
        dcwsHisMapper.delete(new LambdaQueryWrapper<DcwsNormalTaskHandleHis>()
                .eq(DcwsNormalTaskHandleHis::getTaskId, id));
        dcwsUserMapper.delete(new LambdaQueryWrapper<DcwsNormalTaskUser>()
                .eq(DcwsNormalTaskUser::getTaskId, id));
        return dcwsNormalTaskMapper.delete(new LambdaQueryWrapper<DcwsNormalTask>()
                .eq(DcwsNormalTask::getTaskId, id)) > 0;
    }

    @Override
    public List<DcwsNormalTaskHandleHisVo> getHistoryRecord(String id) {
        LambdaQueryWrapper<DcwsNormalTaskHandleHis> lqw = Wrappers.lambdaQuery();
        lqw.eq(DcwsNormalTaskHandleHis::getTaskId, id);
        lqw.eq(DcwsNormalTaskHandleHis::getIsDisplay, "Y");
        lqw.orderByDesc(DcwsNormalTaskHandleHis::getUpdateTime);
        return dcwsHisMapper.selectVoList(lqw);
    }

    @Override
    public List<DcwsTaskTypeVo> queryWfType() {
        LambdaQueryWrapper<WfCategory> lqw = Wrappers.lambdaQuery();
        lqw.notIn(WfCategory::getCategoryCode,20);
        lqw.orderByDesc(WfCategory::getId);
        List<WfCategoryVo> list = wfCategoryMapper.selectVoList(lqw);
        List<DcwsTaskTypeVo> tempList = new ArrayList<>();
        for(WfCategoryVo wfCategoryVo : list){
            DcwsTaskTypeVo dcwsTaskTypeVo = new DcwsTaskTypeVo();
            dcwsTaskTypeVo.setTaskType(Long.valueOf(wfCategoryVo.getCategoryCode()));
            dcwsTaskTypeVo.setTaskName(wfCategoryVo.getCategoryName());
            tempList.add(dcwsTaskTypeVo);
        }
        return tempList;
    }
}
