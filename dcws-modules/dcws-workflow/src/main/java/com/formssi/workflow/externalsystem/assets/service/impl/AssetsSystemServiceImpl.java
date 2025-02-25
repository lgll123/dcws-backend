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

import static com.formssi.workflow.externalsystem.assets.constant.AssetsConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssetsSystemServiceImpl implements IAssetsSystemService {
    private static final String beanName = "assets" + IExternalSystemAPIStrategy.BASE_NAME;
    @Autowired
    private final Map<String, AssetProcessor> processors;

    @Override
    public List<Map<String, Object>> queryRepertory(AssetsSystemBo bo) {
        Map<String, String> params = new HashMap<>();
        params.put(SEARCH, StringUtils.defaultIfBlank(bo.getName(), ""));
        params.put(LIMIT, String.valueOf(bo.getLimit()));
        params.put(OFFSET, String.valueOf(bo.getOffset()));
        params.put(ORDER_NUMBER, "null");
        params.put(SORT, "created_at");
        params.put(ORDER, "desc");
        params.put(EXPAND, "false");

        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("外系统类型不正确!");
        }
        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
        Map<String, Object> responseMap = instance.process(params, "APIType",REQUEST_TYPE_GET);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) responseMap.get(ROWS);
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
        Map<String, Object> responseMap = instance.process(null,fullUrl,REQUEST_TYPE_GET);
        AssetsSystemBo result = new AssetsSystemBo();
        result.setId(TypeSafeUtils.safeGetInteger(responseMap, ID));
        result.setQty(TypeSafeUtils.safeGetInteger(responseMap, QTY));
        result.setRemainQty(TypeSafeUtils.safeGetInteger(responseMap, REMAINING_QTY));
        result.setCheckoutsCount(TypeSafeUtils.safeGetInteger(responseMap, CHECKOUTS_COUNT));
        return result;
    }

    @Override
    public TableDataInfo<CategoryBo> queryPageCategories(String categoryType, PageQuery pageQuery, String pathUrl) {
        Map<String, String> params = new HashMap<>();
        params.put(CATEGORY_TYPE, categoryType);
        params.put(SEARCH, "");
        params.put(LIMIT, String.valueOf(pageQuery.getPageSize()));
        params.put(OFFSET, String.valueOf(pageQuery.getPageSize() * pageQuery.getPageNum()));
        params.put(ORDER, pageQuery.getIsAsc());

        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("外系统类型不正确!");
        }
        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
        Map<String, Object> responseMap = instance.process(params, pathUrl,REQUEST_TYPE_GET);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) responseMap.get(ROWS);
        Integer total = TypeSafeUtils.safeGetInteger(responseMap, TOTAL);

        List<CategoryBo> categoryBos = new ArrayList<>();
        rows.forEach(row -> {
            CategoryBo categoryBo = new CategoryBo();
            categoryBo.setId(TypeSafeUtils.safeGetInteger(row, ID));
            categoryBo.setCategoryType(TypeSafeUtils.safeGetString(row, CATEGORY_TYPE));
            categoryBo.setName(TypeSafeUtils.safeGetString(row, NAME));
            categoryBos.add(categoryBo);
        });
        return new TableDataInfo<>(categoryBos, total.longValue());
    }


    @Override
    public TableDataInfo<AssetsSystemBo> queryAccessoriesById(Integer categoryId, PageQuery pageQuery,String categories) {
        Map<String, String> params = new HashMap<>();
        params.put(CATEGORY_ID, String.valueOf(categoryId));
        params.put(SEARCH, "");
        params.put(LIMIT, String.valueOf(pageQuery.getPageSize()));
        params.put(OFFSET, String.valueOf(pageQuery.getPageSize() * pageQuery.getPageNum()));
        params.put(ORDER, pageQuery.getIsAsc());

        if (CATEGORIES_HARDWARE.equals(categories)) {
            params.put(STATUS, STATUS_REQUESTABLE);
        }
        if (!SpringUtils.containsBean(beanName)) {
            throw new ServiceException("外系统类型不正确!");
        }
        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
        Map<String, Object> responseMap = instance.process(params, categories,REQUEST_TYPE_GET);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) responseMap.get(ROWS);
        Integer total = TypeSafeUtils.safeGetInteger(responseMap, TOTAL);

        List<AssetsSystemBo> assetsBos = new ArrayList<>();
        rows.forEach(row -> {
            AssetsSystemBo bo = new AssetsSystemBo();
            bo.setId(TypeSafeUtils.safeGetInteger(row, ID));
            bo.setName(TypeSafeUtils.safeGetString(row, NAME));
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





