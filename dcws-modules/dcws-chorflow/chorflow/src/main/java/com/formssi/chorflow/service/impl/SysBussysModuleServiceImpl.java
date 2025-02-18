package com.formssi.chorflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.chorflow.domain.SysBussysModule;
import com.formssi.chorflow.mapper.SysBussysModuleMapper;
import com.formssi.chorflow.service.ISysBussysModuleService;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 应用系统模块表 服务实现类
 * </p>
 *
 * @author lijun
 * @since 2024-12-19
 */
@Service
public class SysBussysModuleServiceImpl extends
    ServiceImpl<SysBussysModuleMapper, SysBussysModule> implements ISysBussysModuleService {


  @Override
  public TableDataInfo<SysBussysModule> selectPageChorFlowList(SysBussysModule sysBussysModule,
      PageQuery pageQuery) {
    Page<SysBussysModule> page = baseMapper.selectPage(pageQuery.build(),
        this.buildQueryWrapper(sysBussysModule));
    return TableDataInfo.build(page);
  }

  // ===============================================================

  private LambdaQueryWrapper<SysBussysModule> buildQueryWrapper(SysBussysModule sysBussysModule) {
    LambdaQueryWrapper<SysBussysModule> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(StringUtils.isNotBlank(sysBussysModule.getSystemCode()),
            SysBussysModule::getSystemCode, sysBussysModule.getSystemCode())
        .like(StringUtils.isNotBlank(sysBussysModule.getModuleName()),
            SysBussysModule::getModuleName, sysBussysModule.getModuleName())
        .like(StringUtils.isNotBlank(sysBussysModule.getDescription()),
            SysBussysModule::getDescription, sysBussysModule.getDescription());
    return wrapper;
  }

}
