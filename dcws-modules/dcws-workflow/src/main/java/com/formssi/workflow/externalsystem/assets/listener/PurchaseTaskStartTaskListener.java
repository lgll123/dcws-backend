package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.collection.CollectionUtil;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.CompleteTaskBo;
import com.formssi.workflow.domain.bo.StartProcessBo;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.bo.TestLeaveBo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
//import com.formssi.workflow.externalsystem.async.AsyncProcessService;
import com.formssi.workflow.externalsystem.assets.servicewrapper.IActTaskServiceWrapper;
import com.formssi.workflow.service.IActTaskService;
import com.formssi.workflow.service.IApplyService;
import com.formssi.workflow.service.ITestLeaveService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

/**
 * 采购任务启动
 */
@Slf4j
public class PurchaseTaskStartTaskListener implements TaskListener {
    private static  final IApplyService applyService = SpringUtils.getBean(IApplyService.class);
    private static  final IActTaskService actTaskService = SpringUtils.getBean(IActTaskService.class);
    private static  final ITestLeaveService testLeaveService = SpringUtils.getBean(ITestLeaveService.class);
    private static  final IActTaskServiceWrapper actTask = SpringUtils.getBean(IActTaskServiceWrapper.class);
//    private static  final AsyncProcessService asyncProcessService = SpringUtils.getBean(AsyncProcessService.class);
    @Override
    public void notify(DelegateTask delegateTask) {
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
            taskNodeDataBo.setApplicant("dcws");
            taskNodeDataBo.setApplicantId(4L);
            taskNodeDataBo.setApplyDate(new Date());
            taskNodeDataBo.setApplyDept("四方精创123");
            taskNodeDataBo.setApplyType("19");
            taskNodeDataBo.setAssetUserId(4L);
            TaskNodeDataVo taskNodeDataVo = applyService.insertByBo(taskNodeDataBo);
            TestLeaveBo testLeaveBo = new TestLeaveBo();
            testLeaveBo.setLeaveDays(1);
            testLeaveBo.setLeaveType("1");
            testLeaveBo.setStartDate(new Date());
            testLeaveBo.setEndDate(new Date());
//            TestLeaveVo testLeaveVo = testLeaveService.insertByBo(testLeaveBo);
            StartProcessBo startProcessBo = new StartProcessBo();
            startProcessBo.setBusinessKey(String.valueOf(taskNodeDataVo.getId()));
            startProcessBo.setRouter("/task/approveTemplate/assetsApproves");
//            startProcessBo.setRouter("/task/template/leave");
            Map<String, Object> stringObjectHashMap = new HashMap<>();
            startProcessBo.setVariables(variables);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    //afterCommit() 默认在 主事务线程 中执行，若此处启动独立流程耗时较长，可能阻塞主线程
//                    CompletableFuture.runAsync(() -> {
                        // startIndependentProcess()方法中启动流程和查询流程任务在一个事务中（有注解），如果流程启动后查询不到流程任务
                        // 有可能是启动流程的事务未提交导致（可能是flowable的事务机制导致调用外部service方法的时候事务提交问题导致）
                        // 这里使用afterCommit后，调用外部service方法启动流程，查询流程任务查询不到的问题解决，
                        // 确保 startIndependentProcess() 使用 REQUIRES_NEW，避免与主流程事务共享连接
                        //查询任务时使用新事务，确保能读取到已提交的数据
                        Map<String, Object> stringObjectMap = actTask.startProcessInNewTransaction(startProcessBo);// 调用包裹方法
                        /*Future<Map<String, Object>> mapFuture = asyncProcessService.startIndependentProcessAsync(startProcessBo);
                        Map<String, Object> stringObjectMap = null;
                        try {
                            stringObjectMap = mapFuture.get();
                        } catch (Exception e) {
                            log.info("流程启动失败： "+e.getMessage(),e);
                        }*//*catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        }*/
                        //        Map<String, Object> stringObjectMap = actTaskService.startWorkFlow(startProcessBo);
                        if (!CollectionUtil.isEmpty(stringObjectMap)) {
                            CompleteTaskBo completeTaskBo = new CompleteTaskBo();
                            completeTaskBo.setTaskId(String.valueOf(stringObjectMap.get("taskId")));
                            List objects = new ArrayList<String>();
                            objects.add("1");
                            completeTaskBo.setMessageType(objects);
                            completeTaskBo.setVariables(variables);
                            actTaskService.completeTask(completeTaskBo);
                        } else {
                            // 记录失败记录，待auto处理启动流程 TODO
                            log.info("待auto处理启动流程");
                        }
//                    });
                }
            });


//        } catch (Exception e) {
//            log.error("An error occurred while calling the external system", e);
            // 抛出BPMN错误，触发错误边界事件
//            throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: " + e.getMessage());
//        }
    }




}
