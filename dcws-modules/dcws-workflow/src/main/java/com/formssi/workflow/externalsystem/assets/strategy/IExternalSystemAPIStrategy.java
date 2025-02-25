package com.formssi.workflow.externalsystem.assets.strategy;

import java.util.Map;

/**
 * 外部系统接口策略
 *
 * @author yqh
 */
public interface IExternalSystemAPIStrategy {
    public static String BASE_NAME = "APIStrategy";
    Map<String, Object> process(Map<String, String> params, String pathUrl,String requestType);

}
