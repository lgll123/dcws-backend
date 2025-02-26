package com.formssi.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.constant.UserConstants;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.system.domain.SysDept;
import com.formssi.system.domain.vo.SysUserVo;
import com.formssi.system.service.ISysUserService;
import com.formssi.workflow.domain.*;
import com.formssi.workflow.domain.bo.DcwsNormalTaskBo;
import com.formssi.workflow.domain.vo.*;
import com.formssi.workflow.mapper.*;
import com.formssi.workflow.service.NormalTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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

    /**
     * 查询非标准流程
     */
    @Override
    public DcwsNormalTaskVo queryById(Long id) {
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
        //新增通用审批表
        boolean flag = dcwsNormalTaskMapper.insert(add) > 0;
        if (flag) {
             //项目下新增非标准流程
             if (!Objects.isNull(add.getProjectId()))  {
                 DcwsProjectTask dcwsProjectTask = new DcwsProjectTask();
                 dcwsProjectTask.setTaskType("20");//非标准流程
                 dcwsProjectTask.setProjectId(add.getProjectId());
                 dcwsProjectTask.setTaskName(add.getTaskName());
                 dcwsProjectTask.setTaskId(add.getTaskId());
                 dcwsProjectTask.setTaskStatus(BusinessStatusEnum.INPROGRESS.getStatus());
                 dcwsProjectTask.setCreateBy(LoginHelper.getUserId());
                 dcwsProjectTask.setCreateEmpName(LoginHelper.getUsername());
                 dcwsProjectTaskMapper.insert(dcwsProjectTask);
             }
            //通用审批处理历史表
            DcwsNormalTaskHandleHis dcwsHis = new DcwsNormalTaskHandleHis();
            dcwsHis.setTaskId(add.getTaskId());
            dcwsHis.setComment(BusinessStatusEnum.findByStatus(BusinessStatusEnum.PASS.getStatus()));
            dcwsHis.setStatus(BusinessStatusEnum.PASS.getStatus());
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
                    dcwsUser.setTaskId(add.getTaskId());
                    dcwsUser.setUserId(userIdTemp);
                    sysUserVo = iSysUserService.selectUserById(Long.valueOf(userIdTemp));
                    dcwsUser.setUserName(sysUserVo.getUserName());
                    dcwsUserMapper.insert(dcwsUser);
                    //通用审批处理历史表
                    DcwsNormalTaskHandleHis dcwsHisTemp = new DcwsNormalTaskHandleHis();
                    dcwsHisTemp.setTaskId(add.getTaskId());
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

        if (BusinessStatusEnum.FINISH.getStatus().equals(bo.getStatus()) || BusinessStatusEnum.PASS.getStatus().equals(bo.getStatus())){
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
                .eq(DcwsNormalTaskHandleHis::getTaskId, Long.valueOf(id)));

        dcwsHisMapper.update(null, new LambdaUpdateWrapper<DcwsNormalTaskHandleHis>()
                    .set(DcwsNormalTaskHandleHis::getIsDisplay, "N")
                    .isNull(DcwsNormalTaskHandleHis::getUpdateTime)
                    .eq(DcwsNormalTaskHandleHis::getTaskId, Long.valueOf(id)));

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
    public List<DcwsNormalTaskHandleHisVo> getHistoryRecord(Long id) {
        LambdaQueryWrapper<DcwsNormalTaskHandleHis> lqw = Wrappers.lambdaQuery();
        lqw.eq(DcwsNormalTaskHandleHis::getTaskId, id);
        lqw.eq(DcwsNormalTaskHandleHis::getIsDisplay, "Y");
        lqw.orderByDesc(DcwsNormalTaskHandleHis::getUpdateTime);
        return dcwsHisMapper.selectVoList(lqw);
    }
}
