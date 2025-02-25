package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.formssi.workflow.domain.DcwsNormalTaskHandleHis;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsNormalTaskHandleHis.class)
public class DcwsNormalTaskHandleHisVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    @ExcelProperty(value = "任务ID")
    private Long taskId;

    /**
     * 处理状态
     */
    @ExcelProperty(value = "处理状态")
    private String status;

    /**
     * 处理意见
     */
    @ExcelProperty(value = "处理意见")
    private String comment;

    /**
     * 办理人id
     */
    @ExcelProperty(value = "办理人id")
    private Long userId;

    /**
     * 办理人名称
     */
    @ExcelProperty(value = "办理人名称")
    private String userName;

    /**
     * 办理人工号
     */
    @ExcelProperty(value = "办理人工号")
    private String empNo;

    /**
     * 文件ID
     */
    @ExcelProperty(value = "文件ID")
    private String fileId;

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
     * 是否显示
     */
    @ExcelProperty(value = "是否显示")
    private String isDisplay;

}
