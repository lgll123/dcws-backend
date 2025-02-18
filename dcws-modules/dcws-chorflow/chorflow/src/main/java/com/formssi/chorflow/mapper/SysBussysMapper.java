package com.formssi.chorflow.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.formssi.chorflow.domain.SysBussys;

/**
 * <p>
 * 应用系统表 Mapper 接口
 * </p>
 *
 * @author lijun
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface SysBussysMapper extends BaseMapper<SysBussys> {

}
