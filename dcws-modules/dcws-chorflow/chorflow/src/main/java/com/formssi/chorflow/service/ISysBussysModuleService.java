package com.formssi.chorflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.chorflow.domain.SysBussysModule;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;

/**
 * <p>
 * 应用系统模块表 服务类
 * </p>
 *
 * @author lijun
 */
public interface ISysBussysModuleService extends IService<SysBussysModule> {

  TableDataInfo<SysBussysModule> selectPageChorFlowList(SysBussysModule sysBussysModule,
      PageQuery pageQuery);
}
