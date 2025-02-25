package com.formssi.workflow.externalsystem.assets.service.impl;

import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.AssetsSystemBo;
import com.formssi.workflow.domain.bo.CategoryBo;
import com.formssi.workflow.externalsystem.assets.service.IAssetsSystemService;
import com.formssi.workflow.externalsystem.assets.strategy.AssetProcessor;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
import com.formssi.workflow.utils.TypeSafeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetsSystemServiceImpl implements IAssetsSystemService {
    private static final String HARDWARE_PATH = "hardware";
    private static final String STATUS_REQUESTABLE = "Requestable";
    private static final String beanName = "assets" + IExternalSystemAPIStrategy.BASE_NAME;
    @Autowired
    private final Map<String, AssetProcessor> processors;

    @Override
    public List<Map<String, Object>> queryRepertory(AssetsSystemBo bo) {
        Map<String, String> params = new HashMap<>();
        params.put("search", StringUtils.defaultIfBlank(bo.getName(), ""));
        params.put("limit", String.valueOf(bo.getLimit()));
        params.put("offset", String.valueOf(bo.getOffset()));
        params.put("order_number", "null");
        params.put("sort", "created_at");
        params.put("order", "desc");
        params.put("expand", "false");

        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("外系统类型不正确!");
        }
        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
        Map<String, Object> responseMap = instance.process(params, "APIType","get");
        List<Map<String, Object>> rows = (List<Map<String, Object>>) responseMap.get("rows");
        if (rows.isEmpty()) {
            throw new ServiceException("库存查询失败：无匹配数据");
        }
        return rows;
    }

    @Override
    public AssetsSystemBo queryQtyById(AssetsSystemBo bo, String pathUrl) {
        String fullUrl = String.format("%s/%s", pathUrl, bo.getId());
        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("外系统类型不正确!");
        }
        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
        Map<String, Object> responseMap = instance.process(null,fullUrl,"get");
        AssetsSystemBo result = new AssetsSystemBo();
        result.setId(TypeSafeUtils.safeGetInteger(responseMap, "id"));
        result.setQty(TypeSafeUtils.safeGetInteger(responseMap, "qty"));
        result.setRemainQty(TypeSafeUtils.safeGetInteger(responseMap, "remaining_qty"));
        result.setCheckoutsCount(TypeSafeUtils.safeGetInteger(responseMap, "checkouts_count"));
        return result;
    }

    @Override
    public TableDataInfo<CategoryBo> queryPageCategories(String categoryType, PageQuery pageQuery, String pathUrl) {
        Map<String, String> params = new HashMap<>();
        params.put("category_type", categoryType);
        params.put("search", "");
        params.put("limit", String.valueOf(pageQuery.getPageSize()));
        params.put("offset", String.valueOf(pageQuery.getPageSize() * pageQuery.getPageNum()));
        params.put("order", pageQuery.getIsAsc());

        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("外系统类型不正确!");
        }
        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
        Map<String, Object> responseMap = instance.process(params, pathUrl,"get");
        List<Map<String, Object>> rows = (List<Map<String, Object>>) responseMap.get("rows");
        Integer total = TypeSafeUtils.safeGetInteger(responseMap, "total");

        List<CategoryBo> categoryBos = new ArrayList<>();
        rows.forEach(row -> {
            CategoryBo categoryBo = new CategoryBo();
            categoryBo.setId(TypeSafeUtils.safeGetInteger(row, "id"));
            categoryBo.setCategoryType(TypeSafeUtils.safeGetString(row, "category_type"));
            categoryBo.setName(TypeSafeUtils.safeGetString(row, "name"));
            categoryBos.add(categoryBo);
        });
        return new TableDataInfo<>(categoryBos, total.longValue());
    }


    @Override
    public TableDataInfo<AssetsSystemBo> queryAccessoriesById(Integer categoryId, PageQuery pageQuery,String categories) {
        Map<String, String> params = new HashMap<>();
        params.put("category_id", String.valueOf(categoryId));
        params.put("search", "");
        params.put("limit", String.valueOf(pageQuery.getPageSize()));
        params.put("offset", String.valueOf(pageQuery.getPageSize() * pageQuery.getPageNum()));
        params.put("order", pageQuery.getIsAsc());

        if (HARDWARE_PATH.equals(categories)) {
            params.put("status", STATUS_REQUESTABLE);
        }
        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("外系统类型不正确!");
        }
        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
        Map<String, Object> responseMap = instance.process(params, categories,"get");
        List<Map<String, Object>> rows = (List<Map<String, Object>>) responseMap.get("rows");
        Integer total = TypeSafeUtils.safeGetInteger(responseMap, "total");

        List<AssetsSystemBo> assetsBos = new ArrayList<>();
        rows.forEach(row -> {
            AssetsSystemBo bo = new AssetsSystemBo();
            bo.setId(TypeSafeUtils.safeGetInteger(row, "id"));
            bo.setName(TypeSafeUtils.safeGetString(row, "name"));
            // 根据urlPath选择处理策略
            AssetProcessor processor = processors.get(categories + "Processor");
            if (processor != null) {
                processor.process(row, bo);
            }
            assetsBos.add(bo);
        });
        return new TableDataInfo<>(assetsBos, total.longValue());
    }

    }





