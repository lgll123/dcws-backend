package com.formssi.chorflow.kstry;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.XmlUtil;
import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.formssi.chorflow.domain.ChorFlow;
import com.formssi.chorflow.domain.flow.FlowReq;
import com.formssi.chorflow.kstry.base.KstryBaseTest;
import com.formssi.chorflow.service.IChorFlowService;
import com.formssi.chorflow.util.WebUtil;
import javax.xml.xpath.XPathConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;

/**
 * Kstry 中途节点开始测试
 *
 * @author joey
 * @date 2024.12.26
 */
@Slf4j
public class MidWayTest extends KstryBaseTest {

  @Autowired
  private IChorFlowService chorFlowService;

  @BeforeEach
  void before() {
    String fileContent = FileUtil.readUtf8String("bpmn/midWay.bpmn");
    final Document docResult = XmlUtil.parseXml(fileContent);
    final Object value = XmlUtil.getByXPath(START_ID_PATH, docResult, XPathConstants.STRING);
    log.info("value: {}", value);
    startId = value.toString();

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
  void testRequest() {
    FlowReq flowReq = new FlowReq();
    flowReq.setStartId(startId);

    request(flowReq);
  }


  @Test
  @Order(3)
  void testRequestWithMidGateWayId() {
    FlowReq flowReq = new FlowReq();
    flowReq.setStartId(startId);
    flowReq.setMidwayStartId("InclusiveGateway01");
    request(flowReq);
  }


  @Test
  @Order(4)
  void testRequestWithGetBalance() {
    FlowReq flowReq = new FlowReq();
    flowReq.setStartId(startId);
    flowReq.setMidwayStartId("getBalance");
    request(flowReq);
  }


  @Test
  @Order(5)
  void testRequestWithCost() {
    FlowReq flowReq = new FlowReq();
    flowReq.setStartId(startId);
    flowReq.setMidwayStartId("cost");
    request(flowReq);
  }


  private void request(FlowReq flowReq) {
    StoryRequest<Object> fireRequest = ReqBuilder.returnType(Object.class)
        // 流程结束的回调
        .recallStoryHook(WebUtil::recallStoryHook)
        // 链路追踪级别，未指定时使用全局默认配置的级别
        .trackingType(TrackingTypeEnum.SERVICE_DETAIL)
        // 结果构造器
        .resultBuilder((Object r, ScopeDataQuery query) -> query.getVarScope())
        // 流程上下文
        .startId(flowReq.getStartId()).request(flowReq).build();
    String midwayStartId = flowReq.getMidwayStartId();
    if (StringUtils.isNotBlank(midwayStartId)) {
      fireRequest.setMidwayStartId(midwayStartId);
    }
    Integer timeout = flowReq.getTimeout();
    if (null != timeout) {
      fireRequest.setTimeout(timeout);
    }
    TaskResponse<Object> fire = storyEngine.fire(fireRequest);

    log.info("objectR: {}", fire.getResult());
  }

}

