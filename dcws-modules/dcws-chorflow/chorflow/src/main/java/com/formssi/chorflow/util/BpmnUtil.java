package com.formssi.chorflow.util;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.camunda.CamundaPropertiesImpl;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

/**
 * Bpmn 工具类
 *
 * @author lijun
 * @date 2024-12-19
 */
public class BpmnUtil {


  public static BpmnModelInstance getBpmnModelInstance(InputStream inputStream) {
    return Bpmn.readModelFromStream(inputStream);
  }

  public static BpmnModelInstance getBpmnModelInstance(String fileContent) {
    return Bpmn.readModelFromStream(getInputStream(fileContent));
  }

  public static ByteArrayInputStream getInputStream(String fileContent) {
    return new ByteArrayInputStream(fileContent.getBytes());
  }

  public static Collection<CamundaProperty> getCamundaProperties(String fileContent) {
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(fileContent);

    return getCamundaProperties(bpmnModelInstance);
  }

  public static Collection<CamundaProperty> getCamundaProperties(File file) {
    BufferedInputStream inputStream = FileUtil.getInputStream(file);
    BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(inputStream);

    return getCamundaProperties(bpmnModelInstance);
  }


  public static Collection<CamundaProperty> getCamundaProperties(
      BpmnModelInstance bpmnModelInstance) {
    Collection<StartEvent> startEvents = bpmnModelInstance.getModelElementsByType(StartEvent.class);
    if (CollectionUtil.isEmpty(startEvents)) {
      return Collections.emptyList();
    }
    for (StartEvent startEvent : startEvents) {
      ExtensionElements extensionElements = startEvent.getExtensionElements();
      if (extensionElements == null) {
        continue;
      }
      CamundaPropertiesImpl camundaProperties = extensionElements.getElementsQuery()
          .filterByType(CamundaPropertiesImpl.class).singleResult();
      if (camundaProperties != null) {
        return camundaProperties.getCamundaProperties();
      }
    }
    return Collections.emptyList();
  }

}


