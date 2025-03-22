package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 在填写好维修单后，向资产系统新增一条资产维修记录
 */
@Slf4j
@Component("AssetsMaintRecordAddTaskListener")
public class AssetsMaintRecordAddTaskListener implements TaskListener {
    private static final IExternalSystemAPIStrategy instance = SpringUtils.getBean("assets" + IExternalSystemAPIStrategy.BASE_NAME);
    @Override
    public void notify(DelegateTask delegateTask) {
        try{
            Map<String, Object> variables = delegateTask.getVariables();
            Object entity = variables.get("entity");
            if (ObjectUtil.isEmpty(entity)) return;
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> taskNodeData = (Map<String, Object>)mapper.readValue(JSONUtil.toJsonStr(entity), Map.class);
            if (ObjectUtil.isEmpty(taskNodeData)) return;
            Map<String,Object> map = mapper.readValue(Convert.toStr(taskNodeData.get("applyDetail")), Map.class);
            Map<String, Object> ITDept = (Map<String, Object>) map.get("ITDept");
            Map<String, Object> extMaint = (Map<String, Object>) ITDept.get("extMaint");
            List<Map<String, Object>> hardware = (List<Map<String, Object>>) ITDept.get("hardware");//资产信息

            String url = "maintenances";
            Map<String, String> requestBodyMap = new HashMap<>();
            requestBodyMap.put("title","title-维护");
            requestBodyMap.put("asset_id","1");//资产
            requestBodyMap.put("supplier_id","2");// 供应商
            requestBodyMap.put("asset_maintenance_type","维护");//资产维护类型
            requestBodyMap.put("start_date","2025-03-06");
            if(!ObjectUtil.isEmpty(hardware)){
                requestBodyMap.put("asset_id",Convert.toStr(hardware.get(0).get("id")));//资产
            }
            Map<String, Object> responseMap = instance.process(requestBodyMap,url,"post");

        } catch (Exception e) {
            log.error("An error occurred while AssetsMaintRecordAddTaskListener", e);
        }
    }




}
