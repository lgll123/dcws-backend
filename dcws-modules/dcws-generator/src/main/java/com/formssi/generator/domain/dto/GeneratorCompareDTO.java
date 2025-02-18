package com.formssi.generator.domain.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/28 16:23
 */
@Data
public class GeneratorCompareDTO {

    /**
     * 当前版本map
     */
    private Map<String, String> currentVersionMap;

    /**
     * 比较版本map
     */
    private Map<String, String> compareVersionMap;

}
