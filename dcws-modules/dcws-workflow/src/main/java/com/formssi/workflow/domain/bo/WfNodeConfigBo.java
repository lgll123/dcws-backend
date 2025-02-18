package com.formssi.workflow.domain.bo;

import com.formssi.workflow.domain.WfNodeConfig;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.core.validate.AddGroup;
import com.formssi.common.core.validate.EditGroup;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.*;

/**
 * 节点配置业务对象 wf_node_config
 *
 * @author may
 * @date 2024-03-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = WfNodeConfig.class, reverseConvertGenerate = false)
public class WfNodeConfigBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 表单id
     */
    private Long formId;

    /**
     * 表单类型
     */
    private String formType;

    /**
     * 节点名称
     */
    @NotBlank(message = "节点名称不能为空", groups = {AddGroup.class, EditGroup.class})
    private String nodeName;

    /**
     * 节点id
     */
    @NotBlank(message = "节点id不能为空", groups = {AddGroup.class, EditGroup.class})
    private String nodeId;

    /**
     * 流程定义id
     */
    @NotBlank(message = "流程定义id不能为空", groups = {AddGroup.class, EditGroup.class})
    private String definitionId;

    /**
     * 是否为申请人节点 （0是 1否）
     */
    @NotBlank(message = "是否为申请人节点不能为空", groups = {AddGroup.class, EditGroup.class})
    private String applyUserTask;

}
