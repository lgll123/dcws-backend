package com.formssi.chorflow.config.kstry;

import cn.kstry.framework.core.annotation.EnableKstry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lijun
 * @date 2024-12-17
 */
@Configuration
@EnableKstry
@Slf4j
public class KstryConfig {

  @PostConstruct
  public void init() {
    log.info("KstryConfig init");
  }

  @Bean
  public DynamicProcessRegister dynamicProcessRegister() {
    return new DynamicProcessRegister();
  }

  @Bean
  public DynamicSubProcessRegister dynamicSubProcessRegister() {
    return new DynamicSubProcessRegister();
  }
 

}