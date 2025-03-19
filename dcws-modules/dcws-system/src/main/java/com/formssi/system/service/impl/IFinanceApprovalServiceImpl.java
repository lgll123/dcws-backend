package com.formssi.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.formssi.system.domain.DcwsFinanceApproval;
import com.formssi.system.domain.vo.DcwsFinanceApprovalVo;
import com.formssi.system.mapper.DcwsFinanceApprovalMapper;
import com.formssi.system.service.IFinanceApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class IFinanceApprovalServiceImpl implements IFinanceApprovalService {
    private final DcwsFinanceApprovalMapper dcwsFinanceApprovalMapper;

    @Override
    public DcwsFinanceApprovalVo selectFinanceApprovalByDeptId(Long deptId) {
        LambdaQueryWrapper<DcwsFinanceApproval> lqw = Wrappers.lambdaQuery();
        lqw.eq(DcwsFinanceApproval::getDeptId, deptId);
        return dcwsFinanceApprovalMapper.selectVoOne(lqw);
    }
}
