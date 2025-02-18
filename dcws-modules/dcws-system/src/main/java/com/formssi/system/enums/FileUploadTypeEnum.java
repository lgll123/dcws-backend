package com.formssi.system.enums;

import com.formssi.common.core.exception.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/12/6 11:06
 */
@AllArgsConstructor
@Getter
public enum FileUploadTypeEnum {

    /**
     * oss文件上传
     */
    OSS("oss", "ossFileService"),

    /**
     * minio文件上传
     */
    MINIO("minio", "minioFileService");

    /**
     * 类型
     */
    private final String type;

    /**
     * 实现类名称
     */
    private final String name;

    public static FileUploadTypeEnum getEnumByType(String type) {
        return Arrays.stream(FileUploadTypeEnum.values()).filter(r -> Objects.equals(type, r.type)).findFirst().orElseThrow(() ->
                new ServiceException("文件上传方式暂不支持")
        );
    }

}
