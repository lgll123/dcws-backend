package com.formssi.system.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author tanghc
 */
@Data
public class DocFolderAddParamBo {
    @NotNull(message = "模块id不能为空")
    private Long moduleId;

    @NotBlank(message = "模块id不能为空")
    private String name;

    private Long parentId;
}
