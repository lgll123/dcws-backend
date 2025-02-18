package com.formssi.chorflow.config.kstry;

import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.dynamic.creator.DynamicSubProcess;
import com.formssi.chorflow.domain.constant.KstryConstants;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 动态流程子注册
 *
 * @author lijun
 * @date 2024-12-17
 */
@Slf4j
public class DynamicSubProcessRegister implements DynamicSubProcess {


  @Override
  public List<SubProcessLink> getSubProcessLinks() {
    log.info("动态获取子流程");
    List<SubProcessLink> subProcessLinks = KstryConstants.KSTRY_SUB_LIST.get();
    if (null == subProcessLinks) {
      log.info("subProcessLinks is null");
    } else {
      log.info("subProcessLinks size : {}", subProcessLinks.size());
    }
    return subProcessLinks;
  }
}