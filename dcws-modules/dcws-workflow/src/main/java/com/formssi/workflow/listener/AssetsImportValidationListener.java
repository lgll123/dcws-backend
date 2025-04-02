package com.formssi.workflow.listener;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
/**
 * 资产入库导入execl 校验监听器
 */
@Slf4j
public class AssetsImportValidationListener<T> extends AnalysisEventListener<T> {
    private final IAssetsSystemService assetsSystemService = SpringUtils.getBean(IAssetsSystemService.class);
    private final List<RowError> errors = new ArrayList<>();
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Override
    public void invoke(T data, AnalysisContext context) {
        // 注解公共校验
        Set<ConstraintViolation<T>> violations = validator.validate(data);
        if (!violations.isEmpty())  {
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, violations.iterator().next().getMessage()));
            return;
        }
        //校验资产数据
        if(data instanceof HardwareInfoImportVo hardwareInfoImportVo){
            checkHardwareData(hardwareInfoImportVo,context);
        }
        //校验许可证数据
        if(data instanceof LicensesInfoImportVo licensesInfoImportVo){
            checkLicensesData(licensesInfoImportVo,context);
        }
        //校验附属品数据
        if(data instanceof AccessoriesInfoImportVo accessoriesInfoImportVo){
            checkAccessoriesData(accessoriesInfoImportVo,context);
        }
        //校验组件数据
        if(data instanceof ComponentsInfoImportVo componentsInfoImportVo){
            checkComponentsData(componentsInfoImportVo,context);
        }
        //校验消耗品数据
        if(data instanceof ConsumablesInfoImportVo consumablesInfoImportVo){
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

    private void checkHardwareData(HardwareInfoImportVo hardwareInfoImportVo, AnalysisContext context){
        // 校验型号是否存在资产系统
        String modelNumber = hardwareInfoImportVo.getModelNumber();
        boolean modelExists = false;
        int totalPages = 1; // 默认总页数为1
        int currentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (currentPage <= totalPages && !modelExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> modelNumberMap = assetsSystemService.selectList(currentPage, "models/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) modelNumberMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                modelExists = results.stream().anyMatch(r -> modelNumber.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            totalPages = Convert.toInt(modelNumberMap.get("page_count"), 1); // 默认1防止空值
            currentPage++;
        }
        if (!modelExists) {
            // 处理型号不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "资产型号不存在"));
        }

        // 校验供应商是否存在资产系统
        String supplier = hardwareInfoImportVo.getSupplier();
        boolean supplierExists = false;
        int supplierTotalPages = 1; // 默认总页数为1
        int supplierCurrentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (supplierCurrentPage <= supplierTotalPages && !supplierExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> suppliersMap = assetsSystemService.selectList(supplierCurrentPage, "suppliers/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) suppliersMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                supplierExists = results.stream().anyMatch(r -> supplier.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            supplierTotalPages = Convert.toInt(suppliersMap.get("page_count"), 1); // 默认1防止空值
            supplierCurrentPage++;
        }
        if (!supplierExists) {
            // 处理供应商不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "供应商不存在"));
        }

        // 校验位置是否存在资产系统
        String location = hardwareInfoImportVo.getLocation();
        boolean locationExists = false;
        int locationTotalPages = 1; // 默认总页数为1
        int locationCurrentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (locationCurrentPage <= locationTotalPages && !locationExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> locationsMap = assetsSystemService.selectList(locationCurrentPage, "locations/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) locationsMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                locationExists = results.stream().anyMatch(r -> location.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            locationTotalPages = Convert.toInt(locationsMap.get("page_count"), 1); // 默认1防止空值
            locationCurrentPage++;
        }
        if (!locationExists) {
            // 处理位置不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "位置不存在"));
        }
    }

    private void checkLicensesData(LicensesInfoImportVo licensesInfoImportVo, AnalysisContext context){
        // 校验分类名称是否存在资产系统
        String category = licensesInfoImportVo.getCategory();
        boolean categoryExists = false;
        int totalPages = 1; // 默认总页数为1
        int currentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (currentPage <= totalPages && !categoryExists){
            //component-组件 consumable-消耗品 license-许可证 accessory-附属品
            Map<String, Object> categoryNumberMap = assetsSystemService.selectList(currentPage, "categories/license/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) categoryNumberMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                categoryExists = results.stream().anyMatch(r -> category.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            totalPages = Convert.toInt(categoryNumberMap.get("page_count"), 1); // 默认1防止空值
            currentPage++;
        }
        if (!categoryExists) {
            // 处理分类名称不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "分类名称不存在"));
        }

        // 校验供应商是否存在资产系统
        /*String supplier = licensesInfoImportVo.getSupplier();
        boolean supplierExists = false;
        int supplierTotalPages = 1; // 默认总页数为1
        int supplierCurrentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (supplierCurrentPage <= supplierTotalPages && !supplierExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> suppliersMap = assetsSystemService.selectList(supplierCurrentPage, "suppliers/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) suppliersMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                supplierExists = results.stream().anyMatch(r -> supplier.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            supplierTotalPages = Convert.toInt(suppliersMap.get("page_count"), 1); // 默认1防止空值
            supplierCurrentPage++;
        }
        if (!supplierExists) {
            // 处理供应商不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "供应商不存在"));
            return;
        }*/

        // 校验制造商是否存在资产系统
        String manufacturer = licensesInfoImportVo.getManufacturer();
        boolean manufacturerExists = false;
        int manufacturerTotalPages = 1; // 默认总页数为1
        int manufacturerCurrentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (manufacturerCurrentPage <= manufacturerTotalPages && !manufacturerExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> manufacturersMap = assetsSystemService.selectList(manufacturerCurrentPage, "manufacturers/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) manufacturersMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                manufacturerExists = results.stream().anyMatch(r -> manufacturer.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            manufacturerTotalPages = Convert.toInt(manufacturersMap.get("page_count"), 1); // 默认1防止空值
            manufacturerCurrentPage++;
        }
        if (!manufacturerExists) {
            // 处理制造商不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "制造商不存在"));
        }
    }

    private void checkAccessoriesData(AccessoriesInfoImportVo accessoriesInfoImportVo, AnalysisContext context){
        // 校验类别是否存在资产系统
        String category = accessoriesInfoImportVo.getCategory();
        boolean categoryExists = false;
        int totalPages = 1; // 默认总页数为1
        int currentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (currentPage <= totalPages && !categoryExists){
            //component-组件 consumable-消耗品 license-许可证 accessory-附属品
            Map<String, Object> categoryMap = assetsSystemService.selectList(currentPage, "accessory/license/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) categoryMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                categoryExists = results.stream().anyMatch(r -> category.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            totalPages = Convert.toInt(categoryMap.get("page_count"), 1); // 默认1防止空值
            currentPage++;
        }
        if (!categoryExists) {
            // 处理类别不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "类别不存在"));
        }

        // 校验供应商是否存在资产系统
        String supplier = accessoriesInfoImportVo.getSupplier();
        boolean supplierExists = false;
        int supplierTotalPages = 1; // 默认总页数为1
        int supplierCurrentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (supplierCurrentPage <= supplierTotalPages && !supplierExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> suppliersMap = assetsSystemService.selectList(supplierCurrentPage, "suppliers/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) suppliersMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                supplierExists = results.stream().anyMatch(r -> supplier.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            supplierTotalPages = Convert.toInt(suppliersMap.get("page_count"), 1); // 默认1防止空值
            supplierCurrentPage++;
        }
        if (!supplierExists) {
            // 处理供应商不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "供应商不存在"));
        }

        // 校验制造商是否存在资产系统
        /*String manufacturer = accessoriesInfoImportVo.getManufacturer();
        boolean manufacturerExists = false;
        int manufacturerTotalPages = 1; // 默认总页数为1
        int manufacturerCurrentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (manufacturerCurrentPage <= manufacturerTotalPages && !manufacturerExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> manufacturersMap = assetsSystemService.selectList(manufacturerCurrentPage, "manufacturers/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) manufacturersMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                manufacturerExists = results.stream().anyMatch(r -> manufacturer.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            manufacturerTotalPages = Convert.toInt(manufacturersMap.get("page_count"), 1); // 默认1防止空值
            manufacturerCurrentPage++;
        }
        if (!manufacturerExists) {
            // 处理制造商不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "制造商不存在"));
        }*/

        // 校验位置是否存在资产系统
        String location = accessoriesInfoImportVo.getLocation();
        boolean locationExists = false;
        int locationTotalPages = 1; // 默认总页数为1
        int locationCurrentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (locationCurrentPage <= locationTotalPages && !locationExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> locationsMap = assetsSystemService.selectList(locationCurrentPage, "locations/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) locationsMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                locationExists = results.stream().anyMatch(r -> location.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            locationTotalPages = Convert.toInt(locationsMap.get("page_count"), 1); // 默认1防止空值
            locationCurrentPage++;
        }
        if (!locationExists) {
            // 处理位置不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "位置不存在"));
        }
    }

    private void checkComponentsData(ComponentsInfoImportVo componentsInfoImportVo, AnalysisContext context){
        // 校验类别是否存在资产系统
        String category = componentsInfoImportVo.getCategory();
        boolean categoryExists = false;
        int totalPages = 1; // 默认总页数为1
        int currentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (currentPage <= totalPages && !categoryExists){
            //component-组件 consumable-消耗品 license-许可证 accessory-附属品
            Map<String, Object> categoryMap = assetsSystemService.selectList(currentPage, "accessory/license/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) categoryMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                categoryExists = results.stream().anyMatch(r -> category.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            totalPages = Convert.toInt(categoryMap.get("page_count"), 1); // 默认1防止空值
            currentPage++;
        }
        if (!categoryExists) {
            // 处理类别不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "类别不存在"));
        }

        // 校验供应商是否存在资产系统
        String supplier = componentsInfoImportVo.getSupplier();
        boolean supplierExists = false;
        int supplierTotalPages = 1; // 默认总页数为1
        int supplierCurrentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (supplierCurrentPage <= supplierTotalPages && !supplierExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> suppliersMap = assetsSystemService.selectList(supplierCurrentPage, "suppliers/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) suppliersMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                supplierExists = results.stream().anyMatch(r -> supplier.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            supplierTotalPages = Convert.toInt(suppliersMap.get("page_count"), 1); // 默认1防止空值
            supplierCurrentPage++;
        }
        if (!supplierExists) {
            // 处理供应商不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "供应商不存在"));
        }

        // 校验位置是否存在资产系统
        String location = componentsInfoImportVo.getLocation();
        boolean locationExists = false;
        int locationTotalPages = 1; // 默认总页数为1
        int locationCurrentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (locationCurrentPage <= locationTotalPages && !locationExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> locationsMap = assetsSystemService.selectList(locationCurrentPage, "locations/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) locationsMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                locationExists = results.stream().anyMatch(r -> location.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            locationTotalPages = Convert.toInt(locationsMap.get("page_count"), 1); // 默认1防止空值
            locationCurrentPage++;
        }
        if (!locationExists) {
            // 处理位置不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "位置不存在"));
        }
    }

    private void checkConsumablesData(ConsumablesInfoImportVo consumablesInfoImportVo, AnalysisContext context){
        // 校验类别是否存在资产系统
        String category = consumablesInfoImportVo.getCategory();
        boolean categoryExists = false;
        int totalPages = 1; // 默认总页数为1
        int currentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (currentPage <= totalPages && !categoryExists){
            //component-组件 consumable-消耗品 license-许可证 accessory-附属品
            Map<String, Object> categoryMap = assetsSystemService.selectList(currentPage, "accessory/license/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) categoryMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                categoryExists = results.stream().anyMatch(r -> category.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            totalPages = Convert.toInt(categoryMap.get("page_count"), 1); // 默认1防止空值
            currentPage++;
        }
        if (!categoryExists) {
            // 处理类别不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "类别不存在"));
        }

        // 校验供应商是否存在资产系统
        String supplier = consumablesInfoImportVo.getSupplier();
        boolean supplierExists = false;
        int supplierTotalPages = 1; // 默认总页数为1
        int supplierCurrentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (supplierCurrentPage <= supplierTotalPages && !supplierExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> suppliersMap = assetsSystemService.selectList(supplierCurrentPage, "suppliers/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) suppliersMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                supplierExists = results.stream().anyMatch(r -> supplier.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            supplierTotalPages = Convert.toInt(suppliersMap.get("page_count"), 1); // 默认1防止空值
            supplierCurrentPage++;
        }
        if (!supplierExists) {
            // 处理供应商不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "供应商不存在"));
        }

        // 校验位置是否存在资产系统
        String location = consumablesInfoImportVo.getLocation();
        boolean locationExists = false;
        int locationTotalPages = 1; // 默认总页数为1
        int locationCurrentPage = 1;
        // 循环分页查询，直到找到匹配项或遍历所有页
        while (locationCurrentPage <= locationTotalPages && !locationExists){
            //suppliers-供应商列表 models-型号列表 locations-位置列表 manufacturers-制造商列表
            Map<String, Object> locationsMap = assetsSystemService.selectList(locationCurrentPage, "locations/selectlist");
            List<Map<String, Object>> results = (List<Map<String, Object>>) locationsMap.get("results");
            if(!ObjectUtil.isEmpty(results)){
                locationExists = results.stream().anyMatch(r -> location.equals(Convert.toStr(r.get("text"))));
            }
            // 更新总页数和下一页
            locationTotalPages = Convert.toInt(locationsMap.get("page_count"), 1); // 默认1防止空值
            locationCurrentPage++;
        }
        if (!locationExists) {
            // 处理位置不存在的逻辑记录错误
            int rowNum = context.readRowHolder().getRowIndex()  + 1; // 转1-based行号
            errors.add(new  RowError(rowNum, "位置不存在"));
        }
    }
}
