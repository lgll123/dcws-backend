package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.CompleteTaskBo;
import com.formssi.workflow.domain.bo.StartProcessBo;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.externalsystem.assets.servicewrapper.IActTaskServiceWrapper;
import com.formssi.workflow.service.IApplyService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
            TaskNodeDataBo purchaseBo = new TaskNodeDataBo();
            purchaseBo.setApplicant("服务采购");
            purchaseBo.setApplicantId(Convert.toLong(taskNodeData.get("applicantId")));
            purchaseBo.setApplyDate(new Date());
            purchaseBo.setApplyDept("四方精创服务采购");
            purchaseBo.setApplyType("23");
            purchaseBo.setAssetUserId(Convert.toLong(taskNodeData.get("assetUserId")));
            purchaseBo.setApplyReson("服务申请采购物料");
            purchaseBo.setCheckTo("服务申请采购物料");
            purchaseBo.setApplyRemarks("服务申请采购物料");
            purchaseBo.setApplyContentType(Convert.toStr(taskNodeData.get("applyContentType")));
            purchaseBo.setApplyDetail(Convert.toStr(taskNodeData.get("applyDetail")));//采购清单

            TaskNodeDataVo taskNodeDataVo = applyService.insertByBo(purchaseBo);
            StartProcessBo startProcessBo = new StartProcessBo();
            startProcessBo.setBusinessKey(String.valueOf(taskNodeDataVo.getId()));
            startProcessBo.setRouter("/task/approveTemplate/applyServiceApproves");
            startProcessBo.setVariables(variables);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    // 在此处启动独立流程的任务
                    Map<String, Object> stringObjectMap = actTaskServiceWrapper.startProcessInNewTransaction(startProcessBo);// 调用包裹方法
                    if (!CollectionUtil.isEmpty(stringObjectMap)) {
                        CompleteTaskBo completeTaskBo = new CompleteTaskBo();
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
