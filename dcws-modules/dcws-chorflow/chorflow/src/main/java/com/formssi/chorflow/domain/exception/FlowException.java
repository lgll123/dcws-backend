package com.formssi.chorflow.domain.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义业务异常
 *
 * @author joey
 */
@Getter
@Slf4j
public class FlowException extends RuntimeException {


  public FlowException(String message) {
    super(message);
  }

  public FlowException(String message, Throwable cause) {
    super(message, cause);
  }

  public FlowException(Throwable cause) {
    super(cause);
  }
}