package com.formssi.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dcws_project_participants_ref")
public class DcwsProjectParticipantsRef extends DcwsBaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 参与人ID
     */
    private Long participantsId;

    /**
     * 参与人姓名
     */
    private String participantsName;

    /**
     * 参与人工号
     */
    private String participantsNo;

    /**
     * 参与类型
     */
    private String participantsType;


}
