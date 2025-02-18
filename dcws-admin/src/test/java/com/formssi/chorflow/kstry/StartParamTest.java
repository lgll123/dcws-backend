package com.formssi.chorflow.kstry;


import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.formssi.chorflow.domain.vo.StartEventVo;
import com.formssi.chorflow.util.BpmnUtil;
import java.util.Collection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Kstry 变量测试
 *
 * @author joey
 * @date 2024.12.26
 */
@Slf4j
public class StartParamTest {

  private static String fileContent;

  @BeforeAll
  static void before() {
    fileContent = FileUtil.readUtf8String("bpmn/httpFlow.bpmn");
  }

  @Test
  @Order(1)
  void testGetCamundaProperty() {
    Collection<CamundaProperty> camundaProperties = BpmnUtil.getCamundaProperties(fileContent);
    log.info("camundaProperty size: {}", camundaProperties.size());

    Assertions.assertNotNull(camundaProperties);
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
          List<String> strings = JSONUtil.toList(camundaValue, String.class);
          startEventVo.setIdempotentKeys(strings);
        }
        if (camundaName.equals(StartEventVo.TRANSACTIONAL_SUSPEND)) {
          String camundaValue = property.getCamundaValue();
          startEventVo.setTransactionalSuspend(Boolean.valueOf(camundaValue));
        }
      }
    }
    log.info("startEventVo:{}", startEventVo);
  }
}

