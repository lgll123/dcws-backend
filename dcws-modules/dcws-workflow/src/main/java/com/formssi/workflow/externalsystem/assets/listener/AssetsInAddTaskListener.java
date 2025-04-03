package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.DcwsAssetsCheckOutBo;
import com.formssi.workflow.domain.vo.DcwsAssetsCheckOutVo;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
import com.formssi.workflow.externalsystem.exception.ApiCallException;
import com.formssi.workflow.service.IAssetsCheckOutRecordService;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.formssi.workflow.common.enums.AssetsCheckStatusEnum.*;
import static com.formssi.workflow.common.enums.AssetsCheckTypeEnum.CHECK_TYPE_5;

/**
 * 资产导入，向资产系统新增一条资产
 */
@Slf4j
@Component("AssetsInAddTaskListener")
public class AssetsInAddTaskListener implements TaskListener {
    private static final IAssetsCheckOutRecordService assetsCheckOutRecordService = SpringUtils.getBean(IAssetsCheckOutRecordService.class);
    private static final IExternalSystemAPIStrategy instance = SpringUtils.getBean("assets" + IExternalSystemAPIStrategy.BASE_NAME);
    // 线程池配置
    private static final ThreadPoolExecutor typeExecutor = new ThreadPoolExecutor(
            1, // 核心线程数
            5, // 最大线程数
            60L, TimeUnit.SECONDS, // 空闲线程存活时间
            new SynchronousQueue<>(), // 直接传递任务队列
            new ThreadPoolExecutor.CallerRunsPolicy() // 饱和策略
    );
    // spring线程池配置
    /*@Bean(name = "assetsTaskExecutor")
    public ThreadPoolTaskExecutor typeExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);                   // 核心线程数
        executor.setMaxPoolSize(5);                    // 最大线程数
        executor.setKeepAliveSeconds(60);              // 空闲线程存活时间（秒）
        executor.setQueueCapacity(0);                  // 使用SynchronousQueue等效配置
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("Asset-Processor-");  // 线程名前缀
        executor.setWaitForTasksToCompleteOnShutdown(true); // 关闭时等待任务完成
        executor.setAwaitTerminationSeconds(30);       // 等待终止时间
        executor.initialize();  // 必须显式初始化
        return executor;
    }
    @PreDestroy
    public void destroy() {
        if (typeExecutor != null) {
            typeExecutor.shutdown();
            log.info("资产处理线程池关闭完成");
        }
    }
    @Autowired
    @Qualifier("assetsTaskExecutor")
    private ThreadPoolTaskExecutor typeExecutor;*/
    private final TransactionTemplate transactionTemplate;  // 编程式事务模板
    public AssetsInAddTaskListener(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        try {
            Object entity = variables.get("entity");
            if (ObjectUtil.isEmpty(entity)) return;

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> taskNodeData = mapper.readValue(JSONUtil.toJsonStr(entity), Map.class);
            if (ObjectUtil.isEmpty(taskNodeData)) return;

            Map<String, Object> map = mapper.readValue(Convert.toStr(taskNodeData.get("applyDetail")), Map.class);
            String taskNodeDataId = Convert.toStr(taskNodeData.get("id"));
            Long assetUserId = Convert.toLong(taskNodeData.get("assetUserId"));

            // 统一处理集合数据
            List<Map<String, Object>> hardware = selectByOptional(map.get("hardware"));
            List<Map<String, Object>> licenses = selectByOptional(map.get("licenses"));
            List<Map<String, Object>> accessories = selectByOptional(map.get("accessories"));
            List<Map<String, Object>> components = selectByOptional(map.get("components"));
            List<Map<String, Object>> consumables = selectByOptional(map.get("consumables"));

            List<CompletableFuture<Void>> futures = new ArrayList<>();

            if (!CollectionUtil.isEmpty(hardware)) futures.add(processAssetsAsync("hardware", hardware, taskNodeDataId, assetUserId, this::createHardwareRequest));
            if (!CollectionUtil.isEmpty(licenses)) futures.add(processAssetsAsync("licenses", licenses, taskNodeDataId, assetUserId, this::createLicensesRequest));
            if (!CollectionUtil.isEmpty(accessories)) futures.add(processAssetsAsync("accessories", accessories, taskNodeDataId, assetUserId, this::createAccessoryRequest));
            if (!CollectionUtil.isEmpty(components)) futures.add(processAssetsAsync("components", components, taskNodeDataId, assetUserId, this::createComponentsRequest));
            if (!CollectionUtil.isEmpty(consumables)) futures.add(processAssetsAsync("consumables", consumables, taskNodeDataId, assetUserId, this::createConsumablesRequest));
            // 仅在有任务时触发等待
            if (!CollectionUtil.isEmpty(futures)) {
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }
        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
        }
    }


