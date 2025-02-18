package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.DcwsApprove;
import com.formssi.workflow.domain.DcwsHis;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsHis.class)
public class DcwsHisVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 办理人id
     */
    private String userId;

    /**
     * 办理人名称
     */
    private String userName;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 开始时间
     */
    private Date createTime;

    /**
     * 结束时间
     */
    private Date updateTime;

}
