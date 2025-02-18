package com.formssi.chorflow.kstry;

import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import com.formssi.chorflow.kstry.base.KstryBaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

@Slf4j
public class UserComponentTest extends KstryBaseTest {


  /**
   * 测试全流程
   */
  @Test
  void testFlow() {

    for (int i = 0; i < 2; i++) {

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
          .startId("performance001").build();

      Mono<Integer> fireAsync = storyEngine.fireAsync(storyRequest);
      Integer result = fireAsync.block();
      log.info("result:{}", result);

    }
  }

}
