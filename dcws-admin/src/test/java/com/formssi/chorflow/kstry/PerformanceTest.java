package com.formssi.chorflow.kstry;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.XmlUtil;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.formssi.chorflow.config.ExecutionTimeAOP;
import com.formssi.chorflow.domain.ChorFlow;
import com.formssi.chorflow.kstry.base.KstryBaseTest;
import java.util.Map;
import javax.xml.xpath.XPathConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.w3c.dom.Document;

/**
 * 性能测试，运行时间测试
 *
 * @author joey
 * @date 2024.12.27
 */
@Slf4j
@Import(ExecutionTimeAOP.class)
public class PerformanceTest extends KstryBaseTest {

  @BeforeAll
  static void before() {
    fileContent = FileUtil.readUtf8String("bpmn/performance.bpmn");
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

  /**
   * 测试全流程
   */
  @Test
  void testFlow() {

    for (int i = 0; i < 3; i++) {

      StoryRequest<Integer> storyRequest = ReqBuilder
          // 指定返回类型
          .returnType(Integer.class)
          // 流程结束的回溯
          // .recallStoryHook(WebUtil::recallStoryHook)
          // 指定监控类型
          .trackingType(TrackingTypeEnum.SERVICE_DETAIL)
          // 指定节点开始
          // .midwayStartId(MidwayStartComponent.MIDWAY_START_ID)
          // 指定req域参数
          // .request(userScopeData)
          // 指定开始事件方法
          // .startProcess(ProcessConfig::userProcess)
          // 指定var域数据载体，可不指定使用默认值
          // .varScopeData(inScopeData)
          // 任务超时时间，为空时使用全局默认超时时间，单位 ms
          // .timeout(5000)
          // 指定开始事件ID
          .startId(startId).build();

      TaskResponse<Integer> fire = storyEngine.fire(storyRequest);
      Integer result = fire.getResult();
      log.info("result:{}", result);
      Map<String, Long> executionTimeMap = ExecutionTimeAOP.executionTimeMap;
      executionTimeMap.forEach((k, v) -> {
        log.info("{} 方法执行时间为: {} μs", k, v);
      });
    }
  }

}
