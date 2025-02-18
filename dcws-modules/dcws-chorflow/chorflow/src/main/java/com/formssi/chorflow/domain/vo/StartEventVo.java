package com.formssi.chorflow.domain.vo;

import java.util.List;
import lombok.Data;

/**
 * 流程编排 响应对象
 *
 * @author lijun
 * @date 2024-12-17
 */
@Data
public class StartEventVo {

  public static final String IDEMPOTENT_KEY = "idempotent";
  public static final String IDEMPOTENT_KEYS = "idempotent-keys";
  public static final String TRANSACTIONAL_SUSPEND = "transactional-suspend";
  /**
   * 是否开启幂等
   */
  private Boolean idempotent = false;

  /**
   * json格式数组，如["data.id","data.name"]
   */
  private List<String> idempotentKeys;

  /**
   * 是否处理悬挂/空回滚
   */
  private Boolean transactionalSuspend = false;

}