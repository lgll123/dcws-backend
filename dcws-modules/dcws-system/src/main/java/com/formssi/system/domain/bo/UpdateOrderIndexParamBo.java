package com.formssi.system.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author tanghc
 */
@Data
public class UpdateOrderIndexParamBo {

    @NotNull(message = "主键id不能为空")
    private Long id;

    @NotNull(message = "排序顺序不能为空")
    private Integer orderIndex;

}
