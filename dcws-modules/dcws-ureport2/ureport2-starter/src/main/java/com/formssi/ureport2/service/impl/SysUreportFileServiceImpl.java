package com.formssi.ureport2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.ureport2.entity.SysUreportFile;
import com.formssi.ureport2.mapper.SysUreportFileMapper;
import com.formssi.ureport2.service.ISysUreportFileService;
import org.springframework.stereotype.Service;

/**
 * 报表文件表 服务实现类
 *
 * @author zhangmiao
 */
@Service
public class SysUreportFileServiceImpl extends ServiceImpl<SysUreportFileMapper, SysUreportFile>
    implements ISysUreportFileService {

  @Override
  public TableDataInfo<SysUreportFile> queryPageList(BaseEntity entity, PageQuery pageQuery) {
    LambdaQueryWrapper<SysUreportFile> wrapper = new LambdaQueryWrapper<>();
    wrapper.like(StringUtils.isNotBlank(entity.getSearchValue()), SysUreportFile::getName, entity.getSearchValue());
    IPage<SysUreportFile> page = this.page(pageQuery.build(), wrapper);
    return TableDataInfo.build(page);
  }

}
