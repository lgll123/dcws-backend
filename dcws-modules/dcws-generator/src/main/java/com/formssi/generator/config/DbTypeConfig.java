package com.formssi.generator.config;

import java.util.Map;

import com.formssi.common.core.utils.SpringUtils;
import com.formssi.generator.config.properties.ConnectConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author 六如
 */
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(ConnectConfigProperties.class)
public class DbTypeConfig {

    private final ConnectConfigProperties connectConfigProperties;

    public static DbTypeConfig getInstance() {
        return SpringUtils.getBean(DbTypeConfig.class);
    }

    public ConnectConfig getConnectConfig(Integer type) {
        return connectConfigProperties.getDbType().get(type);
    }

    public Map<Integer, ConnectConfig> getConnectConfigMap() {
        return connectConfigProperties.getDbType();
    }

    public void setConnectConfigMap(Map<Integer, ConnectConfig> connectConfigMap) {
        this.connectConfigProperties.setDbType(connectConfigMap);
    }
}
