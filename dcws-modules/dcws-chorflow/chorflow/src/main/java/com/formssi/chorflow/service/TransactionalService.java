package com.formssi.chorflow.service;


import com.formssi.chorflow.domain.flow.HttpRequestVo;

/**
 * kstry 处理服务类
 *
 * @author lijun
 * @date 2025-01-14
 */
public interface TransactionalService {

  /**
   * 处理悬挂/空回滚
   *
   * @param httpResult
   * @param httpRequestVo
   * @return
   */
  void transactionalSuspend(String httpResult, HttpRequestVo httpRequestVo);


  /**
   * 处理分布式事务
   *
   * @param httpResult
   * @param httpRequestVo
   * @return
   */
  void transactional(String httpResult, HttpRequestVo httpRequestVo);

  /**
   * 处理红线事务
   *
   * @param httpResult
   * @param httpRequestVo
   */
  void transactionalRed(String httpResult, HttpRequestVo httpRequestVo);
}