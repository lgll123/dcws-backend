package com.formssi.ureport2.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis相关配置
 *
 * @author zhangmiao
 */
@Configuration
@EnableTransactionManagement
@MapperScan({"com.formssi.ureport2.mapper"})
public class MyBatisConfig {

}
