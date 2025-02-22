package com.formssi.workflow.domain.bo;

import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.DcwsNormalTaskUser;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsNormalTaskUser.class, reverseConvertGenerate = false)
public class DcwsNormalTaskUserBo extends BaseEntity {

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

}
