package com.formssi.workflow.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.workflow.domain.DcwsApprove;
import com.formssi.workflow.domain.DcwsUser;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = DcwsUser.class, reverseConvertGenerate = false)
public class DcwsUserBo extends BaseEntity {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 办理人id
     */
    private String userId;

}
