package com.formssi.chorflow.controller;

import com.formssi.chorflow.domain.ChorFlow;
import com.formssi.chorflow.domain.flow.FlowReq;
import com.formssi.chorflow.domain.vo.ChorFlowVo;
import com.formssi.chorflow.service.IChorFlowService;
import com.formssi.common.core.domain.R;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程编排 控制层
 *
 * @author lijun
 * @date 2024-12-17
 */
@RestController
@RequestMapping("/chorflow")
@Slf4j
public class ChorFlowController {

  @Autowired
  private IChorFlowService chorFlowService;

  @PostMapping("/request")
  @Description("流程请求")
  @Operation(summary = "流程请求")
  public R<Object> request(@RequestBody FlowReq flowReq) {
    return chorFlowService.kstryRequest(flowReq);
  }

  @PostMapping("/addOrUpdate")
  @Description("添加或更新流程")
  @Operation(summary = "添加或更新流程")
  public R<ChorFlow> addOrUpdate(@RequestBody ChorFlow chorFlow) {
    return R.ok(chorFlowService.addOrUpdate(chorFlow));
  }

  @GetMapping("/page")
  @Description("分页")
  @Operation(summary = "分页")
  public TableDataInfo<ChorFlowVo> page(ChorFlowVo chorFlow, PageQuery pageQuery) {
    return chorFlowService.selectPageChorFlowList(chorFlow, pageQuery);
  }

  @DeleteMapping("/delete/{id}")
  @Description("删除")
  @Operation(summary = "删除")
  public R<Void> delete(@PathVariable("id") Long id) {
    chorFlowService.delete(id);
    return R.ok();
  }

  @GetMapping("/get/{id}")
  @Description("查询")
  @Operation(summary = "查询")
  public R<ChorFlowVo> get(@PathVariable("id") Long id) {
    return R.ok(chorFlowService.get(id));
  }


}
