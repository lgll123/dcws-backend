package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.DcwsCompleteTaskBo;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.externalsystem.assets.servicewrapper.IActTaskServiceWrapper;
import com.formssi.workflow.service.IApplyService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.formssi.workflow.domain.bo.StartProcessBo;
import java.util.*;

/**
 * 采购任务启动
 */
@Slf4j
@Component("PurchaseTaskStartExeListener")
public class PurchaseTaskStartExeListener implements ExecutionListener {
    private static  final IApplyService applyService = SpringUtils.getBean(IApplyService.class);
    private static  final IActTaskServiceWrapper actTaskServiceWrapper = SpringUtils.getBean(IActTaskServiceWrapper.class);
    @Override
    public void notify(DelegateExecution delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        TaskNodeDataBo taskNodeDataBo = null;
        HashMap<String,Object> hashMap =null;
        Map<String, Object> purchaseDetail =null;
        String materialInfoJson = null;
        try {
            Object entity = variables.get("entity");
            if(variables.get("entity")!=null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    taskNodeDataBo = objectMapper.readValue(JSON.toJSONString(entity), TaskNodeDataBo.class);
                    hashMap = objectMapper.readValue(taskNodeDataBo.getApplyDetail(), HashMap.class);
                    purchaseDetail = (Map<String, Object>) hashMap.get("purchaseDetail");
                    materialInfoJson = JSON.toJSONString(purchaseDetail.get("materialInfo"));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
            TaskNodeDataBo purchaseBo = new TaskNodeDataBo();
            purchaseBo.setApplicant("dcws采购");
            purchaseBo.setApplicantId(4L);
            purchaseBo.setApplyDate(new Date());
            purchaseBo.setApplyDept("四方精创采购");
            purchaseBo.setApplyType("19");
            purchaseBo.setAssetUserId(4L);
            purchaseBo.setApplyReson("采购物料");
            purchaseBo.setApplyDetail(materialInfoJson);//采购清单
            purchaseBo.setApplyRemarks(String.valueOf(purchaseDetail.get("purchaseAmount")));//采购金额

            TaskNodeDataVo taskNodeDataVo = applyService.insertByBo(purchaseBo);
            StartProcessBo startProcessBo = new StartProcessBo();
            startProcessBo.setBusinessKey(String.valueOf(taskNodeDataVo.getId()));
            startProcessBo.setRouter("/task/approveTemplate/assetsApproves");
            startProcessBo.setVariables(variables);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 在此处启动独立流程的任务
                    Map<String, Object> stringObjectMap = actTaskServiceWrapper.startProcessInNewTransaction(startProcessBo);// 调用包裹方法
                    if (!CollectionUtil.isEmpty(stringObjectMap)) {
                        DcwsCompleteTaskBo completeTaskBo = new DcwsCompleteTaskBo();
                        completeTaskBo.setTaskId(String.valueOf(stringObjectMap.get("taskId")));
                        List objects = new ArrayList<String>();
                        objects.add("1");
                        completeTaskBo.setMessageType(objects);
                        completeTaskBo.setVariables(variables);
                        boolean completeTaskResult = actTaskServiceWrapper.completeTaskInNewTransaction(completeTaskBo);
                        if (!completeTaskResult) {
                            // 记录失败记录，待auto处理启动流程 TODO
                            log.info("待auto处理启动流程----------------");
                        }
                    } else {
                        // 记录失败记录，待auto处理启动流程 TODO
                        log.info("待auto处理启动流程----------------");
                    }
                }
            });
        } catch (Exception e) {
            // 记录失败记录，待auto处理启动流程 TODO
            log.info("待auto处理启动流程----------------");
            log.error("An error occurred while calling the external system", e);
        }
    }




}
