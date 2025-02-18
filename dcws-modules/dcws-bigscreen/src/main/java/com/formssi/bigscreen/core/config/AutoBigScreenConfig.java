package com.formssi.bigscreen.core.config;

import com.formssi.bigscreen.core.constant.DataRoomConst.ScanPackage;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * 加载大屏相关配置
 *
 * @author zhangmiao
 */
@ComponentScan(value = {ScanPackage.COMPONENT})
@MapperScan(value = {ScanPackage.DAO})
public class AutoBigScreenConfig {


}
