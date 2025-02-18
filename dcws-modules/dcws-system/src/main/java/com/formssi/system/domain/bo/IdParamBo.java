package com.formssi.system.domain.bo;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

/**
 * @author tanghc
 */
@Data
public class IdParamBo {

    @NotNull(message = "id不能为空")
    private Long id;

}
