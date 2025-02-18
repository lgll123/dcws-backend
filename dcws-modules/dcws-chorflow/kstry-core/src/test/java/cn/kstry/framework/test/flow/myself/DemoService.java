package cn.kstry.framework.test.flow.myself;

import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;

@TaskComponent(name = "DemoService")
@SuppressWarnings("unused")
@Slf4j
public class DemoService {

  @TaskService(name = "node1")
  public void node1(ScopeDataOperator dataOperator) {
    log.info("node1===================================");
  }

  @TaskService(name = "node2")
  public void node2(ScopeDataOperator dataOperator) {
    log.info("node2===================================");
  }

  @TaskService(name = "node3")
  public void node3(ScopeDataOperator dataOperator) {
    log.info("node3===================================");
  }

  @TaskService(name = "node4")
  public void node4(ScopeDataOperator dataOperator) {
    log.info("node4===================================");
    dataOperator.setResult(LocalDateTime.now().toString());
  }


}
