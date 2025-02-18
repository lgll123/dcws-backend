package com.formssi.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.common.core.utils.CopyUtil;
import com.formssi.system.domain.SysDocParam;
import com.formssi.system.domain.SysModuleConfig;
import com.formssi.system.domain.SysModuleEnvironment;
import com.formssi.system.domain.SysModuleEnvironmentParam;
import com.formssi.system.enums.ParamStyleEnum;
import com.formssi.system.mapper.SysModuleConfigMapper;
import com.formssi.system.service.IModuleConfigService;
import com.formssi.system.service.IModuleEnvironmentParamService;
import com.formssi.system.service.IModuleEnvironmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/26 15:21
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ModuleConfigServiceImpl extends ServiceImpl<SysModuleConfigMapper, SysModuleConfig> implements IModuleConfigService {

    private final IModuleEnvironmentService moduleEnvironmentService;
    private final IModuleEnvironmentParamService moduleEnvironmentParamService;

    @Override
    public List<SysDocParam> listGlobalHeaders(long moduleId) {
        return listGlobal(moduleId, ParamStyleEnum.HEADER);
    }

    @Override
    public List<SysDocParam> listGlobalParams(long moduleId) {
        return listGlobal(moduleId, ParamStyleEnum.REQUEST);
    }

    @Override
    public List<SysDocParam> listGlobalReturns(long moduleId) {
        return listGlobal(moduleId, ParamStyleEnum.RESPONSE);
    }

    private List<SysDocParam> listGlobal(long moduleId, ParamStyleEnum paramStyleEnum) {
        SysModuleEnvironment environment = moduleEnvironmentService.getFirst(moduleId);
        if (environment == null) {
            return Collections.emptyList();
        }
        List<SysModuleEnvironmentParam> moduleEnvironmentParams = moduleEnvironmentParamService.listByEnvironmentAndStyle(environment.getId(), paramStyleEnum.getStyle());
        // id去重，防止跟doc_param表id重复
        long offset = System.currentTimeMillis();
        for (SysModuleEnvironmentParam moduleEnvironmentParam : moduleEnvironmentParams) {
            moduleEnvironmentParam.setId(moduleEnvironmentParam.getId() + offset);
            if (moduleEnvironmentParam.getParentId() > 0) {
                moduleEnvironmentParam.setParentId(moduleEnvironmentParam.getParentId() + offset);
            }
        }
        return CopyUtil.copyList(moduleEnvironmentParams, SysDocParam::new);
    }

}
