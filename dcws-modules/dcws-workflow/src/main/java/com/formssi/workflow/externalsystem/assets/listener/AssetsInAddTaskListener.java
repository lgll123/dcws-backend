package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.DcwsAssetsCheckOutBo;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
import com.formssi.workflow.service.IAssetsCheckOutRecordService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.formssi.workflow.common.enums.AssetsCheckStatusEnum.CHECK_STATUS_0;
import static com.formssi.workflow.common.enums.AssetsCheckStatusEnum.CHECK_STATUS_1;
import static com.formssi.workflow.common.enums.AssetsCheckTypeEnum.CHECK_TYPE_1;
import static com.formssi.workflow.common.enums.AssetsCheckTypeEnum.CHECK_TYPE_5;

/**
 * 资产导入，向资产系统新增一条资产
 */
@Slf4j
@Component("AssetsInAddTaskListener")
public class AssetsInAddTaskListener implements TaskListener {
    private static final IAssetsCheckOutRecordService assetsCheckOutRecordService = SpringUtils.getBean(IAssetsCheckOutRecordService.class);
    private static final IExternalSystemAPIStrategy instance = SpringUtils.getBean("assets" + IExternalSystemAPIStrategy.BASE_NAME);
    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        try {
            Object entity = variables.get("entity");
            if (ObjectUtil.isEmpty(entity)) return;
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> taskNodeData = (Map<String, Object>)mapper.readValue(JSONUtil.toJsonStr(entity), Map.class);
            if (ObjectUtil.isEmpty(taskNodeData)) return;
            Map<String ,Object> map = mapper.readValue(Convert.toStr(taskNodeData.get("applyDetail")), Map.class);
            // 统一处理add 集合数据
            List<Map<String, Object>> hardware = selectByOptional(map.get("hardware"));//资产-hardware
            List<Map<String, Object>> licenses = selectByOptional(map.get("licenses"));//许可证-licenses
            List<Map<String, Object>> accessories = selectByOptional(map.get("accessories"));//附属品-accessories
            List<Map<String, Object>> components = selectByOptional(map.get("components"));//组件-components
            List<Map<String, Object>> consumables = selectByOptional(map.get("consumables"));//消耗品-consumables
            String taskNodeDataId = Convert.toStr(taskNodeData.get("id"));
            Long assetUserId = Convert.toLong(taskNodeData.get("assetUserId"));// 资产系统用户ID
            DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
            bo.setTaskNodeDataId(taskNodeDataId);
            bo.setCheckOutUser(assetUserId.toString());
            bo.setAutoHandleNum(0);
            bo.setStatus(CHECK_STATUS_1.getCode());//成功
            bo.setCheckOutIn("3");
            bo.setCheckType(CHECK_TYPE_5.getCode());
            // 资产-hardware 新增
            if(!ObjectUtil.isEmpty(hardware)) {
                bo.setAssetsType("hardware");
                Map<String, String> requestBodyMap = new HashMap<>();
                hardware.forEach(h->{
                    bo.setAssetsDetail(JSONUtil.toJsonStr(h));
                    requestBodyMap.put("asset_tag", Convert.toStr(h.get("assetTag")));
                    requestBodyMap.put("status_id", "7");// TODO 默认7
                    requestBodyMap.put("model_id", Convert.toStr(h.get("modelId")));
                    requestBodyMap.put("name", Convert.toStr(h.get("name")));
                    requestBodyMap.put("serial", Convert.toStr(h.get("serialNumber")));
                    requestBodyMap.put("rtd_location_id", Convert.toStr(h.get("locationId")));
                    requestBodyMap.put("purchase_cost", Convert.toStr(h.get("purchaseCost")));
                    requestBodyMap.put("supplier_id", Convert.toStr(h.get("supplierId")));
                    try {
                        Map<String, Object> responseMap = instance.process(requestBodyMap,"hardware","post");
                        bo.setMessage(Convert.toStr(responseMap.get("messages")));
                        bo.setCode(Convert.toStr(responseMap.get("status")));
                    } catch (Exception e) {
                        bo.setStatus(CHECK_STATUS_0.getCode());
                        bo.setMessage(e.getMessage());
                        log.error("资产-hardware 新增失败", e);
                    }
                    saveCheckOutRecord(bo);// 资产入库记录新增记录
                });
            }
            // 附属品-accessories 新增
            if (!CollectionUtil.isEmpty(accessories)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                accessories.forEach(a->{
                    bo.setAssetsDetail(JSONUtil.toJsonStr(a));
                    requestBodyMap.put("name", Convert.toStr(a.get("name")));
                    requestBodyMap.put("qty", Convert.toStr(a.get("num")));
                    requestBodyMap.put("category_id", Convert.toStr(a.get("categoryId")));
                    requestBodyMap.put("model_number", Convert.toStr(a.get("modelNumber")));
                    requestBodyMap.put("location_id", Convert.toStr(a.get("locationId")));
                    requestBodyMap.put("purchase_cost", Convert.toStr(a.get("purchaseCost")));
                    requestBodyMap.put("purchase_date", Convert.toStr(a.get("purchaseDate")));
                    requestBodyMap.put("supplier_id", Convert.toStr(a.get("supplierId")));
                    try {
                        Map<String, Object> responseMap = instance.process(requestBodyMap,"accessories","post");
                        bo.setMessage(Convert.toStr(responseMap.get("messages")));
                        bo.setCode(Convert.toStr(responseMap.get("status")));
                    } catch (Exception e) {
                        bo.setStatus(CHECK_STATUS_0.getCode());
                        bo.setMessage(e.getMessage());
                        log.error("附属品-hardware 新增失败", e);
                    }
                    saveCheckOutRecord(bo);// 资产入库记录新增记录
                });
            }
            // 组件-components 新增
            if (!CollectionUtil.isEmpty(components)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                components.forEach(c->{
                    bo.setAssetsDetail(JSONUtil.toJsonStr(c));
                    requestBodyMap.put("name", Convert.toStr(c.get("name")));
                    requestBodyMap.put("qty", Convert.toStr(c.get("num")));
                    requestBodyMap.put("category_id", Convert.toStr(c.get("categoryId")));
                    requestBodyMap.put("serial", Convert.toStr(c.get("serial")));
                    requestBodyMap.put("location_id", Convert.toStr(c.get("locationId")));
                    requestBodyMap.put("supplier_id", Convert.toStr(c.get("supplierId")));
                    requestBodyMap.put("purchase_cost", Convert.toStr(c.get("purchaseCost")));
                    try {
                        Map<String, Object> responseMap = instance.process(requestBodyMap,"components","post");
                        bo.setMessage(Convert.toStr(responseMap.get("messages")));
                        bo.setCode(Convert.toStr(responseMap.get("status")));
                    } catch (Exception e) {
                        bo.setStatus(CHECK_STATUS_0.getCode());
                        bo.setMessage(e.getMessage());
                        log.error("组件-hardware 新增失败", e);
                    }
                    saveCheckOutRecord(bo);// 资产入库记录新增记录
                });
            }
            // 消耗品-consumables 新增
            if (!CollectionUtil.isEmpty(consumables)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                consumables.forEach(c->{
                    bo.setAssetsDetail(JSONUtil.toJsonStr(c));
                    requestBodyMap.put("name", Convert.toStr(c.get("name")));
                    requestBodyMap.put("qty", Convert.toStr(c.get("num")));
                    requestBodyMap.put("category_id", Convert.toStr(c.get("categoryId")));
                    requestBodyMap.put("supplier_id", Convert.toStr(c.get("supplierId")));
                    requestBodyMap.put("location_id", Convert.toStr(c.get("locationId")));
                    requestBodyMap.put("model_number", Convert.toStr(c.get("modelNumber")));
                    requestBodyMap.put("purchase_cost", Convert.toStr(c.get("purchaseCost")));
                    try {
                        Map<String, Object> responseMap = instance.process(requestBodyMap,"consumables","post");
                        bo.setMessage(Convert.toStr(responseMap.get("messages")));
                        bo.setCode(Convert.toStr(responseMap.get("status")));
                    } catch (Exception e) {
                        bo.setStatus(CHECK_STATUS_0.getCode());
                        bo.setMessage(e.getMessage());
                        log.error("消耗品-hardware 新增失败", e);
                    }
                    saveCheckOutRecord(bo);// 资产入库记录新增记录
                });
            }
            //许可证-licenses 新增
            if (!CollectionUtil.isEmpty(licenses)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                licenses.forEach(c->{
                    bo.setAssetsDetail(JSONUtil.toJsonStr(c));
                    requestBodyMap.put("name", Convert.toStr(c.get("name")));
                    requestBodyMap.put("seats", Convert.toStr(c.get("seats")));
                    requestBodyMap.put("category_id", Convert.toStr(c.get("categoryId")));
                    requestBodyMap.put("serial", Convert.toStr(c.get("serial")));
                    requestBodyMap.put("manufacturer_id", Convert.toStr(c.get("manufacturerId")));
                    requestBodyMap.put("license_name", Convert.toStr(c.get("licenseName")));
                    requestBodyMap.put("license_email", Convert.toStr(c.get("licenseEmail")));
                    requestBodyMap.put("expiration_date", Convert.toStr(c.get("expirationDate")));
                    try {
                        Map<String, Object> responseMap = instance.process(requestBodyMap,"licenses","post");
                        bo.setMessage(Convert.toStr(responseMap.get("messages")));
                        bo.setCode(Convert.toStr(responseMap.get("status")));
                    } catch (Exception e) {
                        bo.setStatus(CHECK_STATUS_0.getCode());
                        bo.setMessage(e.getMessage());
                        log.error("许可证-hardware 新增失败", e);
                    }
                    saveCheckOutRecord(bo);// 资产入库记录新增记录
                });
            }
        } catch(Exception e) {
            log.error("An error occurred while calling the external system", e);
        }
    }
    // Optional 获取处理列表
    private List<Map<String,Object>> selectByOptional(Object dataList) {
        return  Optional.ofNullable(dataList)
                .map(obj -> (List<Map<String, Object>>) obj)
                .orElse(Collections.emptyList());
    }

    // 统一保存记录
    private void saveCheckOutRecord(DcwsAssetsCheckOutBo bo) {
        try {
            assetsCheckOutRecordService.insertByBo(bo);
            if(bo.getId()==null){
                log.info("资产入库记录新增失败：id == null");
            }
            bo.setId(null);
        } catch (Exception e) {
            log.error("资产入库记录新增失败", e);
        }
    }

}
