package com.formssi.workflow.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import com.formssi.common.tenant.helper.TenantHelper;
import com.formssi.workflow.domain.ActHiProcinst;
import com.formssi.workflow.mapper.ActHiProcinstMapper;
import com.formssi.workflow.service.IActHiProcinstService;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 流程实例Service业务层处理
 *
 * @author may
 * @date 2023-07-22
 */
@RequiredArgsConstructor
@Service
public class ActHiProcinstServiceImpl implements IActHiProcinstService {

    private final ActHiProcinstMapper baseMapper;

    /**
     * 按照业务id查询
     *
     * @param businessKeys 业务id
     */
    @Override
    public List<ActHiProcinst> selectByBusinessKeyIn(List<String> businessKeys) {
        return baseMapper.selectList(new LambdaQueryWrapper<ActHiProcinst>()
            .in(ActHiProcinst::getBusinessKey, businessKeys)
            .eq(TenantHelper.isEnable(), ActHiProcinst::getTenantId, TenantHelper.getTenantId()));
    }

    /**
     * 按照业务id查询
     *
     * @param businessKey 业务id
     */
    @Override
    public ActHiProcinst selectByBusinessKey(String businessKey) {
        return baseMapper.selectOne(new LambdaQueryWrapper<ActHiProcinst>()
            .eq(ActHiProcinst::getBusinessKey, businessKey)
            .eq(TenantHelper.isEnable(), ActHiProcinst::getTenantId, TenantHelper.getTenantId()));

    }
}
