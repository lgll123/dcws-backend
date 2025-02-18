package com.formssi.chorflow.controller;

import com.formssi.chorflow.domain.SysBussys;
import com.formssi.chorflow.domain.vo.SysBussysVo;
import com.formssi.chorflow.service.ISysBussysService;
import com.formssi.common.core.domain.R;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 应用系统表 前端控制器
 * </p>
 *
 * @author lijun
 */
@RestController
@RequestMapping("/bus-sys")
public class SysBussysController {

  @Autowired
  private ISysBussysService sysBussysService;

  @GetMapping("/tree")
  @Description("树形列表")
  @Operation(summary = "树形列表")
  public R<List<SysBussysVo>> tree() {
    return R.ok(sysBussysService.tree());
  }

  @GetMapping("/page")
  @Description("分页")
  @Operation(summary = "分页")
  public TableDataInfo<SysBussys> page(SysBussys sysBussys, PageQuery pageQuery) {
    return sysBussysService.selectPageChorFlowList(sysBussys, pageQuery);
  }

}
