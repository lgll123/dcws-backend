package com.formssi.chorflow.service;


import cn.hutool.core.thread.ThreadUtil;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TaskComponent(name = "RetryFlowService")
public class RetryComponent {


  public static AtomicInteger atomicInteger = new AtomicInteger(0);

  @TaskService(name = "test", desc = "test")
  public void test(ScopeDataOperator dataOperator) {
    atomicInteger.incrementAndGet();
    ThreadUtil.sleep(20000);
  }


}
