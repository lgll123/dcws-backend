package com.formssi.workflow.domain.vo;

import lombok.Data;

@Data
public class WfUserTaskButtonVo {
    /**
     * 按钮名称
     * */
    private String name;

    /**
     * 按钮key eg:pass
     * */
    private String prop;

    /**
     * 是否显示 0:不显示 1：显示
     * */
    private String disable;
}
