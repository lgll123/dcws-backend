package com.formssi.workflow.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.formssi.workflow.domain.DcwsNormalTaskUser;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;


@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = DcwsNormalTaskUser.class)
public class DcwsNormalTaskUserVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 办理人id
     */
    private String userId;

    /**
     * 办理人名称
     */
    private String userName;

    /**
     * 开始时间
     */
    private Date createTime;

    /**
     * 结束时间
     */
    private Date updateTime;

}
