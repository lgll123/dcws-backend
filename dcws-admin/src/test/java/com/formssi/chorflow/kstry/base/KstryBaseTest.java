package com.formssi.chorflow.kstry.base;

import cn.kstry.framework.core.engine.StoryEngine;
import com.formssi.chorflow.service.impl.ChorFlowServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Kstry 测试父类
 *
 * @author joey
 * @date 2024.11.28
 */
@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class KstryBaseTest {

  @Autowired
  protected StoryEngine storyEngine;
  @Autowired
  protected ChorFlowServiceImpl chorFlowService;

  public static String fileContent;
  public static String startId;
  public static Long id;

  public static final String START_ID_PATH = "//bpmn:definitions/bpmn:process/bpmn:startEvent/@id";
}