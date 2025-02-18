package com.formssi.chorflow.kstry;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.XmlUtil;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.formssi.chorflow.domain.ChorFlow;
import com.formssi.chorflow.domain.flow.FlowReq;
import com.formssi.chorflow.kstry.base.KstryBaseTest;
import com.formssi.chorflow.service.RetryComponent;
import com.formssi.chorflow.util.WebUtil;
import javax.xml.xpath.XPathConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

/**
 * Kstry 重试测试
 *
 * @author joey
 * @date 2024.12.26
 */
@Slf4j
public class RetryTest extends KstryBaseTest {

  @BeforeAll
  static void before() {
    fileContent = FileUtil.readUtf8String("bpmn/retry.bpmn");
    final Document docResult = XmlUtil.parseXml(fileContent);
    final Object value = XmlUtil.getByXPath(START_ID_PATH, docResult, XPathConstants.STRING);
    log.info("value: {}", value);
    startId = value.toString();
  }

  @Test
  @Order(1)
  void testAddFlow() {
    ChorFlow one = chorFlowService.getOne(new QueryWrapper<ChorFlow>().eq("flow_name", startId));
    if (null != one) {
      chorFlowService.removeById(one.getId());
    }
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
  void testRetry() {
    FlowReq flowReq = new FlowReq();
    flowReq.setStartId(startId);
    TaskResponse<Object> taskResponse = kstryRequest(flowReq);
    Assertions.assertNotNull(taskResponse);
    Assertions.assertTrue(taskResponse.isSuccess());
    // 断言总共试了8次
    log.info("taskResponse: {}", taskResponse);
    Assertions.assertEquals(8, RetryComponent.atomicInteger.get());
    log.info("atomicInteger: {}", RetryComponent.atomicInteger);
  }

  /**
   * @param flowReq
   * @return
   */
  public TaskResponse<Object> kstryRequest(FlowReq flowReq) {
    StoryRequest<Object> fireRequest = ReqBuilder.returnType(Object.class)
        // 流程结束的回调
        .recallStoryHook(WebUtil::recallStoryHook)
        // 链路追踪级别，未指定时使用全局默认配置的级别
        .trackingType(TrackingTypeEnum.SERVICE_DETAIL)
        // 流程超时时间，未指定时使用全局默认配置的超时时间
        .timeout(10000)
        // 流程上下文
        .startId(flowReq.getStartId())
        // 请求对象
        .request(flowReq).build();
    String midwayStartId = flowReq.getMidwayStartId();
    if (StringUtils.isNotBlank(midwayStartId)) {
      fireRequest.setMidwayStartId(midwayStartId);
    }
    return storyEngine.fire(fireRequest);
  }

}
