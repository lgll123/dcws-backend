package com.formssi.generator.param;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/28 15:41
 */
@Data
public class GeneratorCompareParam {

    /**
     * 当前版本
     */
    @NotNull(message = "当前版本不能为空")
    private Integer currentVersion;

    /**
     * 比较版本
     */
    @NotNull(message = "比较版本不能为空")
    private Integer compareVersion;

}
