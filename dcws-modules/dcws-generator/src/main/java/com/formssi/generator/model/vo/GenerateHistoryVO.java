package com.formssi.generator.model.vo;

import com.formssi.generator.param.GeneratorHistoryParam;
import lombok.Data;

import  com.formssi.generator.param.GeneratorParam;
import java.util.List;

/**
 * @author tanghc
 */
@Data
public class GenerateHistoryVO {

    private GeneratorHistoryParam configContent;

    private String generateTime;

    private List<String> datasourceList;
    private List<String> templateNames;

    private Integer version;

}
