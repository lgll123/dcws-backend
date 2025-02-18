package com.formssi.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.generator.domain.DmLayout;
import com.formssi.generator.model.request.DmLayoutPageRequest;
import com.formssi.generator.model.request.DmLayoutTableRequest;
import com.formssi.generator.model.request.DmTableRequest;

/**
 * ER图设计 服务层
 *
 * @author Shene Tao
 */
public interface IDmLayoutService extends IService<DmLayout> {

    TableDataInfo<DmLayout> findLayoutPage(DmLayoutPageRequest layoutPageRequest, PageQuery pageQuery);

    DmLayout getLayout(Long layoutId);

    void addLayout(String requestBody);

    void updateLayout(Long layoutId, String requestBody);

    void deleteLayoutByIds(Long[] layoutIds);

    void applyTableLayout(DmLayoutTableRequest layoutTableRequest, boolean isForced);

    void applyTableLayout(DmTableRequest dmTableRequest, boolean isForced);
}
