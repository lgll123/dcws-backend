package com.formssi.generator.sql;

import com.formssi.generator.config.ConnectConfig;
import com.formssi.generator.config.DbTypeConfig;
import com.formssi.generator.domain.DatasourceConfig;
import jodd.util.StringUtil;

public class GeneratorConfig {

    private Integer dbType;
    /** 数据库名称 */
    private String dbName;
    /** schema(PGSQL专用) */
    private String schemaName;
    /**
     * 驱动class
     */
    private String driverClass;
    /** 数据库host */
    private String host;
    /** 数据库端口 */
    private Integer port;
    /** 数据库用户名 */
    private String username;
    /** 数据库密码 */
    private String password;


    public static GeneratorConfig build(DatasourceConfig datasourceConfig) {
        GeneratorConfig generatorConfig = new GeneratorConfig();
        generatorConfig.setDbName(datasourceConfig.getDbName());
        generatorConfig.setDbType(datasourceConfig.getDbType());
        generatorConfig.setHost(datasourceConfig.getHost());
        generatorConfig.setPort(datasourceConfig.getPort());
        generatorConfig.setUsername(datasourceConfig.getUsername());
        generatorConfig.setPassword(datasourceConfig.getPassword());
        generatorConfig.setSchemaName(datasourceConfig.getSchemaName());
        String driver = datasourceConfig.getDriverClass();
        if (StringUtil.isEmpty(driver)) {
            ConnectConfig connectConfig = DbTypeConfig.getInstance().getConnectConfig(datasourceConfig.getDbType());
            driver = connectConfig.getDriver();
        }
        generatorConfig.setDriverClass(driver);
        return generatorConfig;
    }

    public String getJdbcUrl() {
        ConnectConfig connectConfig = DbTypeConfig.getInstance().getConnectConfig(dbType);
        if (dbType == null) {
            throw new RuntimeException("不支持数据库类型" + this.dbType + "，请在 app.yml 中配置");
        }
        String jdbcUrl = connectConfig.getUrl();
        // jdbc:mysql://{HOST}:{PORT}/{DB_NAME}?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
        jdbcUrl = jdbcUrl.replace("{HOST}", host);
        jdbcUrl = jdbcUrl.replace("{PORT}", String.valueOf(port));
        jdbcUrl = jdbcUrl.replace("{DB_NAME}", dbName);
        return jdbcUrl;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public Integer getDbType() {
        return dbType;
    }

    public void setDbType(Integer dbType) {
        this.dbType = dbType;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getDriverClass() {
        return driverClass;
    }
}
