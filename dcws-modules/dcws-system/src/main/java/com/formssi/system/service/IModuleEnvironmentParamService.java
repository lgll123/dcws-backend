package com.formssi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.system.domain.SysModuleEnvironmentParam;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/26 16:39
 */
public interface IModuleEnvironmentParamService extends IService<SysModuleEnvironmentParam> {

    /**
     * 根据环境id和类型查询模块环境参数列表
     * @param environmentId 环境id
     * @param style 类型
     * @return 返回环境参数列表
     */
    List<SysModuleEnvironmentParam> listByEnvironmentAndStyle(Long environmentId, byte style);

    /**
     * 获取所有的公共参数
     *
     * @param environmentId 环境id
     * @return 返回所有的公共参数
     */
    List<SysModuleEnvironmentParam> listAllByEnvironment(long environmentId);

}
