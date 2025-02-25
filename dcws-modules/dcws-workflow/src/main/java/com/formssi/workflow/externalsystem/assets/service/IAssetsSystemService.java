package com.formssi.workflow.externalsystem.assets.service;

import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.AssetsSystemBo;
import com.formssi.workflow.domain.bo.CategoryBo;

import java.util.List;
import java.util.Map;

public interface IAssetsSystemService {
    /**
     * 查询请物料库存
     */
    List<Map<String,Object>> queryRepertory(AssetsSystemBo bo);
    /**
     * 根据物料Id查询物料库存
     */
    public AssetsSystemBo queryQtyById(AssetsSystemBo bo,String urlPath);
    /**
     * 根据目录类型查询目录列表
     */
    TableDataInfo<CategoryBo> queryPageCategories(String categoryType, PageQuery pageQuery, String urlPath);

    /**
     * 根据目录id查询物料列表
     */
    TableDataInfo<AssetsSystemBo> queryAccessoriesById(Integer categoryId, PageQuery pageQuery, String categories);

}
