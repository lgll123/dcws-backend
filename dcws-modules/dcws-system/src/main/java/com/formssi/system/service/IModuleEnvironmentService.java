package com.formssi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.system.domain.SysModuleEnvironment;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/26 13:52
 */
public interface IModuleEnvironmentService extends IService<SysModuleEnvironment> {

    /**
     * 查询模块对应的环境
     *
     * @param moduleId 模块id
     * @return 返回环境
     */
    List<SysModuleEnvironment> listModuleEnvironment(long moduleId);

    /**
     * 根据模块id获取模块环境
     * @param moduleId 模块id
     * @return 返回模块环境
     */
    SysModuleEnvironment getFirst(long moduleId);

}
