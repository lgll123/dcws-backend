package com.formssi.common.core.exception;

/**
 * 构造器异常
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/7 9:12
 */
public class ConstructionException extends RuntimeException {

    public ConstructionException(Throwable cause) {
        super(cause);
    }

    public ConstructionException(String message, Throwable cause) {
        super(message, cause);
    }
}
