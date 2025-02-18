package com.formssi.chorflow.domain.constant;

import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import com.formssi.chorflow.domain.vo.StartEventVo;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * kstry 常量
 *
 * @author lijun
 * @date 2024-12-24
 */
public class KstryConstants {


  public static final ThreadLocal<List<SubProcessLink>> KSTRY_SUB_LIST = new ThreadLocal<>();

  public static final Map<String, StartEventVo> START_EVENT_MAP = new ConcurrentHashMap<>();

}
