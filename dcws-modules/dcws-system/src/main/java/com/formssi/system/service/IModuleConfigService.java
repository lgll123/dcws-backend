package com.formssi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.system.domain.SysDocParam;
import com.formssi.system.domain.SysModuleConfig;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/26 15:20
 */
public interface IModuleConfigService extends IService<SysModuleConfig> {

    /**
     * 根据模块id获取全局头参数列表
     * @param moduleId 模块id
     * @return 返回全局头参数列表
     */
    List<SysDocParam> listGlobalHeaders(long moduleId);

    /**
     * 根据模块id获取全局参数
     * @param moduleId 模块id
     * @return 返回全局参数列表
     */
    List<SysDocParam> listGlobalParams(long moduleId);

    /**
     * 根据模块id获取全局响应
     * @param moduleId 模块id
     * @return 返回全局响应参数列表
     */
    List<SysDocParam> listGlobalReturns(long moduleId);

}
