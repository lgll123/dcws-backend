package com.formssi.ureport2.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.ureport2.entity.SysUreportFile;

/**
 * 报表文件表 服务类
 *
 * @author zhangmiao
 */
public interface ISysUreportFileService extends IService<SysUreportFile> {


  TableDataInfo<SysUreportFile> queryPageList(BaseEntity entity, PageQuery pageQuery);

}
