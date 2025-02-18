package com.formssi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.formssi.system.domain.SysDocInfo;

/**
 * @author tanghc
 */
public interface SysDocInfoMapper extends BaseMapper<SysDocInfo> {

    int saveDocInfo(SysDocInfo sysDocInfo);

}
