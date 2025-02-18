package com.formssi.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 测试执行监听器
 *
 * @author zhangmiao
 */
@Slf4j
@Component
public class TestExecutionListener implements ExecutionListener {

  @Override
  public void notify(DelegateExecution execution) {
    log.info("============= 执行监听器通知 =================");
    log.info("Delegate Execution: {}, {}", execution.getEventName(), execution);
  }

}