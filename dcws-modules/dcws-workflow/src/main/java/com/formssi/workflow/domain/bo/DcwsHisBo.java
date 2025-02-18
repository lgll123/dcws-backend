package com.formssi.workflow.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.DcwsApprove;
import com.formssi.workflow.domain.DcwsHis;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsHis.class, reverseConvertGenerate = false)
public class DcwsHisBo extends BaseEntity {

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
