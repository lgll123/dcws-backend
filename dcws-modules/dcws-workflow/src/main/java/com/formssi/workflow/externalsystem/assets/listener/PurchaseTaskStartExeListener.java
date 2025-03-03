package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.collection.CollectionUtil;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.CompleteTaskBo;
import com.formssi.workflow.domain.bo.StartProcessBo;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.externalsystem.assets.servicewrapper.IActTaskServiceWrapper;
import com.formssi.workflow.service.IApplyService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

/**
 * 采购任务启动
 */
@Slf4j
public class PurchaseTaskStartExeListener implements ExecutionListener {
    private static  final IApplyService applyService = SpringUtils.getBean(IApplyService.class);
    private static  final IActTaskServiceWrapper actTaskServiceWrapper = SpringUtils.getBean(IActTaskServiceWrapper.class);
    @Override
    public void notify(DelegateExecution delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
//        TaskNodeDataBo taskNodeDataBo = null;
//        HashMap<String,Object> hashMap =null;
//        try {
           /* Object entity = variables.get("entity");
            if(variables.get("entity")!=null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    taskNodeDataBo = objectMapper.readValue(JSON.toJSONString(entity), TaskNodeDataBo.class);
                    hashMap = objectMapper.readValue(taskNodeDataBo.getApplyDetail(), HashMap.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }*/


            TaskNodeDataBo taskNodeDataBo = new TaskNodeDataBo();
            taskNodeDataBo.setApplicant("dcws采购");
            taskNodeDataBo.setApplicantId(4L);
            taskNodeDataBo.setApplyDate(new Date());
            taskNodeDataBo.setApplyDept("四方精创采购");
            taskNodeDataBo.setApplyType("19");
            taskNodeDataBo.setAssetUserId(4L);
            taskNodeDataBo.setApplyReson("采购物料");
            TaskNodeDataVo taskNodeDataVo = applyService.insertByBo(taskNodeDataBo);
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


//        } catch (Exception e) {
//            log.error("An error occurred while calling the external system", e);
//        }
    }




}
