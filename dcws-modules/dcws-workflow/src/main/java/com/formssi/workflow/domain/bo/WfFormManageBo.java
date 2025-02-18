package com.formssi.workflow.domain.bo;

import com.formssi.workflow.domain.WfFormManage;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 表单管理业务对象 wf_form_manage
 *
 * @author may
 * @date 2024-03-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = WfFormManage.class, reverseConvertGenerate = false)
public class WfFormManageBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = { EditGroup.class })
    private Long id;

    /**
     * 表单名称
     */
    @NotBlank(message = "表单Key不能为空", groups = { AddGroup.class, EditGroup.class })
    private String formKey;

    /**
     * 表单名称
     */
    @NotBlank(message = "表单名称不能为空", groups = { AddGroup.class, EditGroup.class })
    private String formName;

    /**
     * 表单类型
     */
    @NotBlank(message = "表单类型不能为空", groups = { AddGroup.class, EditGroup.class })
    private String formType;
    /**
     * 路由地址/表单ID
     */
    @NotBlank(message = "路由地址/表单ID不能为空", groups = { AddGroup.class, EditGroup.class })
    private String router;


    /**
     * 备注
     */
    private String remark;


}
