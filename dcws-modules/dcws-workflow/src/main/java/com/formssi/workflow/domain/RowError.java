package com.formssi.workflow.domain;

import lombok.Data;

@Data
public class RowError {
    private int rowNum;  // 行号（1-based）
    private String message; // 错误信息

    public RowError(int rowNum, String message) {
        this.rowNum = rowNum;
        this.message = message;
    }
}
