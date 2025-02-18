package com.formssi.generator.sql;

import com.formssi.generator.config.ConnectConfig;
import com.formssi.generator.config.DbTypeConfig;
import com.formssi.generator.util.ClassUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQLServiceFactory {


    private static final Map<Integer, SQLService> SERVICE_CONFIG = new ConcurrentHashMap<>(16);

    public static SQLService build(GeneratorConfig generatorConfig) {
        Integer dbType = generatorConfig.getDbType();
        return SERVICE_CONFIG.computeIfAbsent(dbType, k -> {
            ConnectConfig connectConfig = DbTypeConfig.getInstance().getConnectConfig(dbType);
            String className = connectConfig.getServiceName();
            Class<?> aClass = ClassUtil.loadClass(className);
            if (aClass == null) {
                throw new RuntimeException("找不到数据库服务类:" + className);
            }
            return ClassUtil.newInstance(aClass);
        });
    }

}
