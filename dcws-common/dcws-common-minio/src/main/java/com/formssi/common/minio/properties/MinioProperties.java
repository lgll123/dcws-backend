package com.formssi.common.minio.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * minio属性类
 * @author lizhangyu
 * @version 1.0
 * @date 2024/12/5 15:13
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * 端点
     */
    private String endpoint;

    /**
     * 访问key
     */
    private String accessKey;

    /**
     * 秘钥key
     */
    private String secretKey;

    /**
     * 桶名称
     */
    private String bucketName;

}
