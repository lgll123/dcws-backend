package com.formssi.chorflow.service;


import cn.kstry.framework.core.annotation.NoticeResult;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskParam;
import cn.kstry.framework.core.annotation.TaskService;
import com.formssi.chorflow.domain.flow.HttpActionVo;
import com.formssi.chorflow.domain.flow.HttpRequestVo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TaskComponent(name = "VariableTestFlowService")
public class VariableComponent {

  @TaskService(name = "test", desc = "test")
  @NoticeResult
  public HttpRequestVo test(@TaskParam(HttpActionVo.Fields.request) HttpRequestVo request) {
    return request;
  }
}
