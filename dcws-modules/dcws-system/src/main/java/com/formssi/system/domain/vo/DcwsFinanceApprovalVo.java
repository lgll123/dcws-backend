package com.formssi.system.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.formssi.system.domain.DcwsFinanceApproval;
import com.formssi.system.domain.SysUser;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AutoMapper(target = DcwsFinanceApproval.class)
public class DcwsFinanceApprovalVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 对象存储主键
     */
    private Long Id;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 会计1
     */
    private Long accountantFirst;

    /**
     * 会计1名称
     */
    private String accountantFirstName;

    /**
     * 会计2
     */
    private Long accountantSecond;

    /**
     * 会计2名称
     */
    private String accountantSecondName;

    /**
     * 总账
     */
    private Long generalLedger;

    /**
     * 总账名称
     */
    private String generalLedgerName;

    /**
     * 税务主管
     */
    private Long taxCommissioner;

    /**
     * 税务主管名称
     */
    private String taxCommissionerName;

    /**
     * 财务经理
     */
    private Long financialManager;

    /**
     * 财务经理名称
     */
    private String financialManagerName;

    /**
     * 财务负责人
     */
    private Long financialDirector;

    /**
     * 财务负责人名称
     */
    private String financialDirectorName;

    /**
     * 出纳1
     */
    private Long cashierFirst;

    /**
     * 出纳1名称
     */
    private String cashierFirstName;

    /**
     * 出纳2
     */
    private Long cashierSecond;

    /**
     * 出纳2名称
     */
    private String cashierSecondName;

}