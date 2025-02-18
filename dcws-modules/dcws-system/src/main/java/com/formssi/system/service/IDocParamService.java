package com.formssi.system.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.common.core.domain.model.LoginUser;
import com.formssi.system.domain.SysDocInfo;
import com.formssi.system.domain.SysDocParam;
import com.formssi.system.domain.dto.DocParamDTO;
import com.formssi.system.enums.ParamStyleEnum;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/22 17:25
 */
public interface IDocParamService extends IService<SysDocParam> {

    /**
     * 保存文档参数
     * @param sysDocInfo 文档信息
     * @param docParamDTOS 文档参数DTO
     * @param paramStyleEnum 参数类型枚举类
     * @param loginUser 用户登录信息
     */
    void saveParams(SysDocInfo sysDocInfo, List<DocParamDTO> docParamDTOS, ParamStyleEnum paramStyleEnum, LoginUser loginUser);

    /**
     * 根据文档id获取文档参数列表
     * @param docId 文档id
     * @return 返回文档参数列表
     */
    List<SysDocParam> getListByDocId(Long docId);
}
