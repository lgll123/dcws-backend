package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.util.ObjectUtil;
import com.formssi.system.domain.vo.DcwsFinanceApprovalVo;
import com.formssi.system.domain.vo.SysDeptVo;
import com.formssi.system.domain.vo.SysUserVo;
import com.formssi.system.service.IFinanceApprovalService;
import com.formssi.system.service.ISysDeptService;
import com.formssi.system.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * 报销任务启动
 */
@Slf4j
@Component("ReimbursementApplyTaskExeListener")
public class ReimbursementApplyTaskExeListener implements ExecutionListener {
    @Autowired
    private ISysDeptService sysDeptService;
    @Autowired
    private ISysUserService iSysUserService;
    @Autowired
    private IFinanceApprovalService iFinanceApprovalService;
    @Override
    public void notify(DelegateExecution delegateTask) {
        try{
            Map<String, Object> variables = delegateTask.getVariables();
            SysUserVo sysUserVo = iSysUserService.selectUserById(Long.valueOf(variables.get("reimbursementId").toString()));
            SysDeptVo sysDeptVo = sysDeptService.selectDeptById(sysUserVo.getDeptId());
            DcwsFinanceApprovalVo dcwsFinanceApprovalVo = iFinanceApprovalService.selectFinanceApprovalByDeptId(sysUserVo.getDeptId());
            delegateTask.setVariable("leader", sysDeptVo.getLeader());
            delegateTask.setVariable("respLeader", sysDeptVo.getRespLeader());
            delegateTask.setVariable("userId", sysUserVo.getUserId());
            Object userList = variables.get("userList");
            if (ObjectUtil.isEmpty(userList)) {
                delegateTask.setVariable("needDeptApproval","N");
            }else{
                delegateTask.setVariable("needDeptApproval","Y");
            };
            if(!Objects.isNull(dcwsFinanceApprovalVo)){
                delegateTask.setVariable("accountantFirst", Objects.isNull(dcwsFinanceApprovalVo.getAccountantFirst())?0:dcwsFinanceApprovalVo.getAccountantFirst());
                delegateTask.setVariable("accountantSecond",Objects.isNull(dcwsFinanceApprovalVo.getAccountantSecond())?0:dcwsFinanceApprovalVo.getAccountantSecond());
                delegateTask.setVariable("generalLedger",dcwsFinanceApprovalVo.getGeneralLedger());
                delegateTask.setVariable("taxCommissioner",dcwsFinanceApprovalVo.getTaxCommissioner());
                delegateTask.setVariable("financialManager",dcwsFinanceApprovalVo.getFinancialManager());
                delegateTask.setVariable("financialDirector",dcwsFinanceApprovalVo.getFinancialDirector());
                delegateTask.setVariable("cashierFirst",dcwsFinanceApprovalVo.getCashierFirst());
                delegateTask.setVariable("cashierSecond",dcwsFinanceApprovalVo.getCashierSecond());
            }
            if(sysDeptVo.getLeader()==null || sysDeptVo.getRespLeader()==null){
                log.error("An error occurred while ReimbursementApplyTaskExeListener respLeader: "+sysDeptVo.getRespLeader()+" leader:"+sysDeptVo.getLeader());
            }
        } catch (Exception e) {
            log.error("An error occurred while ReimbursementApplyTaskExeListener", e);
        }
    }
}
