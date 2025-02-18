package com.formssi.system.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author tanghc
 */
@Data
public class ModifyInfoParamBo {

    @NotNull(message = "文档主键id不能为空")
    private Long id;

    private String description;

    private String example;

}
