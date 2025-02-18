package com.formssi.dashboard.core.config;

import com.formssi.dashboard.core.constant.DashboardConst.ScanPackage;
import com.formssi.dataset.constant.DatasetConstant;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 加载大屏相关配置
 *
 * @author zhangmiao
 */
@ComponentScan(value = {ScanPackage.COMPONENT, DatasetConstant.ScanPackage.COMPONENT})
@MapperScan(value = {ScanPackage.DAO, DatasetConstant.ScanPackage.DAO})
public class AutoDashboardConfig {

}
