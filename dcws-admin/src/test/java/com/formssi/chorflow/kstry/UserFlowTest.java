package com.formssi.chorflow.kstry;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.formssi.chorflow.domain.ChorFlow;
import com.formssi.chorflow.domain.flow.FlowReq;
import com.formssi.chorflow.kstry.base.KstryBaseTest;
import com.formssi.chorflow.service.IChorFlowService;
import com.formssi.common.core.domain.R;
import javax.xml.xpath.XPathConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

@Slf4j
public class UserFlowTest extends KstryBaseTest {

  @Autowired
  private IChorFlowService chorFlowService;

  @BeforeAll
  static void before() {
    fileContent = FileUtil.readUtf8String("bpmn/userFlow.bpmn");
    final Document docResult = XmlUtil.parseXml(fileContent);
    final Object value = XmlUtil.getByXPath(START_ID_PATH, docResult, XPathConstants.STRING);
    log.info("value: {}", value);
    startId = value.toString();
  }

  @Test
  @Order(1)
  void testAddFlow() {
    ChorFlow chorFlow = chorFlowService.getOne(
        new QueryWrapper<ChorFlow>().eq("flow_name", startId));
    if (null == chorFlow) {
      chorFlow = new ChorFlow();
    }
    // 入库
    chorFlow.setFlowName(startId);
    chorFlow.setApiId(1L);
    chorFlow.setFileContent(fileContent);
    chorFlow.setDescription(startId);
    chorFlow.setSystemId("code");
    chorFlow.setModuleId("code");
    chorFlow.setImg("Test");
    // 添加流程
    ChorFlow newChorFlow = chorFlowService.addOrUpdate(chorFlow);
    log.info("newChorFlow: {}", newChorFlow);
    Assertions.assertNotNull(newChorFlow);
    id = newChorFlow.getId();
    Assertions.assertNotNull(id);
  }

  /**
   * 测试全流程
   */
  @Test
  @Order(2)
  void testFlow() {
    String readUtf8String = FileUtil.readUtf8String("json/userFlow.json");
    FlowReq flowReq = JSONUtil.toBean(readUtf8String, FlowReq.class);
    R<Object> objectR = chorFlowService.kstryRequest(flowReq);
    log.info("objectR: {}", objectR);
  }

  /**
   * 测试全流程
   */
  @Test
  @Order(3)
  void testFlowWithWidStartId() {
    String readUtf8String = FileUtil.readUtf8String("json/userFlow.json");
    FlowReq flowReq = JSONUtil.toBean(readUtf8String, FlowReq.class);
    flowReq.setMidwayStartId("getBalance");
    R<Object> objectR = chorFlowService.kstryRequest(flowReq);
    log.info("objectR: {}", objectR);
  }

}
