package com.formssi.system.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 操作消息提醒
 */
@Data
public class HrResultVo<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code;

    private List<Object> conditionList;

    private List<T> data;

    private String failure;

    private String message;

    private String page;

    private String success;

    private List<Object> valueList;

    public HrResultVo() {
    }


}
