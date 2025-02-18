package com.formssi.chorflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.chorflow.domain.ChorFlow;
import com.formssi.chorflow.domain.flow.FlowReq;
import com.formssi.chorflow.domain.vo.ChorFlowVo;
import com.formssi.common.core.domain.R;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;

/**
 * 流程编排 服务类
 *
 * @author lijun
 * @date 2024-12-17
 */
public interface IChorFlowService extends IService<ChorFlow> {

  ChorFlow addOrUpdate(ChorFlow chorFlow);

  TableDataInfo<ChorFlowVo> selectPageChorFlowList(ChorFlowVo chorFlow, PageQuery pageQuery);

  void delete(Long id);

  ChorFlowVo get(Long id);

  R<Object> kstryRequest(FlowReq flowReq);
}
