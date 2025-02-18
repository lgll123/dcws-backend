package com.formssi.chorflow.domain.vo;

import com.formssi.chorflow.domain.ChorFlow;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * 流程编排 响应对象
 *
 * @author lijun
 * @date 2024-12-17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@FieldNameConstants
@ToString(callSuper = true)
public class ChorFlowVo extends ChorFlow {

  private String systemName;

  private String moduleName;

}