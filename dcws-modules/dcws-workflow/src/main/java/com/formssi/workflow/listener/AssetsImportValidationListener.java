package com.formssi.workflow.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.RowError;
import com.formssi.workflow.domain.vo.*;
import com.formssi.workflow.externalsystem.assets.service.IAssetsSystemService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
/**
 * 资产入库导入execl 校验监听器
 */
@Slf4j
public class AssetsImportValidationListener<T> extends AnalysisEventListener<T> {
    private final IAssetsSystemService assetsSystemService = SpringUtils.getBean(IAssetsSystemService.class);
    private final List<RowError> errors = new ArrayList<>();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    public static final String MODELS_LIST = "models/selectlist";
    public static final String SUPPLIERS_LIST = "suppliers/selectlist";
    public static final String LOCATIONS_LIST = "locations/selectlist";
    public static final String MANUFACTURERS_LIST = "manufacturers/selectlist";
    public static final String LICENSE_CATEGORY = "categories/license/selectlist";

    @Override
    public void invoke(T data, AnalysisContext context) {
        // 注解公共校验
        Set<ConstraintViolation<T>> violations = validator.validate(data);
        if (!violations.isEmpty())  {
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, violations.iterator().next().getMessage()));
            return;
        }
        if(data instanceof HardwareInfoImportVo hardwareInfoImportVo){
            //校验资产数据
            checkHardwareData(hardwareInfoImportVo,context);
        }else if(data instanceof LicensesInfoImportVo licensesInfoImportVo){
            //校验许可证数据
            checkLicensesData(licensesInfoImportVo,context);
        }else if(data instanceof AccessoriesInfoImportVo accessoriesInfoImportVo){
            //校验附属品数据
            checkAccessoriesData(accessoriesInfoImportVo,context);
        }else if(data instanceof ComponentsInfoImportVo componentsInfoImportVo){
            //校验组件数据
            checkComponentsData(componentsInfoImportVo,context);
        }else if(data instanceof ConsumablesInfoImportVo consumablesInfoImportVo){
            //校验消耗品数据
            checkConsumablesData(consumablesInfoImportVo,context);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (!errors.isEmpty())  {
            throw new ServiceException(
                    errors.stream()
                            .map(e -> "第" + e.getRowNum()  + "行: " + e.getMessage())
                            .collect(Collectors.joining(" ；\n")));
        }
    }

    private void checkHardwareData(HardwareInfoImportVo vo, AnalysisContext context) {
        List<String> modelIds = checkEntityExists(vo.getModelNumber(), MODELS_LIST, "资产型号不存在", context);
        vo.setModelId(ObjectUtil.isEmpty(modelIds)?null:modelIds.get(0));
        List<String> supplierIds = checkEntityExists(vo.getSupplier(), SUPPLIERS_LIST, "供应商不存在", context);
        vo.setSupplierId(ObjectUtil.isEmpty(supplierIds)?null:supplierIds.get(0));
        List<String> locationIds = checkEntityExists(vo.getLocation(), LOCATIONS_LIST, "位置不存在", context);
        vo.setLocationId(ObjectUtil.isEmpty(locationIds)?null:locationIds.get(0));
    }

    private void checkLicensesData(LicensesInfoImportVo vo, AnalysisContext context) {
        List<String> categoryIds = checkEntityExists(vo.getCategory(), LICENSE_CATEGORY, "分类名称不存在", context);
        vo.setCategoryId(ObjectUtil.isEmpty(categoryIds)?null:categoryIds.get(0));
        List<String> manufacturerIds = checkEntityExists(vo.getManufacturer(), MANUFACTURERS_LIST, "制造商不存在", context);
        vo.setManufacturerId(ObjectUtil.isEmpty(manufacturerIds)?null:manufacturerIds.get(0));
    }

    private void checkAccessoriesData(AccessoriesInfoImportVo vo, AnalysisContext context){
        List<String> categoryIds = checkEntityExists(vo.getCategory(), LICENSE_CATEGORY, "类别不存在", context);
        vo.setCategoryId(ObjectUtil.isEmpty(categoryIds)?null:categoryIds.get(0));
        List<String> supplierIds = checkEntityExists(vo.getSupplier(), SUPPLIERS_LIST, "供应商不存在", context);
        vo.setSupplierId(ObjectUtil.isEmpty(supplierIds)?null:supplierIds.get(0));
        List<String> locationIds = checkEntityExists(vo.getLocation(), LOCATIONS_LIST, "位置不存在", context);
        vo.setLocationId(ObjectUtil.isEmpty(locationIds)?null:locationIds.get(0));
    }

    private void checkComponentsData(ComponentsInfoImportVo vo, AnalysisContext context){
        List<String> categoryIds = checkEntityExists(vo.getCategory(), LICENSE_CATEGORY, "类别不存在", context);
        vo.setCategoryId(ObjectUtil.isEmpty(categoryIds)?null:categoryIds.get(0));
        List<String> supplierIds = checkEntityExists(vo.getSupplier(), SUPPLIERS_LIST, "供应商不存在", context);
        vo.setSupplierId(ObjectUtil.isEmpty(supplierIds)?null:supplierIds.get(0));
        List<String> locationIds = checkEntityExists(vo.getLocation(), LOCATIONS_LIST, "位置不存在", context);
        vo.setLocationId(ObjectUtil.isEmpty(locationIds)?null:locationIds.get(0));
    }

    private void checkConsumablesData(ConsumablesInfoImportVo vo, AnalysisContext context){
        List<String> categoryIds = checkEntityExists(vo.getCategory(), LICENSE_CATEGORY, "类别不存在", context);
        vo.setCategoryId(Objects.requireNonNull(categoryIds).get(0));
        List<String> supplierIds = checkEntityExists(vo.getSupplier(), SUPPLIERS_LIST, "供应商不存在", context);
        vo.setSupplierId(Objects.requireNonNull(supplierIds).get(0));
        List<String> locationIds = checkEntityExists(vo.getLocation(), LOCATIONS_LIST, "位置不存在", context);
        vo.setLocationId(Objects.requireNonNull(locationIds).get(0));
    }

    // 通用校验方法
    private List<String> checkEntityExists(String value, String apiPath, String errorMsg, AnalysisContext context) {
        int currentPage = 1, totalPages = 1;
        while (currentPage <= totalPages) {
            Map<String, Object> response = assetsSystemService.selectList(currentPage, apiPath);
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (CollectionUtil.isNotEmpty(results)) {
                return results.stream()
                        .filter(r ->value.equals(Convert.toStr(r.get("text")).trim()))
                        .map(l->Convert.toStr(l.get("id")))
                        .toList();
            }
            totalPages = Convert.toInt(response.get("page_count"), 1);
            currentPage++;
        }
        addError(context, errorMsg);
        return null;
    }

    private void addError(AnalysisContext context, String message) {
        int rowNum = context.readRowHolder().getRowIndex() + 1;
        errors.add(new RowError(rowNum, message));
    }
}
