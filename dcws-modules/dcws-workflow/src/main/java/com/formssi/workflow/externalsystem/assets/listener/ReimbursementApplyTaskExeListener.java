package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.utils.DateUtils;
import com.formssi.system.domain.vo.DcwsFinanceApprovalVo;
import com.formssi.system.domain.vo.SysDeptVo;
import com.formssi.system.domain.vo.SysUserVo;
import com.formssi.system.service.IFinanceApprovalService;
import com.formssi.system.service.ISysDeptService;
import com.formssi.system.service.ISysUserService;
import com.formssi.workflow.common.enums.ApplyTypeEnum;
import com.formssi.workflow.domain.bo.DcwsInvoiceInfoBo;
import com.formssi.workflow.service.IApplyService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 费用报销任务启动
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
    @Autowired
    private IApplyService iApplyService;

    @Override
    public void notify(DelegateExecution delegateTask) {
        try {
            Map<String, Object> variables = delegateTask.getVariables();
            SysUserVo sysUserVo = iSysUserService.selectUserById(Long.valueOf(variables.get("reimbursementId").toString()));
            SysDeptVo sysDeptVo = sysDeptService.selectDeptById(sysUserVo.getDeptId());
            DcwsFinanceApprovalVo dcwsFinanceApprovalVo = iFinanceApprovalService.selectFinanceApprovalByDeptId(sysUserVo.getDeptId());
            delegateTask.setVariable("leader", sysDeptVo.getLeader());
            delegateTask.setVariable("respLeader", sysDeptVo.getRespLeader());
            delegateTask.setVariable("userId", sysUserVo.getUserId());
            Object userList = variables.get("userList");
            if (ObjectUtil.isEmpty(userList)) {
                delegateTask.setVariable("needDeptApproval", "N");
            } else {
                delegateTask.setVariable("needDeptApproval", "Y");
            }
            if (!Objects.isNull(dcwsFinanceApprovalVo)) {
                delegateTask.setVariable("accountantFirst", dcwsFinanceApprovalVo.getAccountantFirst());
                delegateTask.setVariable("accountantSecond", dcwsFinanceApprovalVo.getAccountantSecond());
                delegateTask.setVariable("generalLedger", dcwsFinanceApprovalVo.getGeneralLedger());
                delegateTask.setVariable("taxCommissioner", dcwsFinanceApprovalVo.getTaxCommissioner());
                delegateTask.setVariable("financialManager", dcwsFinanceApprovalVo.getFinancialManager());
                delegateTask.setVariable("financialDirector", dcwsFinanceApprovalVo.getFinancialDirector());
                delegateTask.setVariable("cashierFirst", dcwsFinanceApprovalVo.getCashierFirst());
                delegateTask.setVariable("cashierSecond", dcwsFinanceApprovalVo.getCashierSecond());
            }
            if (sysDeptVo.getLeader() == null || sysDeptVo.getRespLeader() == null) {
                log.error("An error occurred while ReimbursementApplyTaskExeListener respLeader: " + sysDeptVo.getRespLeader() + " leader:" + sysDeptVo.getLeader());
            }
            //保存发票信息
            Map<String, Object> entityInfo = (Map<String, Object>) variables.get("entity");
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> applyDetails = objectMapper.readValue(JSONUtil.toJsonStr(entityInfo.get("applyDetail")), Map.class);
            if (!Objects.isNull(applyDetails.get("invoiceDetail"))) {
                List<Map<String, Object>> invoiceDetail = objectMapper.readValue(JSONUtil.toJsonStr(applyDetails.get("invoiceDetail")), List.class);
                for (Map<String, Object> invoiceMap : invoiceDetail) {
                    if (Objects.isNull(iApplyService.getInvoiceInfoById((String) invoiceMap.get("invoiceId")))) {
                        DcwsInvoiceInfoBo invoiceInfoBo = new DcwsInvoiceInfoBo();
                        invoiceInfoBo.setBusinessKey((String) entityInfo.get("id"));
                        invoiceInfoBo.setApplyType(ApplyTypeEnum.of((String) entityInfo.get("applyType")).getDesc());
                        invoiceInfoBo.setAmount(new BigDecimal(String.valueOf(invoiceMap.get("amount"))));
                        invoiceInfoBo.setFileName((String) invoiceMap.get("invoiceName"));
                        invoiceInfoBo.setFileUrl((String) invoiceMap.get("fileUrl"));
                        invoiceInfoBo.setInvoiceId((String) invoiceMap.get("invoiceId"));
                        invoiceInfoBo.setApplicant(String.valueOf(entityInfo.get("applicant")));
                        invoiceInfoBo.setApplyDept(String.valueOf(entityInfo.get("applyDept")));
                        invoiceInfoBo.setApplyDate(DateUtils.parseDate(entityInfo.get("applyDate")));
                        iApplyService.insertInvoiceInfoBo(invoiceInfoBo);
                    }
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while ReimbursementApplyTaskExeListener", e);
        }
    }
}
