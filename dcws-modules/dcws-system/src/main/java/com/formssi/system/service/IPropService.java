package com.formssi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.system.domain.SysProp;
import com.formssi.system.enums.PropTypeEnum;

import java.util.Map;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/26 9:41
 */
public interface IPropService extends IService<SysProp> {

    /**
     * 保存属性参数
     * @param props 参数map
     * @param refId 关联id
     * @param type 类型
     */
    void saveProps(Map<String, ?> props, Long refId, PropTypeEnum type);

    /**
     * 根据文档id获取属性
     * @param docId 文档id
     * @return 属性map
     */
    Map<String, String> getDocProps(Long docId);
}
