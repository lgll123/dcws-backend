package com.formssi.workflow.domain.bo;

import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.DcwsProjectParticipantsRef;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsProjectParticipantsRef.class, reverseConvertGenerate = false)
public class DcwsProjectParticipantsRefBo extends BaseEntity {

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
