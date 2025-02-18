package com.formssi.ureport2.datesource;

import com.bstek.ureport.definition.datasource.BuildinDatasource;
import jakarta.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;
import org.springframework.stereotype.Component;

/**
 * 默认数据源
 *
 * @author zhangmiao
 */
@Component()
public class MyDatasource implements BuildinDatasource {

  @Resource
  private DataSource dataSource;

  @Override
  public String name() {
    return "MyDatasource";
  }

  @Override
  public Connection getConnection() {
    try {
      return dataSource.getConnection();
    } catch (SQLException e) {
      return null;
    }
  }
}