    private CompletableFuture<Void> processAssetsAsync(String assetType,
                                                       List<Map<String, Object>> items,
                                                       String taskNodeDataId,
                                                       Long assetUserId,
                                                       Function<Map<String, Object>, Map<String, String>> requestMapper) {
        /*if (CollectionUtil.isEmpty(items)) {
            return CompletableFuture.completedFuture(null);
        }*/
        // CompletableFuture 并发
        return CompletableFuture.runAsync(() -> items.forEach(item -> {
            DcwsAssetsCheckOutBo bo = createBaseBo(taskNodeDataId, assetUserId);
            bo.setAssetsType(assetType);
            bo.setAssetsDetail(JSONUtil.toJsonStr(item));
            try {
                Map<String, String> requestBody = requestMapper.apply(item);
                Map<String, Object> responseMap = instance.process(requestBody, assetType, "post");
                handleResponse(bo, responseMap);
            } catch (ApiCallException e) {
                handleApiException(bo, e);
            } catch (Exception e) {
                handleGenericException(bo, e);
            } finally {
                saveAssetsInRecord(bo);
            }
        }), typeExecutor);
    }

    // 各类型请求参数构造方法
    private Map<String, String> createHardwareRequest(Map<String, Object> item) {
        Map<String, String> request = new HashMap<>();
        request.put("asset_tag", Convert.toStr(item.get("assetTag")));
        request.put("status_id", "7");
        request.put("model_id", Convert.toStr(item.get("modelId")));
        request.put("name", Convert.toStr(item.get("name")));
        request.put("serial", Convert.toStr(item.get("serialNumber")));
        request.put("rtd_location_id", Convert.toStr(item.get("locationId")));
        request.put("purchase_cost", Convert.toStr(item.get("purchaseCost")));
        request.put("supplier_id", Convert.toStr(item.get("supplierId")));
        return request;
    }
    private Map<String, String> createAccessoryRequest(Map<String, Object> item) {
        Map<String, String> request = new HashMap<>();
        request.put("name", Convert.toStr(item.get("name")));
        request.put("qty", Convert.toStr(item.get("num")));
        request.put("category_id", Convert.toStr(item.get("categoryId")));
        request.put("model_number", Convert.toStr(item.get("modelNumber")));
        request.put("location_id", Convert.toStr(item.get("locationId")));
        request.put("purchase_cost", Convert.toStr(item.get("purchaseCost")));
        request.put("purchase_date", Convert.toStr(item.get("purchaseDate")));
        request.put("supplier_id", Convert.toStr(item.get("supplierId")));
        return request;
    }
    private Map<String, String> createComponentsRequest(Map<String, Object> item) {
        Map<String, String> request = new HashMap<>();
        request.put("name", Convert.toStr(item.get("name")));
        request.put("qty", Convert.toStr(item.get("num")));
        request.put("category_id", Convert.toStr(item.get("categoryId")));
        request.put("serial", Convert.toStr(item.get("serial")));
        request.put("location_id", Convert.toStr(item.get("locationId")));
        request.put("supplier_id", Convert.toStr(item.get("supplierId")));
        request.put("purchase_cost", Convert.toStr(item.get("purchaseCost")));
        return request;
    }
    private Map<String, String> createConsumablesRequest(Map<String, Object> item) {
        Map<String, String> request = new HashMap<>();
        request.put("name", Convert.toStr(item.get("name")));
        request.put("qty", Convert.toStr(item.get("num")));
        request.put("category_id", Convert.toStr(item.get("categoryId")));
        request.put("supplier_id", Convert.toStr(item.get("supplierId")));
        request.put("location_id", Convert.toStr(item.get("locationId")));
        request.put("model_number", Convert.toStr(item.get("modelNumber")));
        request.put("purchase_cost", Convert.toStr(item.get("purchaseCost")));
        return request;
    }
    private Map<String, String> createLicensesRequest(Map<String, Object> item) {
        Map<String, String> request = new HashMap<>();
        request.put("name", Convert.toStr(item.get("name")));
        request.put("seats", Convert.toStr(item.get("seats")));
        request.put("category_id", Convert.toStr(item.get("categoryId")));
        request.put("serial", Convert.toStr(item.get("serial")));
        request.put("manufacturer_id", Convert.toStr(item.get("manufacturerId")));
        request.put("license_name", Convert.toStr(item.get("licenseName")));
        request.put("license_email", Convert.toStr(item.get("licenseEmail")));
        request.put("expiration_date", Convert.toStr(item.get("expirationDate")));
        return request;
    }
    // 公共处理方法
    private void handleResponse(DcwsAssetsCheckOutBo bo, Map<String, Object> response) {
        bo.setMessage(Convert.toStr(response.get("messages")));
        bo.setCode(Convert.toStr(response.get("status")));
        if ("error".equals(bo.getCode())) {
            bo.setStatus(CHECK_STATUS_0.getCode());
        }
    }
    private void handleApiException(DcwsAssetsCheckOutBo bo, ApiCallException e) {
        bo.setStatus(CHECK_STATUS_2.getCode());
        bo.setMessage(e.getMessage());
        log.error("API调用失败", e);
    }

