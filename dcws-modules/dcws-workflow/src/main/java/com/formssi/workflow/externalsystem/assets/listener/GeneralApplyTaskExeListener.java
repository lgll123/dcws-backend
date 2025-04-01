package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.util.ObjectUtil;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.system.domain.vo.SysDeptVo;
import com.formssi.system.domain.vo.SysUserVo;
import com.formssi.system.service.ISysDeptService;
import com.formssi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 通用事项审批
 */
@Slf4j
@Component("GeneralApplyTaskExeListener")
public class GeneralApplyTaskExeListener implements ExecutionListener {
    @Autowired
    private ISysDeptService sysDeptService;
    @Autowired
    private ISysUserService iSysUserService;
    @Override
    public void notify(DelegateExecution delegateTask) {
        try{
            Map<String, Object> variables = delegateTask.getVariables();
            SysUserVo sysUserVo = iSysUserService.selectUserById(LoginHelper.getUserId());
            SysDeptVo sysDeptVo = sysDeptService.selectDeptById(sysUserVo.getDeptId());
            delegateTask.setVariable("leader", sysDeptVo.getLeader());
            delegateTask.setVariable("respLeader", sysDeptVo.getRespLeader());
            delegateTask.setVariable("userId", sysUserVo.getUserId());
            Object userList = variables.get("userList");
            if (ObjectUtil.isEmpty(userList)) {
                delegateTask.setVariable("needDeptApproval","N");
            }else{
                delegateTask.setVariable("needDeptApproval","Y");
            };

            if(sysDeptVo.getLeader()==null || sysDeptVo.getRespLeader()==null){
                log.error("An error occurred while GeneralApplyTaskExeListener respLeader: "+sysDeptVo.getRespLeader()+" leader:"+sysDeptVo.getLeader());
            }
        } catch (Exception e) {
            log.error("An error occurred while GeneralApplyTaskExeListener", e);
        }
    }
}
