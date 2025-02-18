package com.formssi.chorflow.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.chorflow.domain.ChorFlow;
import com.formssi.chorflow.domain.vo.ChorFlowVo;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * @author lijun
 * @date 2024-12-17
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface ChorFlowMapper extends BaseMapper<ChorFlow> {


  Page<ChorFlowVo> selectPageChorFlowList(IPage<ChorFlowVo> page,
      @Param(Constants.WRAPPER) Wrapper<ChorFlowVo> queryWrapper);

  List<ChorFlowVo> selectPageChorFlowList(
      @Param(Constants.WRAPPER) Wrapper<ChorFlowVo> queryWrapper);
}