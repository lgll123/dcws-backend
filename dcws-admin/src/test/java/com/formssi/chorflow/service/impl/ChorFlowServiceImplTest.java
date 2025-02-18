package com.formssi.chorflow.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.XmlUtil;
import com.formssi.chorflow.domain.ChorFlow;
import com.formssi.chorflow.domain.vo.ChorFlowVo;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import javax.xml.xpath.XPathConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;

/**
 * ChorFlowService 测试类
 *
 * @author lijun
 * @date 2024-12-25
 */
@Slf4j
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChorFlowServiceImplTest {

  @Autowired
  private ChorFlowServiceImpl chorFlowService;

  public static final String START_EVENT_ID = "//bpmn:definitions/bpmn:process/bpmn:startEvent/@id";
  public static String fileContent;
  public static String startId;
  public static Long id;

  @BeforeAll
  static void before() {
    fileContent = FileUtil.readUtf8String("bpmn/httpFlow.bpmn");
    final Document docResult = XmlUtil.parseXml(fileContent);
    final Object value = XmlUtil.getByXPath(START_EVENT_ID, docResult, XPathConstants.STRING);
    log.info("value: {}", value);
    startId = value.toString();
  }

  @Test
  @Order(1)
  void testAdd() {
    // 入库
    ChorFlow chorFlow = new ChorFlow();
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

  @Test
  @Order(2)
  void testSelectPageChorFlowList() {
    ChorFlowVo chorFlowVo = new ChorFlowVo();
    PageQuery pageQuery = new PageQuery();
    TableDataInfo<ChorFlowVo> tableDataInfo = chorFlowService.selectPageChorFlowList(chorFlowVo,
        pageQuery);
    log.info("tableDataInfo: {}", tableDataInfo);
    Assertions.assertNotNull(tableDataInfo);
  }

  @Test
  @Order(3)
  void testGet() {
    ChorFlowVo chorFlowVo = chorFlowService.get(id);
    log.info("chorFlowVo: {}", chorFlowVo);
    Assertions.assertNotNull(chorFlowVo);
    Assertions.assertEquals(id, chorFlowVo.getId());
  }

  @Test
  @Order(4)
  void testDelete() {
    chorFlowService.delete(id);
    ChorFlowVo chorFlowVo = chorFlowService.get(id);
    Assertions.assertNull(chorFlowVo);
  }
 
}