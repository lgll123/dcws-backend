package com.formssi.chorflow.domain.vo;

import com.formssi.chorflow.domain.SysBussys;
import com.formssi.chorflow.domain.SysBussysModule;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * 应用系统 响应对象
 *
 * @author lijun
 * @date 2024-12-17
 */
@EqualsAndHashCode(callSuper = true)
@Data
@FieldNameConstants
@ToString(callSuper = true)
public class SysBussysVo extends SysBussys {

  private List<SysBussysModule> modules;

}