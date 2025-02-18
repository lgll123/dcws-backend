package com.formssi.ureport2.controller;

import com.formssi.common.core.validate.QueryGroup;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.ureport2.entity.SysUreportFile;
import com.formssi.ureport2.service.ISysUreportFileService;
import jakarta.annotation.Resource;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API接口
 *
 * @author zhangmiao
 */
@RestController
@RequestMapping()
public class SampleController {

  @Resource
  private ISysUreportFileService iSysUreportFileService;

  @GetMapping("/list/ureport/file")
  public TableDataInfo<SysUreportFile> list(@Validated(QueryGroup.class) BaseEntity entity, PageQuery pageQuery) {
    return iSysUreportFileService.queryPageList(entity, pageQuery);
  }

}
