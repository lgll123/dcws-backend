package com.formssi.workflow.domain;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dcws_normal_task_handle_his")
public class DcwsNormalTaskHandleHis extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 处理状态
     */
    private String status;

    /**
     * 处理意见
     */
    private String comment;

    /**
     * 办理人id
     */
    private Long userId;

    /**
     * 办理人名称
     */
    private String userName;

    /**
     * 办理人工号
     */
    private String empNo;

    /**
     * 文件ID
     */
    private String fileId;

    /**
     * 开始时间
     */
    private Date createTime;

    /**
     * 结束时间
     */
    private Date updateTime;

    /**
     * 是否显示
     */
    private String isDisplay;



}
