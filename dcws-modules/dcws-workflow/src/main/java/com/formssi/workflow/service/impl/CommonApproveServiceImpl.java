package com.formssi.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.domain.dto.RoleDTO;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.core.service.WorkflowService;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.common.tenant.helper.TenantHelper;
import com.formssi.system.domain.vo.SysUserVo;
import com.formssi.system.service.ISysUserService;
import com.formssi.workflow.common.constant.FlowConstant;
import com.formssi.workflow.domain.DcwsApprove;
import com.formssi.workflow.domain.DcwsHis;
import com.formssi.workflow.domain.DcwsUser;
import com.formssi.workflow.domain.bo.DcwsApproveBo;
import com.formssi.workflow.domain.vo.DcwsApproveVo;
import com.formssi.workflow.domain.vo.TaskVo;
import com.formssi.workflow.domain.vo.WfNodeConfigVo;
import com.formssi.workflow.mapper.DcwsApproveMapper;
import com.formssi.workflow.mapper.DcwsHisMapper;
import com.formssi.workflow.mapper.DcwsUserMapper;
import com.formssi.workflow.service.CommonApproveService;
import com.formssi.workflow.utils.WorkflowUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class CommonApproveServiceImpl implements CommonApproveService {

    private final DcwsApproveMapper baseMapper;
    private final DcwsHisMapper dcwsHisMapper;
    private final DcwsUserMapper dcwsUserMapper;
    private final WorkflowService workflowService;
    private final ISysUserService iSysUserService;

    /**
     * 查询非标准流程
     */
    @Override
    public DcwsApproveVo queryById(Long id) {
        return baseMapper.selectVoById(id);
    }

    /**
     * 查询非标准流程列表
     */
    @Override
    public TableDataInfo<DcwsApproveVo> queryPageList(DcwsApproveBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<DcwsApprove> lqw = buildQueryWrapper(bo);
        lqw.eq(DcwsApprove::getCreateBy, LoginHelper.getUserId());
        Page<DcwsApproveVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    @Override
    public TableDataInfo<DcwsApproveVo> getPageByTaskWait(DcwsApproveBo dcwsApproveBo, PageQuery pageQuery) {
        QueryWrapper<DcwsApproveVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t.status", BusinessStatusEnum.WAITING.getStatus());
        queryWrapper.eq("t.user_Id", LoginHelper.getUserId());
        queryWrapper.orderByDesc("t.create_time");
        Page<DcwsApproveVo> page = baseMapper.getTaskWaitByPage(pageQuery.build(), queryWrapper);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<DcwsApproveVo> getPageByTaskFinish(DcwsApproveBo dcwsApproveBo, PageQuery pageQuery) {
        QueryWrapper<DcwsApproveVo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("t.user_Id", LoginHelper.getUserId());
        queryWrapper.orderByDesc("t.create_time");
        Page<DcwsApproveVo> page = baseMapper.getTaskFinishByPage(pageQuery.build(), queryWrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 查询非标准流程列表
     */
    @Override
    public List<DcwsApproveVo> queryList(DcwsApproveBo bo) {
        LambdaQueryWrapper<DcwsApprove> lqw = buildQueryWrapper(bo);
        return baseMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<DcwsApprove> buildQueryWrapper(DcwsApproveBo bo) {
        LambdaQueryWrapper<DcwsApprove> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(BaseEntity::getCreateTime);
        return lqw;
    }

    /**
     * 新增非标准流程
     */
    @Override
    public DcwsApproveVo insertByBo(DcwsApproveBo bo) {
        Long userId = LoginHelper.getUserId();
        DcwsApprove add = MapstructUtils.convert(bo, DcwsApprove.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus(BusinessStatusEnum.WAITING.getStatus());
            add.setCreateBy(userId);
        }
        //新增通用审批表
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setTaskId(add.getTaskId());
            //通用审批处理历史表
            DcwsHis dcwsHis = new DcwsHis();
            dcwsHis.setTaskId(add.getTaskId());
            dcwsHis.setComment(BusinessStatusEnum.findByStatus(BusinessStatusEnum.PASS.getStatus()));
            dcwsHis.setStatus(BusinessStatusEnum.PASS.getStatus());
            dcwsHis.setUserId(String.valueOf(userId));
            SysUserVo sysUserVo = iSysUserService.selectUserById(userId);
            dcwsHis.setUserName(sysUserVo.getUserName());
            dcwsHis.setUpdateTime(new Date());
            dcwsHisMapper.insert(dcwsHis);
            if (!StringUtils.isBlank(add.getUserId())){
                String[] users = add.getUserId().split(",");
                for (String userIdTemp : users){
                    //新增通用审批处理人表
                    DcwsUser dcwsUser = new DcwsUser();
                    dcwsUser.setTaskId(add.getTaskId());
                    dcwsUser.setUserId(userIdTemp);
                    dcwsUserMapper.insert(dcwsUser);
                    //通用审批处理历史表
                    DcwsHis dcwsHisTemp = new DcwsHis();
                    dcwsHisTemp.setTaskId(add.getTaskId());
                    dcwsHisTemp.setComment(BusinessStatusEnum.findByStatus(BusinessStatusEnum.WAITING.getStatus()));
                    dcwsHisTemp.setStatus(BusinessStatusEnum.WAITING.getStatus());
                    dcwsHisTemp.setUserId(userIdTemp);
                    sysUserVo = iSysUserService.selectUserById(Long.valueOf(userIdTemp));
                    dcwsHisTemp.setUserName(sysUserVo.getUserName());
                    dcwsHisMapper.insert(dcwsHisTemp);
                }
            }
        }
        return MapstructUtils.convert(add, DcwsApproveVo.class);
    }

    /**
     * 修改非标准流程
     */
    @Override
    public DcwsApproveVo updateByBo(DcwsApproveBo bo) {
        DcwsApprove update = MapstructUtils.convert(bo, DcwsApprove.class);
        baseMapper.updateByTaskId(update);

        Long userId = LoginHelper.getUserId();
        DcwsHis dcwsHis = new DcwsHis();
        dcwsHis.setTaskId(bo.getTaskId());
        dcwsHis.setComment(BusinessStatusEnum.findByStatus(bo.getStatus()));
        dcwsHis.setUserId(String.valueOf(userId));
        dcwsHisMapper.updateByTaskId(dcwsHis);

        //如果状态为待审核，则更新审批处理人表和通用审批处理历史表
        if (BusinessStatusEnum.WAITING.getStatus().equals(bo.getStatus())){

            dcwsUserMapper.deleteById(bo.getTaskId());

            String[] users = bo.getUserId().split(",");
            for (String userIdTemp : users){
                //新增通用审批处理人表
                DcwsUser dcwsUser = new DcwsUser();
                dcwsUser.setTaskId(bo.getTaskId());
                dcwsUser.setUserId(userIdTemp);
                dcwsUserMapper.insert(dcwsUser);

                //通用审批处理历史表
                DcwsHis dcwsHisTemp = new DcwsHis();
                dcwsHisTemp.setTaskId(bo.getTaskId());
                dcwsHisTemp.setComment(BusinessStatusEnum.findByStatus(BusinessStatusEnum.WAITING.getStatus()));
                dcwsHisTemp.setStatus(BusinessStatusEnum.WAITING.getStatus());
                dcwsHisTemp.setUserId(userIdTemp);
                SysUserVo sysUserVo = iSysUserService.selectUserById(Long.valueOf(userIdTemp));
                dcwsHisTemp.setUserName(sysUserVo.getUserName());
                dcwsHisMapper.insert(dcwsHisTemp);
            }
        }
        return MapstructUtils.convert(update, DcwsApproveVo.class);
    }

    /**
     * 批量删除非标准流程
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<Long> ids) {
        List<String> idList = StreamUtils.toList(ids, String::valueOf);
        workflowService.deleteRunAndHisInstance(idList);
        return baseMapper.deleteByIds(ids) > 0;
    }
}
