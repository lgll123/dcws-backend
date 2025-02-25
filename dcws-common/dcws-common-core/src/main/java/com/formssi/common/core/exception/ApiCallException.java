package com.formssi.common.core.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ApiCallException  extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误提示
     */
    private String message;
    /**
     * 错误明细，内部调试错误
     */
    private String detailMessage;

    public ApiCallException(String message) {
        this.message = message;
    }

    public ApiCallException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public ApiCallException setMessage(String message) {
        this.message = message;
        return this;
    }

    public ApiCallException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }
}
