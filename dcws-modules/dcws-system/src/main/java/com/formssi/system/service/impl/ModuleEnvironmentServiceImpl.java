package com.formssi.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.system.domain.SysModuleEnvironment;
import com.formssi.system.enums.BooleanEnum;
import com.formssi.system.mapper.SysModuleEnvironmentMapper;
import com.formssi.system.service.IModuleEnvironmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/26 13:52
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ModuleEnvironmentServiceImpl extends ServiceImpl<SysModuleEnvironmentMapper, SysModuleEnvironment> implements IModuleEnvironmentService {

    @Override
    public List<SysModuleEnvironment> listModuleEnvironment(long moduleId) {
        return lambdaQuery().eq(SysModuleEnvironment::getModuleId, moduleId)
                .eq(SysModuleEnvironment::getIsDeleted, BooleanEnum.FALSE.getType())
                .orderByDesc(SysModuleEnvironment::getGmtCreate)
                .list();
    }

    @Override
    public SysModuleEnvironment getFirst(long moduleId) {
        return lambdaQuery().eq(SysModuleEnvironment::getModuleId, moduleId)
                .eq(SysModuleEnvironment::getIsDeleted, BooleanEnum.FALSE.getType())
                .orderByAsc(SysModuleEnvironment::getId)
                .one();
    }

}
