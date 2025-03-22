package com.formssi.workflow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 生成申请单PDF到minio/档案系统服务器状态 1-成功 0-失败 2-minio成功 3-档案系统成功 4-待处理
 *
 * @author yqh
 */
@Getter
@AllArgsConstructor
public enum StorageFileStatusEnum {
    /**
     * 失败
     */
    STORAGEFILESTATUS_0("0", "失败"),
    /**
     * 成功
     */
    STORAGEFILESTATUS_1("1", "成功"),
    /**
     * minio成功
     */
    STORAGEFILESTATUS_2("2", "minio成功"),
    /**
     * 档案系统成功
     */
    STORAGEFILESTATUS_3("3", "档案系统成功"),
    /**
     * 待处理
     */
    STORAGEFILESTATUS_4("4", "待处理");

    private final String code;
    private final String desc;
    public static StorageFileStatusEnum of(String type) {
        if (type == null) {
            return null;
        }
        for (StorageFileStatusEnum value : StorageFileStatusEnum.values()) {
            if (Objects.equals(type, value.getCode())) {
                return value;
            }
        }
        return null;
    }
}
