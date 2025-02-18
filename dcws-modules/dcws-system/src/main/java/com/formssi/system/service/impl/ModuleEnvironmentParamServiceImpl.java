package com.formssi.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.system.domain.SysModuleEnvironmentParam;
import com.formssi.system.enums.BooleanEnum;
import com.formssi.system.mapper.SysModuleEnvironmentParamMapper;
import com.formssi.system.service.IModuleEnvironmentParamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/26 16:40
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ModuleEnvironmentParamServiceImpl extends ServiceImpl<SysModuleEnvironmentParamMapper, SysModuleEnvironmentParam> implements IModuleEnvironmentParamService {

    @Override
    public List<SysModuleEnvironmentParam> listByEnvironmentAndStyle(Long environmentId, byte style) {
        if (environmentId == null) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .eq(SysModuleEnvironmentParam::getEnvironmentId, environmentId)
                .eq(SysModuleEnvironmentParam::getStyle, style)
                .eq(SysModuleEnvironmentParam::getIsDeleted, BooleanEnum.FALSE.getType())
                .orderByAsc(SysModuleEnvironmentParam::getOrderIndex)
                .orderByAsc(SysModuleEnvironmentParam::getId)
                .list();
    }

    @Override
    public List<SysModuleEnvironmentParam> listAllByEnvironment(long environmentId) {
        return lambdaQuery()
                .eq(SysModuleEnvironmentParam::getEnvironmentId, environmentId)
                .eq(SysModuleEnvironmentParam::getIsDeleted, BooleanEnum.FALSE.getType())
                .orderByAsc(SysModuleEnvironmentParam::getOrderIndex)
                .orderByAsc(SysModuleEnvironmentParam::getId)
                .list();
    }

}
