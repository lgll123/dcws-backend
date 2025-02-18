package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.formssi.workflow.domain.DcwsApprove;
import com.formssi.workflow.domain.TestLeave;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsApprove.class)
public class DcwsApproveVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @ExcelProperty(value = "主键")
    private Long taskId;

    /**
     * 任务名称
     */
    @ExcelProperty(value = "任务名称")
    private String taskName;

    /**
     * 办理人id
     */
    @ExcelProperty(value = "办理人id")
    private String userId;

    /**
     * 处理状态
     */
    @ExcelProperty(value = "处理状态")
    private String status;

    /**
     * 父级任务id
     */
    @ExcelProperty(value = "父级任务id")
    private String parentTaskId;

    /**
     * 业务id
     */
    @ExcelProperty(value = "业务id")
    private String businessKey;

    /**
     * 处理说明
     */
    @ExcelProperty(value = "处理说明")
    private String remark;

    /**
     * 开始时间
     */
    @ExcelProperty(value = "开始时间")
    private Date createTime;

    /**
     * 结束时间
     */
    @ExcelProperty(value = "结束时间")
    private Date updateTime;

    /**
     * 创建人
     */
    @ExcelProperty(value = "创建人")
    private String createBy;

}
