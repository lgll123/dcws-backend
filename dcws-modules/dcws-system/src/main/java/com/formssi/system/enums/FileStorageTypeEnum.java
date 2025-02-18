package com.formssi.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/12/6 14:29
 */
@AllArgsConstructor
@Getter
public enum FileStorageTypeEnum {

    /**
     * 上传到服务器
     */
    SERVER(0, "上传到服务器"),

    /**
     * 失效
     */
    CLOUD_SERVER(1, "上传到云服务oss");

    /**
     * 状态
     */
    private final Integer type;

    /**
     * 描述
     */
    private final String desc;
}
