package com.formssi.ureport2.config;

import com.bstek.ureport.console.UReportServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * 集成ureport2 报表功能
 *
 * @author zhangmiao
 */
@Configuration(proxyBeanMethods = false)
@ImportResource("classpath:ureport-console-context.xml")
public class Ureport2Config {

  /**
   * 配置报表
   */
  @Bean
  public ServletRegistrationBean<UReportServlet> initUReport() {
    return new ServletRegistrationBean<>(new UReportServlet(), "/ureport/*");
  }


}