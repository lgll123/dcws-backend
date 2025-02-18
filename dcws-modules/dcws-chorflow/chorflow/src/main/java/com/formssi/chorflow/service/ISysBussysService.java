package com.formssi.chorflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.chorflow.domain.SysBussys;
import com.formssi.chorflow.domain.vo.SysBussysVo;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import java.util.List;

/**
 * <p>
 * 应用系统表 服务类
 * </p>
 *
 * @author lijun
 */
public interface ISysBussysService extends IService<SysBussys> {

  List<SysBussysVo> tree();

  TableDataInfo<SysBussys> selectPageChorFlowList(SysBussys sysBussys, PageQuery pageQuery);
}
