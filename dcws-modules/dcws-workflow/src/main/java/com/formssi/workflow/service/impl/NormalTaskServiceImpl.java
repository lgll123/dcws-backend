package com.formssi.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.domain.BaseEntity;
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
public class NormalTaskServiceImpl implements NormalTaskService {

    private final DcwsNormalTaskMapper baseMapper;
    private final DcwsNormalTaskHandleHisMapper dcwsHisMapper;
    private final DcwsNormalTaskUserMapper dcwsUserMapper;
    private final ISysUserService iSysUserService;
    private final DcwsProjectTaskMapper dcwsProjectTaskMapper;

    /**
     * 查询非标准流程
     */
    @Override
    public DcwsNormalTaskVo queryById(Long id) {
        DcwsNormalTaskVo dcwsApproveVo = baseMapper.selectVoById(id);
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
        lqw.eq(DcwsNormalTask::getCreateBy, LoginHelper.getUserId());
        Page<DcwsNormalTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public TableDataInfo<DcwsNormalTaskVo> getPageByTaskWait(DcwsNormalTaskBo dcwsNormalTaskBo, PageQuery pageQuery) {
        QueryWrapper<DcwsNormalTaskVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t.status", BusinessStatusEnum.WAITING.getStatus());
        queryWrapper.eq("t.user_Id", LoginHelper.getUserId());
        queryWrapper.orderByDesc("t.create_time");
        Page<DcwsNormalTaskVo> page = baseMapper.getTaskWaitByPage(pageQuery.build(), queryWrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<DcwsNormalTaskVo> getPageByTaskFinish(DcwsNormalTaskBo dcwsNormalTaskBo, PageQuery pageQuery) {
        QueryWrapper<DcwsNormalTaskVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t.user_Id", LoginHelper.getUserId());
        queryWrapper.orderByDesc("t.create_time");
        Page<DcwsNormalTaskVo> page = baseMapper.getTaskFinishByPage(pageQuery.build(), queryWrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 查询非标准流程列表
     */
    @Override
    public List<DcwsNormalTaskVo> queryList(DcwsNormalTaskBo bo) {
        LambdaQueryWrapper<DcwsNormalTask> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
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
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
             //项目下新增非标准流程
             if (!Objects.isNull(add.getProjectId()))  {
                 DcwsProjectTask dcwsProjectTask = new DcwsProjectTask();
                 dcwsProjectTask.setTaskType("20");//非标准流程
                 dcwsProjectTask.setProjectId(add.getProjectId());
                 dcwsProjectTask.setTaskName(add.getTaskName());
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
        baseMapper.updateByTaskId(bo.getStatus(),bo.getUserId(),bo.getTaskId());

        Long userId = LoginHelper.getUserId();
        dcwsHisMapper.updateByTaskId(BusinessStatusEnum.findByStatus(bo.getStatus()),String.valueOf(userId),bo.getTaskId());

        //如果状态为待审核，则更新审批处理人表和通用审批处理历史表
        if (BusinessStatusEnum.WAITING.getStatus().equals(bo.getStatus())){

            dcwsUserMapper.deleteById(bo.getTaskId());

            String[] users = bo.getUserId().split(",");
            for (String userIdTemp : users){
                //新增通用审批处理人表
                DcwsNormalTaskUser dcwsUser = new DcwsNormalTaskUser();
                dcwsUser.setTaskId(bo.getTaskId());
                dcwsUser.setUserId(userIdTemp);
                SysUserVo sysUserVo = iSysUserService.selectUserById(Long.valueOf(userIdTemp));
                dcwsUser.setUserName(sysUserVo.getUserName());
                dcwsUserMapper.insert(dcwsUser);

                //通用审批处理历史表
                DcwsNormalTaskHandleHis dcwsHisTemp = new DcwsNormalTaskHandleHis();
                dcwsHisTemp.setTaskId(bo.getTaskId());
                dcwsHisTemp.setComment(BusinessStatusEnum.findByStatus(BusinessStatusEnum.WAITING.getStatus()));
                dcwsHisTemp.setStatus(BusinessStatusEnum.WAITING.getStatus());
                dcwsHisTemp.setUserId(Long.valueOf(userIdTemp));
                dcwsHisTemp.setUserName(sysUserVo.getUserName());
                dcwsHisMapper.insert(dcwsHisTemp);
            }
        }
        return MapstructUtils.convert(update, DcwsNormalTaskVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelProcessApply(String id) {
        Long userId = LoginHelper.getUserId();
        return baseMapper.updateByTaskId(BusinessStatusEnum.CANCEL.getStatus(),String.valueOf(userId),Long.valueOf(id)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRunAndHisInstance(String id) {
        dcwsHisMapper.deleteByTaskId(Long.valueOf(id));
        dcwsUserMapper.deleteByTaskId(Long.valueOf(id));
        return baseMapper.deleteByTaskId(Long.valueOf(id)) > 0;
    }

    @Override
    public List<DcwsNormalTaskHandleHisVo> getHistoryRecord(Long id) {
        return dcwsHisMapper.getHistoryRecord(id);
    }
}
