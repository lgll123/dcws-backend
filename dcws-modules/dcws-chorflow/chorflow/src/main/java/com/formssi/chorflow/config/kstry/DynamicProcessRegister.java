package com.formssi.chorflow.config.kstry;

import cn.kstry.framework.core.component.bpmn.BpmnProcessParser;
import cn.kstry.framework.core.component.bpmn.builder.SubProcessLink;
import cn.kstry.framework.core.component.bpmn.link.ProcessLink;
import cn.kstry.framework.core.component.dynamic.creator.DynamicProcess;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.formssi.chorflow.domain.ChorFlow;
import com.formssi.chorflow.domain.constant.KstryConstants;
import com.formssi.chorflow.domain.vo.StartEventVo;
import com.formssi.chorflow.util.BpmnUtil;
import com.formssi.common.json.utils.JsonUtils;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.springframework.beans.factory.annotation.Value;

/**
 * 动态流程注册
 *
 * @author lijun
 * @date 2024-12-17
 */
@Slf4j
public class DynamicProcessRegister implements DynamicProcess {

  @Value("${kstry.version:-1}")
  private Long version;

  /**
   * 传入startId返回当前流程的版本号，版本号没有变化并且流程缓存没有失效时，框架不会调用getProcessLink(String
   * startId)来获取新的流程，反之则会重新获取新的流程。流程缓存的时长是1天。修改流程后控制该方法进行版本号升级，新的流程会即刻生效。如果想要删除某个流程，升级版本号后getProcessLink(String
   * startId)返回空即可实现 如果不需要缓存，每次都要获取最新的流程，将version(String key)的返回值设置成小于0的值即可
   *
   * @param startId 默认：startEventId
   * @return
   */
  @Override
  public long version(String startId) {
    return version;
  }

  @Override
  public Optional<ProcessLink> getProcessLink(String startId) {
    log.info("动态获取流程, startId:{}", startId);

    if (StringUtils.isBlank(startId)) {
      return Optional.empty();
    }
    Optional<ChorFlow> optionalChorFlow = new LambdaQueryChainWrapper<>(ChorFlow.class).eq(
        ChorFlow::getFlowName, startId).oneOpt();
    if (optionalChorFlow.isPresent()) {
      String fileContent = optionalChorFlow.map(ChorFlow::getFileContent).orElse(null);
      if (StringUtils.isBlank(fileContent)) {
        return Optional.empty();
      }
      BpmnProcessParser parser = new BpmnProcessParser(startId, fileContent);

      Collection<CamundaProperty> camundaProperties = BpmnUtil.getCamundaProperties(fileContent);
      StartEventVo startEventVo = new StartEventVo();

      if (CollectionUtils.isNotEmpty(camundaProperties)) {
        for (CamundaProperty property : camundaProperties) {
          String camundaName = property.getCamundaName();
          if (StringUtils.isBlank(camundaName)) {
            continue;
          }
          if (camundaName.equals(StartEventVo.IDEMPOTENT_KEY)) {
            String camundaValue = property.getCamundaValue();
            startEventVo.setIdempotent(Boolean.valueOf(camundaValue));
          } else if (camundaName.equals(StartEventVo.IDEMPOTENT_KEYS)) {
            String camundaValue = property.getCamundaValue();
            List<String> strings = JsonUtils.parseArray(camundaValue, String.class);
            startEventVo.setIdempotentKeys(strings);
          }
          if (camundaName.equals(StartEventVo.TRANSACTIONAL_SUSPEND)) {
            String camundaValue = property.getCamundaValue();
            startEventVo.setTransactionalSuspend(Boolean.valueOf(camundaValue));
          }
        }
      }
      log.info("parse startEventVo : {}", JsonUtils.toJsonString(startEventVo));
      KstryConstants.START_EVENT_MAP.put(startId, startEventVo);

      // 解析子流程
      List<SubProcessLink> parseSubProcessLinks = Lists.newArrayList(
          parser.getAllSubProcessLink().values());
      log.info("解析子流程 subProcessLinks size : {}", parseSubProcessLinks.size());

      KstryConstants.KSTRY_SUB_LIST.set(parseSubProcessLinks);

      return parser.getProcessLink(startId);
    }
    return Optional.empty();
  }
}