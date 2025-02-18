package com.formssi.chorflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.chorflow.domain.SysBussys;
import com.formssi.chorflow.domain.SysBussysModule;
import com.formssi.chorflow.domain.vo.SysBussysVo;
import com.formssi.chorflow.mapper.SysBussysMapper;
import com.formssi.chorflow.service.ISysBussysModuleService;
import com.formssi.chorflow.service.ISysBussysService;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 应用系统表 服务实现类
 * </p>
 *
 * @author lijun
 * @since 2024-12-19
 */
@Service
public class SysBussysServiceImpl extends ServiceImpl<SysBussysMapper, SysBussys> implements
    ISysBussysService {

  @Autowired
  private ISysBussysModuleService sysBussysModuleService;

  @Override
  public List<SysBussysVo> tree() {
    List<SysBussysModule> moduleList = sysBussysModuleService.list();
    List<SysBussys> sysBussysList = this.list();
    List<SysBussysVo> sysBussysVoList = new ArrayList<>();

    if (CollectionUtils.isEmpty(sysBussysList)) {
      return sysBussysVoList;
    }
    for (SysBussys sysBussys : sysBussysList) {
      SysBussysVo sysBussysVo = new SysBussysVo();
      sysBussysVo.setSystemCode(sysBussys.getSystemCode());
      sysBussysVo.setSystemName(sysBussys.getSystemName());
      sysBussysVo.setDescription(sysBussys.getDescription());
      sysBussysVo.setCreateTime(sysBussys.getCreateTime());
      sysBussysVo.setCreateBy(sysBussys.getCreateBy());
      sysBussysVo.setUpdateTime(sysBussys.getUpdateTime());
      sysBussysVo.setUpdateBy(sysBussys.getUpdateBy());
      sysBussysVo.setDeleted(sysBussys.getDeleted());
      List<SysBussysModule> modules = new ArrayList<>();
      for (SysBussysModule module : moduleList) {
        if (module.getSystemCode().equals(sysBussys.getSystemCode())) {
          modules.add(module);
        }
      }
      sysBussysVo.setModules(modules);
      sysBussysVoList.add(sysBussysVo);
    }
    return sysBussysVoList;
  }

  @Override
  public TableDataInfo<SysBussys> selectPageChorFlowList(SysBussys sysBussys, PageQuery pageQuery) {
    Page<SysBussys> page = baseMapper.selectPage(pageQuery.build(),
        this.buildQueryWrapper(sysBussys));
    return TableDataInfo.build(page);
  }

  // ===============================================================

  private LambdaQueryWrapper<SysBussys> buildQueryWrapper(SysBussys sysBussys) {
    LambdaQueryWrapper<SysBussys> wrapper = new LambdaQueryWrapper<>();
    wrapper.like(StringUtils.isNotBlank(sysBussys.getSystemName()), SysBussys::getSystemName,
            sysBussys.getSystemName())
        .like(StringUtils.isNotBlank(sysBussys.getDescription()), SysBussys::getDescription,
            sysBussys.getDescription());
    return wrapper;
  }
}
