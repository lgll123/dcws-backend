package com.formssi.generator.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.formssi.common.core.domain.R;
import com.formssi.generator.domain.DatasourceConfig;
import com.formssi.generator.domain.GenerateHistory;
import com.formssi.generator.domain.TemplateConfig;
import com.formssi.generator.param.GeneratorHistoryParam;
import com.formssi.generator.service.IDatasourceConfigService;
import com.formssi.generator.service.IGenerateHistoryService;
import com.formssi.generator.service.ITemplateConfigService;
import com.formssi.generator.model.vo.GenerateHistoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/history")
public class GenerateHistoryController {

    private final IGenerateHistoryService generateHistoryService;

    private final IDatasourceConfigService datasourceConfigService;

    private final ITemplateConfigService templateConfigService;

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    @GetMapping("/list")
    public R<List<GenerateHistoryVO>> listAll() {
        List<GenerateHistory> generateHistories = generateHistoryService.listAll();
        List<GenerateHistoryVO> generateHistoryVOS = generateHistories.stream()
                .map(generateHistory -> {
                    GenerateHistoryVO generateHistoryVO = new GenerateHistoryVO();
                    GeneratorHistoryParam historyParam = JSON.parseObject(generateHistory.getConfigContent(), GeneratorHistoryParam.class);

                    List<String> datasourceInfoList = getDatasourceListByIds(historyParam.getDatasourceConfigIds());
                    if (CollectionUtils.isEmpty(datasourceInfoList)) {
                        return null;
                    }
                    List<String> templateNames = this.listTemplateNames(historyParam.getTemplateConfigIdList());
                    generateHistoryVO.setGenerateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(generateHistory.getGenerateTime()));
                    generateHistoryVO.setConfigContent(historyParam);
                    generateHistoryVO.setDatasourceList(datasourceInfoList);
                    generateHistoryVO.setTemplateNames(templateNames);
                    generateHistoryVO.setVersion(generateHistory.getVersion());
                    return generateHistoryVO;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return R.ok(generateHistoryVOS);
    }

    /**
     * 根据数据源id列表获取数据源名称
     * @param datasourceConfigIds 数据源id列表
     * @return 返回数据源名称
     */
    private List<String> getDatasourceListByIds(List<Integer> datasourceConfigIds) {
        List<DatasourceConfig> datasourceConfigList = datasourceConfigService.getByIdList(datasourceConfigIds);
        if (CollectionUtils.isEmpty(datasourceConfigList)) {
            return Collections.emptyList();
        }

        List<String> dataSourceNameList = new ArrayList<>();
        String tpl = "%s(%s:%s)";
        datasourceConfigList.forEach(item -> {
            String dataSourceName = String.format(tpl, item.getDbName(), item.getHost(), item.getPort());
            dataSourceNameList.add(dataSourceName);
        });

        return dataSourceNameList;
    }

    private String getDatasourceInfo(int datasourceConfigId) {
        DatasourceConfig datasourceConfig = datasourceConfigService.getById(datasourceConfigId);
        if (datasourceConfig == null) {
            return null;
        }
        String tpl = "%s(%s:%s)";
        return String.format(tpl, datasourceConfig.getDbName(), datasourceConfig.getHost(), datasourceConfig.getPort());
    }

    private List<String> listTemplateNames(List<Integer> idList) {
        return templateConfigService.listTemplate(idList)
                .stream()
                .map(TemplateConfig::getName)
                .collect(Collectors.toList());
    }

}
