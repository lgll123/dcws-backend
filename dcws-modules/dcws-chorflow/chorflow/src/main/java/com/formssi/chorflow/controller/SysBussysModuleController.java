package com.formssi.chorflow.controller;

import com.formssi.chorflow.domain.SysBussysModule;
import com.formssi.chorflow.service.ISysBussysModuleService;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 应用系统模块表 前端控制器
 * </p>
 *
 * @author lijun
 */
@RestController
@RequestMapping("/bus-sys-module")
public class SysBussysModuleController {

  @Autowired
  private ISysBussysModuleService sysBussysModuleService;

  @GetMapping("/page")
  @Description("分页")
  @Operation(summary = "分页")
  public TableDataInfo<SysBussysModule> page(SysBussysModule sysBussysModule, PageQuery pageQuery) {
    return sysBussysModuleService.selectPageChorFlowList(sysBussysModule, pageQuery);
  }

}
