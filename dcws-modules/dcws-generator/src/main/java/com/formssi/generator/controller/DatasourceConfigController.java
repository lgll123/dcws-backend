package com.formssi.generator.controller;

import com.formssi.common.core.domain.R;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.generator.config.DbTypeConfig;
import com.formssi.generator.domain.DatasourceConfig;
import com.formssi.generator.model.vo.TableQueryVO;
import com.formssi.generator.service.IDatasourceConfigService;
import com.formssi.generator.sql.*;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tanghc
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/datasource")
public class DatasourceConfigController {

    private final IDatasourceConfigService datasourceConfigService;

    @PostMapping("/add")
    public R<Void> add(@RequestBody DatasourceConfig datasourceConfig) {
        datasourceConfigService.insert(datasourceConfig);
        return R.ok();
    }

    @GetMapping("/list")
    public R<List<DatasourceConfig>> list() {
        List<DatasourceConfig> datasourceConfigList = datasourceConfigService.listAll();
        return R.ok(datasourceConfigList);
    }

    @PostMapping("/update")
    public R<Void> update(@RequestBody DatasourceConfig datasourceConfig) {
        datasourceConfigService.update(datasourceConfig);
        return R.ok();
    }

    @PostMapping("/del")
    public R<Void> del(@RequestBody DatasourceConfig datasourceConfig) {
        datasourceConfigService.delete(datasourceConfig);
        return R.ok();
    }

    @GetMapping("/table/{id}")
    public R<List<TableDefinition>> listTable(@PathVariable("id") int id) {
        DatasourceConfig dataSourceConfig = datasourceConfigService.getById(id);
        GeneratorConfig generatorConfig = GeneratorConfig.build(dataSourceConfig);
        SQLService service = SQLServiceFactory.build(generatorConfig);
        List<TableDefinition> list = service.getTableSelector(generatorConfig).getSimpleTableDefinitions();
        return R.ok(list);
    }

    /**
     * 根据条件查询
     * @param tableQueryVO 表查询
     * @return 返回表
     */
    @PostMapping("/table/getByDataSourceConfigId")
    public R<List<TableDefinition>> listTable(@RequestBody TableQueryVO tableQueryVO) {
        DatasourceConfig dataSourceConfig = datasourceConfigService.getById(tableQueryVO.getDatasourceConfigId());
        GeneratorConfig generatorConfig = GeneratorConfig.build(dataSourceConfig);
        SQLService service = SQLServiceFactory.build(generatorConfig);
        List<TableDefinition> list = service.getTableSelector(generatorConfig).getSimpleTableDefinitions();

        // 按照表名称过滤
        if (StringUtils.isNotEmpty(tableQueryVO.getTableName())) {
            list = list.stream().filter(item -> item.getTableName().contains(tableQueryVO.getTableName())).toList();
        }

        // 按照表描述过滤
        if (StringUtils.isNotEmpty(tableQueryVO.getComment())) {
            list = list.stream().filter(item -> item.getComment().contains(tableQueryVO.getComment())).toList();
        }

        return R.ok(list);
    }

    @PostMapping("/test")
    public R<Void> test(@RequestBody DatasourceConfig datasourceConfig) {
        String error = DBConnect.testConnection(GeneratorConfig.build(datasourceConfig));
        if (error != null) {
            return R.fail(error);
        }
        return R.ok();
    }

    @GetMapping("/dbtype")
    public R<List<DbTypeShow>> dbType(DatasourceConfig datasourceConfig) {
        List<DbTypeShow> dbTypeShowList = DbTypeConfig.getInstance()
                .getConnectConfigMap()
                .entrySet()
                .stream()
                .map(entry -> new DbTypeShow(entry.getValue().getName(), entry.getKey()))
                .collect(Collectors.toList());
        return R.ok(dbTypeShowList);
    }

    private static class DbTypeShow {
        private String label;
        private Integer dbType;

        public DbTypeShow(String label, Integer dbType) {
            this.label = label;
            this.dbType = dbType;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Integer getDbType() {
            return dbType;
        }

        public void setDbType(Integer dbType) {
            this.dbType = dbType;
        }
    }

}
