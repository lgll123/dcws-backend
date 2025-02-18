package com.formssi.generator.service;

import com.formssi.generator.param.GeneratorParam;
import com.formssi.generator.sql.CodeFile;
import com.formssi.generator.sql.GeneratorConfig;

import java.util.List;
import java.util.Map;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/8 10:31
 */
public interface IGeneratorService {

    /**
     * 生成代码内容,map的
     *
     * @param generatorParam 生成参数
     * @param generatorConfig 数据源配置
     * @return 一张表对应多个模板
     */
    List<CodeFile> generate(GeneratorParam generatorParam, GeneratorConfig generatorConfig);

    /**
     * 代码预览
     * @param generatorParam 生成参数
     * @param generatorConfig 生成模版
     * @param datasourceConfigId 数据源id
     * @return 返回代码生成map
     */
    Map<String, String> codePreview(GeneratorParam generatorParam, GeneratorConfig generatorConfig, int datasourceConfigId);
}
