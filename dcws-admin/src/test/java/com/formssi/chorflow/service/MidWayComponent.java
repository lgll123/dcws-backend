package com.formssi.chorflow.service;


import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@TaskComponent(name = "MidWayComponent")
public class MidWayComponent {


  @TaskService(name = "test")
  public void test(ScopeDataOperator dataOperator) {
    Optional<String> optional = dataOperator.getTaskProperty();
    if (optional.isPresent()) {
      String taskProperty = optional.get();
      log.info("taskProperty:{}", taskProperty);
      dataOperator.setVarData(taskProperty, taskProperty);
      return;
    }
    log.info("taskProperty is null");
  }

}
