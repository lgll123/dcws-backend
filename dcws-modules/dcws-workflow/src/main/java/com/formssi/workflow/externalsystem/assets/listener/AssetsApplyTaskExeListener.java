package com.formssi.workflow.externalsystem.assets.listener;

import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.system.domain.vo.SysDeptVo;
import com.formssi.system.service.ISysDeptService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 采购任务启动
 */
@Slf4j
@Component("AssetsApplyTaskExeListener")
public class AssetsApplyTaskExeListener implements ExecutionListener {
    @Autowired
    private ISysDeptService sysDeptService;
    @Override
    public void notify(DelegateExecution delegateTask) {
        try{
            SysDeptVo sysDeptVo = sysDeptService.selectDeptById(LoginHelper.getDeptId());
            delegateTask.setVariable("leader", sysDeptVo.getLeader());
            delegateTask.setVariable("respLeader", sysDeptVo.getRespLeader());
            delegateTask.setVariable("userId", LoginHelper.getUserId());
            if(sysDeptVo.getLeader()==null || sysDeptVo.getRespLeader()==null){
                log.error("An error occurred while AssetsApplyTaskExeListener respLeader: "+sysDeptVo.getRespLeader()+" leader:"+sysDeptVo.getLeader());
            }
        } catch (Exception e) {
            log.error("An error occurred while AssetsApplyTaskExeListener", e);
        }
    }




}
