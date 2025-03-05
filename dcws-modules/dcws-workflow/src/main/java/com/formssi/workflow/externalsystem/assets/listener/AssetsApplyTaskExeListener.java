package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.convert.Convert;
import com.formssi.common.core.service.DeptService;
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
    private DeptService deptService;
    @Autowired
    private ISysDeptService sysDeptService;
    @Override
    public void notify(DelegateExecution delegateTask) {
        try{
            Long deptId = LoginHelper.getDeptId();
            SysDeptVo sysDeptVo = sysDeptService.selectDeptById(deptId);
            Long respLeader = deptService.selectDeptRespLeaderById(Convert.toStr(deptId));
            Long leader = deptService.selectDeptLeaderById(Convert.toStr(deptId));
            delegateTask.setVariable("leader", leader);
            delegateTask.setVariable("respLeader", respLeader);
            delegateTask.setVariable("userId", LoginHelper.getUserId());
            if(respLeader==null || leader==null){
                log.error("An error occurred while AssetsApplyTaskExeListener respLeader: "+respLeader+" leader:"+leader);
            }
        } catch (Exception e) {
            log.error("An error occurred while AssetsApplyTaskExeListener", e);
        }
    }




}
