package com.formssi.chorflow.service.impl;

import cn.kstry.framework.core.bus.ScopeDataQuery;
import cn.kstry.framework.core.engine.StoryEngine;
import cn.kstry.framework.core.engine.facade.ReqBuilder;
import cn.kstry.framework.core.engine.facade.StoryRequest;
import cn.kstry.framework.core.engine.facade.TaskResponse;
import cn.kstry.framework.core.enums.TrackingTypeEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.chorflow.domain.ChorFlow;
import com.formssi.chorflow.domain.constant.KstryConstants;
import com.formssi.chorflow.domain.exception.FlowException;
import com.formssi.chorflow.domain.flow.FlowReq;
import com.formssi.chorflow.domain.flow.FlowReq.Fields;
import com.formssi.chorflow.domain.flow.FlowResponse;
import com.formssi.chorflow.domain.vo.ChorFlowVo;
import com.formssi.chorflow.mapper.ChorFlowMapper;
import com.formssi.chorflow.service.IChorFlowService;
import com.formssi.chorflow.util.WebUtil;
import com.formssi.common.core.domain.R;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import jakarta.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 流程编排 实现类
 *
 * @author lijun
 * @date 2024-12-17
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChorFlowServiceImpl extends ServiceImpl<ChorFlowMapper, ChorFlow> implements
    IChorFlowService {

  @Resource
  private StoryEngine storyEngine;


  @Override
  public ChorFlow addOrUpdate(ChorFlow chorFlow) {
    ChorFlow newChorFlow = new ChorFlow();
    newChorFlow.setFlowName(chorFlow.getFlowName());
    newChorFlow.setApiId(chorFlow.getApiId());
    newChorFlow.setFileContent(chorFlow.getFileContent());
    newChorFlow.setDescription(chorFlow.getDescription());
    newChorFlow.setSystemId(chorFlow.getSystemId());
    newChorFlow.setModuleId(chorFlow.getModuleId());
    newChorFlow.setImg(chorFlow.getImg());
    Long id = chorFlow.getId();
    if (null != id) {
      newChorFlow.setId(id);
    }
    // 添加流程
    baseMapper.insertOrUpdate(newChorFlow);
    return newChorFlow;
  }

  @Override
  public TableDataInfo<ChorFlowVo> selectPageChorFlowList(ChorFlowVo chorFlow,
      PageQuery pageQuery) {
    Page<ChorFlowVo> page = baseMapper.selectPageChorFlowList(pageQuery.build(),
        this.buildQueryWrapper(chorFlow));
    return TableDataInfo.build(page);
  }

  @Override
  public void delete(Long id) {
    ChorFlow chorFlow = baseMapper.selectById(id);
    if (null == chorFlow) {
      throw new FlowException("流程不存在");
    }
    KstryConstants.START_EVENT_MAP.remove(chorFlow.getFlowName());
    baseMapper.deleteById(id);
  }

  @Override
  public ChorFlowVo get(Long id) {
    QueryWrapper<ChorFlowVo> wrapper = Wrappers.query();
    wrapper.eq("cf.id", id);
    List<ChorFlowVo> chorFlowVos = baseMapper.selectPageChorFlowList(wrapper);
    if (CollectionUtils.isEmpty(chorFlowVos)) {
      return null;
    }
    return chorFlowVos.get(0);
  }


  @Override
  public R<Object> kstryRequest(FlowReq flowReq) {
    try {
      StoryRequest<Object> fireRequest = ReqBuilder.returnType(Object.class)
          // 流程结束的回调
          .recallStoryHook(WebUtil::recallStoryHook)
          // 链路追踪级别，未指定时使用全局默认配置的级别
          .trackingType(TrackingTypeEnum.SERVICE_DETAIL)
          // 结果构造器
          .resultBuilder((Object r, ScopeDataQuery query) -> resultBuild(query))
          // 流程上下文
          .startId(flowReq.getStartId()).request(flowReq).build();
      String midwayStartId = flowReq.getMidwayStartId();
      if (StringUtils.isNotBlank(midwayStartId)) {
        fireRequest.setMidwayStartId(midwayStartId);
      }
      Integer timeout = flowReq.getTimeout();
      if (null != timeout) {
        fireRequest.setTimeout(timeout);
      }
      TaskResponse<Object> fire = storyEngine.fire(fireRequest);

      return fire.isSuccess() ? R.ok(fire.getResult())
          : R.fail(fire.getResultException().getMessage());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      return R.fail(e.getMessage());
    } finally {
      KstryConstants.KSTRY_SUB_LIST.remove();
    }
  }

  // ===============================================================

  /**
   * 结果构造
   *
   * @param scopeDataQuery
   * @return
   */
  private static Map<String, Object> resultBuild(ScopeDataQuery scopeDataQuery) {
    Map<String, Object> map = new LinkedHashMap<>();
    // 设置返回值
    Optional<Set<FlowResponse>> optionalFlowResponseSet = scopeDataQuery.getReqData(
        Fields.responses);
    if (optionalFlowResponseSet.isPresent()) {
      Set<FlowResponse> flowResponseSet = optionalFlowResponseSet.get();
      if (CollectionUtils.isNotEmpty(flowResponseSet)) {
        for (FlowResponse flowResponse : flowResponseSet) {
          String responsePath = flowResponse.getPath();
          Optional<Object> data;
          try {
            data = scopeDataQuery.getData(responsePath);
            if (data.isPresent()) {
              Object result = data.get();
              if (result.getClass() != Object.class) {
                map.put(flowResponse.getKey(), result);
                log.info("get result success ,result: {}", result);
              }
            }
          } catch (Exception e) {
            log.error("get result error", e);
          }
        }
      }
    }
    return map;
  }

  /**
   * 构建查询条件
   *
   * @param chorFlow 查询条件对象
   * @return
   */
  private QueryWrapper<ChorFlowVo> buildQueryWrapper(ChorFlow chorFlow) {
    QueryWrapper<ChorFlowVo> wrapper = Wrappers.query();
    wrapper.eq("cf.deleted", 0).eq(null != chorFlow.getApiId(), "cf.api_id", chorFlow.getApiId())
        .like(StringUtils.isNotBlank(chorFlow.getFlowName()), "cf.flow_name",
            chorFlow.getFlowName())
        .like(StringUtils.isNotBlank(chorFlow.getDescription()), "cf.description",
            chorFlow.getDescription())
        .like(StringUtils.isNotBlank(chorFlow.getFileContent()), "cf.file_content",
            chorFlow.getFileContent())
        .eq(null != chorFlow.getModuleId(), "cf.module_id", chorFlow.getModuleId())
        .eq(null != chorFlow.getSystemId(), "cf.system_id", chorFlow.getSystemId());
    wrapper.orderByDesc("cf.update_time");
    return wrapper;
  }
}