    private void handleGenericException(DcwsAssetsCheckOutBo bo, Exception e) {
        bo.setStatus(CHECK_STATUS_0.getCode());
        bo.setMessage(e.getMessage());
        log.error("处理过程中发生异常", e);
    }
    private DcwsAssetsCheckOutBo createBaseBo(String taskNodeDataId, Long assetUserId) {
        DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
        bo.setTaskNodeDataId(taskNodeDataId);
        bo.setCheckOutUser(assetUserId.toString());
        bo.setAutoHandleNum(0);
        bo.setCheckOutIn("3");
        bo.setCheckType(CHECK_TYPE_5.getCode());
        bo.setStatus(CHECK_STATUS_1.getCode()); // 默认成功状态
        return bo;
    }
    // Optional 获取处理列表
    private List<Map<String,Object>> selectByOptional(Object dataList) {
        return  Optional.ofNullable(dataList)
                .map(obj -> (List<Map<String, Object>>) obj)
                .orElse(Collections.emptyList());
    }

    // 统一保存记录
    private void saveAssetsInRecord(DcwsAssetsCheckOutBo bo) {
        try {
//            assetsCheckOutRecordService.insertByBo(bo);
            saveAssetsInRecordInNewTransaction(bo);// 异步处理时，使用独立事务操作数据库
            if(bo.getId()==null){
                log.info("资产入库记录新增失败：id == null");
            }
            bo.setId(null);
        } catch (Exception e) {
            log.error("资产入库记录新增失败", e);
        }
    }
    /**
     * 在新事务中调用原方法，异常仅回滚新事务
     * 新增入库记录
     */
    public DcwsAssetsCheckOutVo saveAssetsInRecordInNewTransaction(DcwsAssetsCheckOutBo bo) {
        return transactionTemplate.execute(status -> {
            try {
                return assetsCheckOutRecordService.insertByBo(bo);  // 调用原方法（已注解@Transactional）
            } catch (Exception e) {
                status.setRollbackOnly();  // 标记事务回滚
                // 记录日志，但无需处理（新事务已标记回滚）
                // 记录失败记录，待auto处理启动流程 TODO
                log.info("新增入库记录失败: " + e.getMessage());
                return null;  // 返回值
            }
        });
    }
}
