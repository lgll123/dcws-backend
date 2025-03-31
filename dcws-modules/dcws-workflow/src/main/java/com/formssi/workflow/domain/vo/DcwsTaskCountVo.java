package com.formssi.workflow.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DcwsTaskCountVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 我的待办
     */
    private Long taskWaitingCount;

    /**
     * 我发起的
     */
    private Long myDocumentCount;

    /**
     * 我的已办
     */
    private Long taskFinishCount;

    /**
     * 抄送我的
     */
    private Long taskCopyListCount;



}
