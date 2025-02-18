package com.formssi.generator.config.properties;

import com.formssi.generator.config.ConnectConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/6 13:45
 */
@Component
@ConfigurationProperties(prefix = "connect")
@Data
public class ConnectConfigProperties {

    private Map<Integer, ConnectConfig> dbType;
}
