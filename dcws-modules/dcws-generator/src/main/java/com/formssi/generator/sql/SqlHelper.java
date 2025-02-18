package com.formssi.generator.sql;

import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 执行SQL语句的帮助类
 *
 * @author hc.tang
 */
public class SqlHelper {
    private static final Logger logger = LoggerFactory.getLogger(SqlHelper.class);

    /**
     * <pre>
     * String sql = "SELECT * FROM datasource_config WHERE dc_id=${id}";
     *
     * DataSourceConfig dataSourceConfig = new DataSourceConfig();
     * dataSourceConfig.setDriverClass("com.mysql.cj.jdbc.Driver");
     * dataSourceConfig.setJdbcUrl("jdbc:mysql://localhost:3306/auto_code");
     * dataSourceConfig.setUsername("root");
     * dataSourceConfig.setPassword("root");
     *
     * Map<String, Object> params = new HashMap<String, Object>();
     * params.put("id", 2);
     *
     * List<Map<String, Object>> map = SqlHelper.runSql(dataSourceConfig, sql,params);
     * </pre>
     *
     * @param generatorConfig 配置
     * @param sql             sql
     * @param params          参数
     * @return 返回查询结果
     */
    public static List<Map<String, Object>> runSql(GeneratorConfig generatorConfig, String sql,
                                                   Map<String, Object> params) {

        DataSource dataSource = DataSourceManager.getDataSource(generatorConfig);
        String runSql = buildSqlWithParams(dataSource, sql, params);
        String[] sqls = runSql.split(";");
        Connection conn;
        try {
            conn = DataSourceManager.getConnection(generatorConfig);
            SqlRunner runner = buildSqlRunner(conn);
            int sqlCount = sqls.length;
            if (sqlCount == 1) {
                return runner.selectAll(sqls[0]);
            } else {
                for (int i = 0; i < sqlCount - 1; i++) {
                    runner.run(sqls[i]);
                }
                return runner.selectAll(sqls[sqlCount - 1]);
            }
        } catch (SQLException e1) {
            logger.error("生成代码错误", e1);
            throw new RuntimeException("生成代码错误");
        }
    }

    /**
     * 参数绑定
     *
     * @param dataSource 数据源
     * @param sql        sql模板
     * @param params     参数
     * @return 构建好的耍起莱
     */
    private static String buildSqlWithParams(DataSource dataSource, String sql, Map<String, Object> params) {
        Configuration configuration = buildConfiguration(dataSource);
        TextSqlNode node = new TextSqlNode(sql);
        DynamicSqlSource dynamicSqlSource = new DynamicSqlSource(configuration, node);
        BoundSql boundSql = dynamicSqlSource.getBoundSql(params);
        return boundSql.getSql();
    }

    public static List<Map<String, Object>> runSql(GeneratorConfig dataBaseConfig, String sql) {
        return runSql(dataBaseConfig, sql, null);
    }

    private static SqlRunner buildSqlRunner(Connection connection) {
        return new SqlRunner(connection);
    }

    private static Configuration buildConfiguration(DataSource dataSource) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development",
                transactionFactory, dataSource);
        return new Configuration(environment);
    }

}
