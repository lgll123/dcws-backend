package com.formssi.chorflow.db;

import com.formssi.system.service.IDocInfoService;
import com.formssi.system.service.IDocParamService;
import com.formssi.system.service.IModuleService;
import com.formssi.system.service.IProjectService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DbBaseTest {

  protected static final Long PROJECT_ID = 1001L;
  protected static Long moduleId;

  @Autowired
  protected IProjectService iProjectService;

  @Autowired
  protected IModuleService moduleService;

  @Autowired
  protected IDocInfoService docInfoService;

  @Autowired
  protected IDocParamService docParamService;


}
