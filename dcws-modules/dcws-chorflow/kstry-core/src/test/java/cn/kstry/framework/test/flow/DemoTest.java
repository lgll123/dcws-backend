package cn.kstry.framework.test.flow;

import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author lykan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Slf4j
@ContextConfiguration(classes = DemoConfiguration.class)
public class DemoTest {

  @Autowired
  private StoryEngine storyEngine;

  /**
   * 测试中途启动节点
   */
  @Test
  public void testMidFlowWithInclusiveGateway() {
    StoryRequest<Object> fireRequest = ReqBuilder.returnType(Object.class)
        .trackingType(TrackingTypeEnum.SERVICE).startId("midFlow")
        .midwayStartId("InclusiveGateway1")
        .build();
    TaskResponse<Object> fire = storyEngine.fire(fireRequest);
    Assert.assertTrue(fire.isSuccess());
    log.info("fire.getResult() = {}", fire.getResult());
  }


  /**
   * 测试中途启动节点
   */
  @Test
  public void testMidFlowWithServiceTask() {
    StoryRequest<Object> fireRequest = ReqBuilder.returnType(Object.class)
        .trackingType(TrackingTypeEnum.SERVICE).startId("midFlow")
        .midwayStartId("node4")
        .build();
    TaskResponse<Object> fire = storyEngine.fire(fireRequest);
    Assert.assertTrue(fire.isSuccess());
    log.info("fire.getResult() = {}", fire.getResult());
  }
}