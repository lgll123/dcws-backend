package com.formssi.workflow.externalsystem.assets.listener;

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
            List<Map<String, Object>> extMaints = Optional.ofNullable(ITDept.get("extMaint"))
                    .map(obj -> (List<Map<String, Object>>) obj)
                    .orElse(Collections.emptyList());
            extMaints.stream()
                    .filter(extMaint->!ObjectUtil.isEmpty(extMaint.get("id")) && !ObjectUtil.isEmpty(extMaint.get("supplierId")))
                    .forEach(e->{
                    Map<String, String> requestBodyMap = new HashMap<>();
                    requestBodyMap.put("title",e.get("name")+"-维修");
                    requestBodyMap.put("asset_id",Convert.toStr(e.get("id")));//资产
                    requestBodyMap.put("supplier_id",Convert.toStr(e.get("supplierId")));// 供应商
                    requestBodyMap.put("asset_maintenance_type","维修");//资产维护类型
                    requestBodyMap.put("start_date",Convert.toStr(e.get("orderDate")));
                    //TODO  添加失败记录，后续处理
                    Map<String, Object> responseMap = instance.process(requestBodyMap,"maintenances","post");
                });
        } catch (Exception e) {
            log.error("An error occurred while AssetsMaintRecordAddTaskListener", e);
        }
    }




}
