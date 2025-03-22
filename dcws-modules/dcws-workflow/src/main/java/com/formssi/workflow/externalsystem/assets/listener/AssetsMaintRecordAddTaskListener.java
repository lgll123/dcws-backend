package com.formssi.workflow.externalsystem.assets.listener;

import com.formssi.common.core.utils.SpringUtils;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.system.domain.vo.SysDeptVo;
import com.formssi.system.service.ISysDeptService;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 在填写好维修单后，想资产系统新增一条资产维修记录
 */
@Slf4j
@Component("AssetsMaintRecordAddTaskListener")
public class AssetsMaintRecordAddTaskListener implements TaskListener {
    private static final IExternalSystemAPIStrategy instance = SpringUtils.getBean("assets" + IExternalSystemAPIStrategy.BASE_NAME);
    @Override
    public void notify(DelegateTask delegateTask) {
        try{
            String url = "/maintenances";
            Map<String, String> requestBodyMap = new HashMap<>();
            requestBodyMap.put("title","title-维护");
            requestBodyMap.put("asset_id","1");
            requestBodyMap.put("supplier_id","2");
            requestBodyMap.put("asset_maintenance_type","维护");
            requestBodyMap.put("start_date","2025-03-06");
            Map<String, Object> responseMap = instance.process(requestBodyMap,url,"post");

        } catch (Exception e) {
            log.error("An error occurred while AssetsApplyTaskExeListener", e);
        }
    }




}
