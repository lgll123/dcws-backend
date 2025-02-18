package com.formssi.generator.controller;

import com.formssi.common.core.domain.R;
import com.formssi.generator.domain.DatasourceConfig;
import com.formssi.generator.param.GeneratorParam;
import com.formssi.generator.service.IDatasourceConfigService;
import com.formssi.generator.service.IGeneratorService;
import com.formssi.generator.sql.CodeFile;
import com.formssi.generator.sql.GeneratorConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author tanghc
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/generate")
public class GeneratorController {

    private final IDatasourceConfigService datasourceConfigService;

    private final IGeneratorService generatorService;

    /**
     * 生成代码
     *
     * @param generatorParam 生成参数
     * @return 返回代码内容
     */
    @PostMapping("/code")
    public R<List<CodeFile>> code(@RequestBody GeneratorParam generatorParam) {
        int datasourceConfigId = generatorParam.getDatasourceConfigId();
        DatasourceConfig datasourceConfig = datasourceConfigService.getById(datasourceConfigId);
        GeneratorConfig generatorConfig = GeneratorConfig.build(datasourceConfig);
        return R.ok(generatorService.generate(generatorParam, generatorConfig));
    }

}
