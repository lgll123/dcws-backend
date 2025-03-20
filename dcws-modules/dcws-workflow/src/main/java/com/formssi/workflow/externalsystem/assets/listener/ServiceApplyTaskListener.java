package com.formssi.workflow.externalsystem.assets.listener;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.DcwsProjectBo;
import com.formssi.workflow.externalsystem.assets.servicewrapper.IActTaskServiceWrapper;
import com.formssi.workflow.service.IApplyService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;

/**
 * 服务申请流程，在流程执行到行政部填写维修单后，启动采购流程
 */
@Slf4j
@Component("ServiceApplyTaskListener")
public class ServiceApplyTaskListener implements TaskListener {
    private static  final IApplyService applyService = SpringUtils.getBean(IApplyService.class);
    private static  final IActTaskServiceWrapper actTaskServiceWrapper = SpringUtils.getBean(IActTaskServiceWrapper.class);
    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        Map<String, Object> taskNodeData = null;
        Map<String,Object> map;
        Map<String, Object> purchaseDetail;
        String materialInfoJson = null;
        try {
            Object entity = variables.get("entity");
            if(variables.get("entity")!=null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    taskNodeData = ((Map<String, Object>)objectMapper.readValue(JSON.toJSONString(entity), Map.class));
//                    map = objectMapper.readValue(Convert.toStr(taskNodeData.get("applyDetail")), Map.class);
//                    purchaseDetail = (Map<String, Object>) map.get("purchaseDetail");
//                    materialInfoJson = JSON.toJSONString(purchaseDetail.get("materialInfo"));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            DcwsProjectBo projectBo =new DcwsProjectBo();
            projectBo.setProjectName("采购项目测试-服务申请");
            projectBo.setProjectType("2");
            Map<String, Object> finalTaskNodeData = taskNodeData;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 在此处启动独立流程的任务
                    Integer result = actTaskServiceWrapper.startProjectInNewTransaction(projectBo, finalTaskNodeData);// 调用包裹方法
                    // 记录失败记录，待auto处理启动流程 TODO
                    log.info("待auto处理启动流程result----------------{}", result);
                }
            });
        } catch (Exception e) {
            // 记录失败记录，待auto处理启动流程 TODO
            log.info("待auto处理启动流程----------------");
            log.error("An error occurred while calling the external system", e);
        }
    }




}
