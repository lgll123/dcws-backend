package com.formssi.workflow.domain.bo;

import com.formssi.workflow.domain.DcwsBaseEntity;
import com.formssi.workflow.domain.DcwsNormalTaskHandleHis;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsNormalTaskHandleHis.class, reverseConvertGenerate = false)
public class DcwsNormalTaskHandleHisBo extends DcwsBaseEntity {

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
