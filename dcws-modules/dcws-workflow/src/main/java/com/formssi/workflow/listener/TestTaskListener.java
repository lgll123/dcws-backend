package com.formssi.workflow.listener;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.impl.el.FixedValue;
import org.flowable.engine.impl.el.JuelExpression;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

/**
 * 测试任务监听器
 *
 * @author zhangmiao
 */
@Slf4j
@Data
@Component
public class TestTaskListener implements TaskListener {

  FixedValue name;

  JuelExpression expName;

  @Override
  public void notify(DelegateTask delegateTask) {
    log.info("============= 任务监听器通知 =================");
    log.info("Delegate Task: {}, {}", delegateTask.getEventName(), delegateTask);
    log.info("Delegate String: {}", name.getExpressionText());
    log.info("Delegate Expression: {}", expName.getExpressionText());
  }

}

