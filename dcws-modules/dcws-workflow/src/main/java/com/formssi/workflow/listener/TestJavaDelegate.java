package com.formssi.workflow.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.el.FixedValue;
import org.flowable.engine.impl.el.JuelExpression;
import org.springframework.stereotype.Component;

/**
 * 测试Java代理
 *
 * @author zhangmiao
 */
@Slf4j
@Component
public class TestJavaDelegate implements JavaDelegate {

	FixedValue name;

	@Override
	public void execute(DelegateExecution delegateExecution) {
		log.info("============= 执行Java监听器通知 =================");
		log.info("Java Delegate Execution: {} {}", delegateExecution.getEventName(), delegateExecution);
		log.info("Java Delegate Variables: {}", delegateExecution.getVariables());
		log.info("Java Delegate String: {}", name.getExpressionText());
	}

	public void hello() {
		log.info("Call method hello");
	}

}

