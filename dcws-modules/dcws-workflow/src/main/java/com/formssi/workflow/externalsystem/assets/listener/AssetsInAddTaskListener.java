package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 资产导入，向资产系统新增一条资产
 */
@Slf4j
@Component("AssetsInAddTaskListener")
public class AssetsInAddTaskListener implements TaskListener {
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

            // 资产-hardware 新增
            if(!ObjectUtil.isEmpty(hardware)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                hardware.forEach(h->{
                    requestBodyMap.put("asset_tag", Convert.toStr(h.get("assetTag")));
                    requestBodyMap.put("status_id", "7");// TODO 默认7
                    requestBodyMap.put("model_id", Convert.toStr(h.get("modelId")));
                    try {
                        Map<String, Object> responseMap = instance.process(requestBodyMap,"hardware","post");
                    } catch (Exception e) {
                        log.error("资产-hardware 新增失败", e);
                    }
                });
            }
            // 附属品-accessories 新增
            if (!CollectionUtil.isEmpty(accessories)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                accessories.forEach(a->{
                    requestBodyMap.put("name", Convert.toStr(a.get("name")));
                    requestBodyMap.put("qty", Convert.toStr(a.get("num")));
                    requestBodyMap.put("category_id", Convert.toStr(a.get("categoryId")));
                    try {
                        Map<String, Object> responseMap = instance.process(requestBodyMap,"accessories","post");
                    } catch (Exception e) {
                        log.error("附属品-hardware 新增失败", e);
                    }
                });
            }
            // 组件-components 新增
            if (!CollectionUtil.isEmpty(components)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                components.forEach(c->{
                    requestBodyMap.put("name", Convert.toStr(c.get("name")));
                    requestBodyMap.put("qty", Convert.toStr(c.get("num")));
                    requestBodyMap.put("category_id", Convert.toStr(c.get("categoryId")));
                    try {
                        Map<String, Object> responseMap = instance.process(requestBodyMap,"components","post");
                    } catch (Exception e) {
                        log.error("组件-hardware 新增失败", e);
                    }
                });
            }
            // 消耗品-consumables 新增
            if (!CollectionUtil.isEmpty(consumables)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                consumables.forEach(c->{
                    requestBodyMap.put("name", Convert.toStr(c.get("name")));
                    requestBodyMap.put("qty", Convert.toStr(c.get("num")));
                    requestBodyMap.put("category_id", Convert.toStr(c.get("categoryId")));
                    try {
                        Map<String, Object> responseMap = instance.process(requestBodyMap,"consumables","post");
                    } catch (Exception e) {
                        log.error("消耗品-hardware 新增失败", e);
                    }
                });
            }
            //许可证-licenses 新增
            if (!CollectionUtil.isEmpty(licenses)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                licenses.forEach(c->{
                    requestBodyMap.put("name", Convert.toStr(c.get("name")));
                    requestBodyMap.put("seats", Convert.toStr(c.get("seats")));
                    requestBodyMap.put("category_id", Convert.toStr(c.get("categoryId")));
                    try {
                        Map<String, Object> responseMap = instance.process(requestBodyMap,"licenses","post");
                    } catch (Exception e) {
                        log.error("许可证-hardware 新增失败", e);
                    }
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



}
