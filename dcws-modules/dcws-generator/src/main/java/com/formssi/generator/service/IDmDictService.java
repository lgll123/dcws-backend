package com.formssi.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.generator.domain.DmDict;
import com.formssi.generator.model.request.DmDictQueryRequest;
import com.formssi.generator.model.request.DmDictRequest;
import com.formssi.generator.model.vo.DmDictVO;

/**
 * 数据字典 服务层
 *
 * @author Shen Tao
 */
public interface IDmDictService extends IService<DmDict> {

    TableDataInfo<DmDictVO> selectPage(DmDictQueryRequest request, PageQuery pageQuery);

    Long addDict(DmDictRequest request);

    void updateDict(Long dictId, DmDictRequest request);

    void deleteDict(Long dictId);

}
