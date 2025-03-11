package com.formssi.system.service;

import com.formssi.system.domain.bo.SealInfoBo;
import com.formssi.system.domain.vo.SealInfoVo;

import java.util.List;

/**
 * 印章管理 服务层
 *
 * @author Lion Li
 */
public interface ISealInfoService {

    /**
     * 查询印章数据
     *
     * @param info 印章信息
     * @return 印章信息集合
     */
    List<SealInfoVo> selectSealInfoList(SealInfoBo info);


}
