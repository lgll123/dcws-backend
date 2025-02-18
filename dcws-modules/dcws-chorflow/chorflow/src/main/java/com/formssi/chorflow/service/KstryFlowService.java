package com.formssi.chorflow.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import cn.kstry.framework.core.annotation.TaskComponent;
import cn.kstry.framework.core.annotation.TaskInstruct;
import cn.kstry.framework.core.annotation.TaskParam;
import cn.kstry.framework.core.annotation.TaskService;
import cn.kstry.framework.core.bus.ScopeDataOperator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.formssi.chorflow.config.ApacheHttpClient5Helper;
import com.formssi.chorflow.domain.constant.KstryConstants;
import com.formssi.chorflow.domain.exception.FlowException;
import com.formssi.chorflow.domain.flow.HttpActionVo;
import com.formssi.chorflow.domain.flow.HttpRequestVo;
import com.formssi.chorflow.domain.flow.ResultConfig;
import com.formssi.chorflow.domain.flow.VarSetVo;
import com.formssi.chorflow.domain.vo.StartEventVo;
import com.formssi.common.json.utils.JsonUtils;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * kstry 处理服务类
 *
 * @author lijun
 * @date 2025-01-14
 */
@Slf4j
@TaskComponent(name = "KstryFlowService")
public class KstryFlowService {

  @Autowired
  private ApacheHttpClient5Helper apacheHttpClient5Helper;

  @Autowired(required = false)
  private TransactionalService transactionalService;

  @TaskInstruct(name = "rest-service")
  @TaskService(name = "rest-service", desc = "服务请求")
  public void request(ScopeDataOperator dataOperator,
      @TaskParam(HttpActionVo.Fields.request) HttpRequestVo request,
      @TaskParam(HttpActionVo.Fields.config) ResultConfig config) {
    log.info("服务请求,request: {}", JsonUtils.toJsonString(request));

    // 处理起始节点
    String httpResult;
    String url = request.getUrl();
    String method = request.getMethod();
    Object body = request.getBody();
    Map<String, Object> urlParams = request.getParams();
    Map<String, String> headers = request.getHeaders();
    try {
      httpResult = apacheHttpClient5Helper.httpRequest(url, method, JsonUtils.toJsonString(body),
          urlParams, headers);
    } catch (Exception e) {
      log.error("httpRequest error", e);
      throw new FlowException(e.getMessage(), e);
    }
    if (StringUtils.isBlank(httpResult)) {
      return;
    }

    // 处理http请求错误
    handleError(httpResult, request);

    StartEventVo startEventVo = KstryConstants.START_EVENT_MAP.get(dataOperator.getStartId());
    log.info("get startEventVo: {}", startEventVo);
    if (null != startEventVo) {
      // 处理幂等 todo
      Boolean idempotent = startEventVo.getIdempotent();
      if (idempotent) {

        List<String> idempotentKeys = startEventVo.getIdempotentKeys();
        log.info("处理幂等, idempotentKeys: {}", JsonUtils.toJsonString(idempotentKeys));

      }
    }
    if (null != startEventVo) {
      Boolean transactionalSuspend = startEventVo.getTransactionalSuspend();
      if (transactionalSuspend) {
        // 处理悬挂/空回滚 todo
        log.info("处理悬挂/空回滚");
        transactionalService.transactionalSuspend(httpResult, request);
      }
    }

    Map<String, Object> response = JsonUtils.parseObject(httpResult, new TypeReference<>() {
    });

    // 是否处理分布式事务
    if (config.getTransactional()) {
      transactionalService.transactional(httpResult, request);
    }

    // 是否红线
    if (config.getTransactionalRed()) {
      transactionalService.transactionalRed(httpResult, request);
    }

    // 获取任务节点的id，并设置到var域中
    String id = config.getId();
    Validate.notEmpty(id);

    VarSetVo varSetVo = new VarSetVo();
    varSetVo.setRequest(request);
    varSetVo.setResponse(response);
    // 设置值到Var域
    dataOperator.setVarData(id, BeanUtil.beanToMap(varSetVo));
  }

  /**
   * 处理http请求错误
   *
   * @param httpResult    http请求结果
   * @param httpRequestVo http请求参数
   */
  private void handleError(String httpResult, HttpRequestVo httpRequestVo) {
    String successCode = httpRequestVo.getSuccessCode();
    String successJsonPath = httpRequestVo.getSuccessJsonPath();
    String errorMessageJsonPath = httpRequestVo.getErrorMessageJsonPath();
    if (StrUtil.isAllNotBlank(successCode, successJsonPath)) {
      JSON parse = JSONUtil.parse(httpResult);
      Object successValue = parse.getByPath(successJsonPath);
      if (null == successValue) {
        return;
      }
      if (!successValue.toString().equals(successCode)) {
        // 发生异常
        String error = StrUtil.format(
            "http请求失败,requestId:{}, response: {}" + JsonUtils.toJsonString(httpRequestVo),
            httpResult);
        log.error(error);

        Object errorMessage = parse.getByPath(errorMessageJsonPath);
        if (null != errorMessage) {
          throw new FlowException(errorMessage.toString());
        }
        throw new FlowException(error);
      }
    }
  }


  @TaskInstruct(name = "distributed-transaction")
  @TaskService(name = "distributed-transaction", desc = "分布式事务处理")
  public void distributedTransaction(ScopeDataOperator dataOperator,
      @TaskParam(HttpActionVo.Fields.request) HttpRequestVo request) {
    log.info("处理分布式事务,request: {}", JsonUtils.toJsonString(request));
    // todo
  }


}
