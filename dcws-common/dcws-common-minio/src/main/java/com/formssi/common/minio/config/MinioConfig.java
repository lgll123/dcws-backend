package com.formssi.common.minio.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.formssi.common.minio.properties.MinioProperties;

/**
 * minio配置类
 * @author lizhangyu
 * @version 1.0
 * @date 2024/12/5 15:16
 */
@Component
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Autowired
    private MinioProperties minioProperties;

    /**
     * 创建 MinIO 客户端
     */
    @Bean
    public MinioClient getMinioClient() {
        return MinioClient.builder().endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

}
