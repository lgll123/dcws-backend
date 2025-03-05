package com.formssi.workflow.externalsystem.assets.service;

import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.AssetsSystemBo;
import com.formssi.workflow.domain.bo.CategoryBo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface IAssetsSystemService {

    /**
     * 根据物料Id查询物料库存
     */
    public AssetsSystemBo queryQtyById(AssetsSystemBo bo,String urlPath);
    /**
     * 根据目录类型查询目录列表
     */
    TableDataInfo<CategoryBo> queryPageCategories(String categoryType,String applyType, PageQuery pageQuery, String urlPath);

    /**
     * 根据目录id查询物料列表
     */
    TableDataInfo<AssetsSystemBo> queryAccessoriesById(Integer categoryId, PageQuery pageQuery, String categories);

    /**
     * 查询组件可checkout的资产列表
     */
    Map<String, Object> selectAssetslist(Integer page,String pathUrl);


    String uploadDocument(MultipartFile file,
                               String title,
                               String created,
                               String correspondentId,
                               String documentTypeId,
                               String storagePathId,
                               String[] tags,
                               String archiveSerialNumber,
                               String[] customFields) throws IOException;
}
