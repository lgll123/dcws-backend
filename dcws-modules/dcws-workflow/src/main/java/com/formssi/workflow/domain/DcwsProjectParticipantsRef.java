package com.formssi.workflow.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dcws_project_participants_ref")
public class DcwsProjectParticipantsRef extends BaseEntity {

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
