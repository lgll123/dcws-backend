package com.formssi.system.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.formssi.system.domain.SysDocParam;

/**
 * @author tanghc
 */
public interface SysDocParamMapper extends BaseMapper<SysDocParam> {

    int saveParam(SysDocParam sysDocParam);

}
