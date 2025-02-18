package com.formssi.workflow.listener;

import com.formssi.common.core.domain.event.ProcessEvent;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.workflow.common.CommonField;
import com.formssi.workflow.utils.WorkflowUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 公用事件流程key必须是（业务表名）执行
 * 唯一id，字段必须是id
 * 流程状态，字段必须是status
 *
 * @author zhangmiao
 */
@Slf4j
@Component
public class CommonEventListener {

  @Resource
  private JdbcTemplate jdbcTemplate;

  @EventListener()
  public void processHandler(ProcessEvent processEvent) {
    CommonField commonField = WorkflowUtils.getCommonField(processEvent.getKey());
    if (commonField != null) {
      log.info("执行公用事件，总体流程监听：{}", processEvent);
      String status = processEvent.getStatus();
      if (processEvent.isSubmit()) {
        status = BusinessStatusEnum.WAITING.getStatus();
      }
      String id = processEvent.getBusinessKey();
      String sql = String.format("update %s set %s = '%s' where %s = '%s'",
          commonField.getTable(), commonField.getStatus(), status, commonField.getPrimaryKey(), id);
      log.info("执行sql：{}", sql);
      try {
        jdbcTemplate.execute(sql);
      } catch (Exception e) {
        log.warn("执行公用事件sql报错，请检查执行sql");
      }
    } else {
      log.info("流程: {} 未启动表单状态监听", processEvent.getKey());
    }
  }

  public void init() {
    log.info("公用事件init");
  }

}