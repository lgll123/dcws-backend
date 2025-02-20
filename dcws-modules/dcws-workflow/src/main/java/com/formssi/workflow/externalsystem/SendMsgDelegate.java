package com.formssi.workflow.externalsystem;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

@Slf4j
public class SendMsgDelegate implements JavaDelegate {


    public void execute(DelegateExecution execution) {

            log.error("send a msg to caigou");

    }
}
