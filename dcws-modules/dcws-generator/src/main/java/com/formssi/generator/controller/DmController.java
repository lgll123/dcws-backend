package com.formssi.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import com.formssi.common.core.domain.R;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.web.core.BaseController;
import com.formssi.generator.domain.DatasourceConfig;
import com.formssi.generator.domain.DmSubTable;
import com.formssi.generator.domain.DmTable;
import com.formssi.generator.domain.DmTableColumn;
import com.formssi.generator.domain.dto.GeneratorCompareDTO;
import com.formssi.generator.param.GeneratorCompareParam;
import com.formssi.generator.param.GeneratorParam;
import com.formssi.generator.service.IDatasourceConfigService;
import com.formssi.generator.service.IDmSubTableService;
import com.formssi.generator.service.IDmTableService;
import com.formssi.generator.service.IGeneratorService;
import com.formssi.generator.sql.GeneratorConfig;
import com.formssi.generator.sql.TableDefinition;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

/**
 * 代码生成 操作处理
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/gen")
public class DmController extends BaseController {

    private final IDmTableService genTableService;
    private final IDatasourceConfigService datasourceConfigService;
    private final IGeneratorService generatorService;
    private final IDmSubTableService dmSubTableService;

    /**
     * 查询代码生成列表
     */
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/list")
    public TableDataInfo<DmTable> genList(DmTable dmTable, PageQuery pageQuery) {
        return genTableService.selectPageGenTableList(dmTable, pageQuery);
    }

    /**
     * 修改代码生成业务
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:query")
    @GetMapping(value = "/{tableId}")
    public R<Map<String, Object>> getInfo(@PathVariable Long tableId) {
        DmTable table = genTableService.selectGenTableById(tableId);
        // 查询子表数据
        List<DmSubTable> subTableList = dmSubTableService.getDmSubTableListByTableId(tableId);
        // 设置子表数据
        table.setSubTableList(subTableList);
        List<DmTable> tables = genTableService.selectGenTableAll();
        List<DmTableColumn> list = genTableService.selectGenTableColumnListByTableId(tableId);
        Map<String, Object> map = new HashMap<>(3);
        map.put("info", table);
        map.put("rows", list);
        map.put("tables", tables);
        return R.ok(map);
    }

    /**
     * 查询数据库列表
     */
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/db/list")
    public TableDataInfo<DmTable> dataList(DmTable dmTable, PageQuery pageQuery) {
        return genTableService.selectPageDbTableList(dmTable, pageQuery);
    }

    /**
     * 查询数据表字段列表
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:list")
    @GetMapping(value = "/column/{tableId}")
    public TableDataInfo<DmTableColumn> columnList(@PathVariable("tableId") Long tableId) {
        TableDataInfo<DmTableColumn> dataInfo = new TableDataInfo<>();
        List<DmTableColumn> list = genTableService.selectGenTableColumnListByTableId(tableId);
        dataInfo.setRows(list);
        dataInfo.setTotal(list.size());
        return dataInfo;
    }

    /**
     * 导入表结构（保存）
     *
     * @param tables 表名串
     */
    @SaCheckPermission("tool:gen:import")
    @Log(title = "代码生成", businessType = BusinessType.IMPORT)
    @PostMapping("/importTable")
    public R<Void> importTableSave(String tables, String dataName) {
        String[] tableNames = Convert.toStrArray(tables);
        // 查询表信息
        List<DmTable> tableList = genTableService.selectDbTableListByNames(tableNames, dataName);
        genTableService.importGenTable(tableList, dataName);
        return R.ok();
    }

    /**
     * 导入表结构（保存）
     * @param tables 表名串
     * @param datasourceConfigId 数据源id
     */
    @SaCheckPermission("tool:gen:import")
    @Log(title = "代码生成", businessType = BusinessType.IMPORT)
    @PostMapping("/importTableList")
    public R<Void> importTableSave(String tables, Integer datasourceConfigId) {
        String[] tableNames = Convert.toStrArray(tables);

        // 根据数据源id查询表列表
        List<TableDefinition> list = genTableService.getListByDatasourceConfigIdAndTableNames(datasourceConfigId, List.of(tableNames));

        // 按照表名过滤
        Set<String> tableNameSet = new HashSet<>(List.of(tableNames));
        if (!CollectionUtils.isEmpty(tableNameSet)) {
            list = list.stream().filter(item -> tableNameSet.contains(item.getTableName())).toList();
        }

        DatasourceConfig dataSourceConfig = datasourceConfigService.getById(datasourceConfigId);
        String dbName = dataSourceConfig.getDbName();

        // 导入表结构
        genTableService.importGenTableList(list, dbName, datasourceConfigId);

        return R.ok();
    }

    /**
     * 修改保存代码生成业务
     */
    @SaCheckPermission("tool:gen:edit")
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> editSave(@Validated @RequestBody DmTable dmTable) {
        genTableService.validateEdit(dmTable);
        genTableService.updateGenTable(dmTable);
        return R.ok();
    }

    /**
     * 删除代码生成
     *
     * @param tableIds 表ID串
     */
    @SaCheckPermission("tool:gen:remove")
    @Log(title = "代码生成", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tableIds}")
    public R<Void> remove(@PathVariable Long[] tableIds) {
        genTableService.deleteGenTableByIds(tableIds);
        return R.ok();
    }

    /**
     * 预览代码
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:preview")
    @GetMapping("/preview/{tableId}")
    public R<Map<String, String>> preview(@PathVariable("tableId") Long tableId) throws IOException {
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        return R.ok(dataMap);
    }

    /**
     * 代码预览
     *
     * @param generatorParam 生成参数
     * @return 返回代码内容
     */
    @SaCheckPermission("tool:gen:preview")
    @PostMapping("/codePreview")
    public R<Map<String, String>> codePreview(@RequestBody GeneratorParam generatorParam) {
        int datasourceConfigId = generatorParam.getDatasourceConfigId();
        DatasourceConfig datasourceConfig = datasourceConfigService.getById(datasourceConfigId);
        GeneratorConfig generatorConfig = GeneratorConfig.build(datasourceConfig);

//        byte[] data = genTableService.batchCodeGenerate(tableIdList, templateConfigIdList)

        return R.ok(genTableService.codePreview(generatorParam, generatorConfig, datasourceConfigId));
    }

    /**
     * 生成代码（下载方式）
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/download/{tableId}")
    public void download(HttpServletResponse response, @PathVariable("tableId") Long tableId) throws IOException {
        byte[] data = genTableService.downloadCode(tableId);
        genCode(response, data);
    }

    /**
     * 生成代码（自定义路径）
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/genCode/{tableId}")
    public R<Void> genCode(@PathVariable("tableId") Long tableId) {
        genTableService.generatorCode(tableId);
        return R.ok();
    }

    /**
     * 同步数据库
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:edit")
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @GetMapping("/synchDb/{tableId}")
    public R<Void> synchDb(@PathVariable("tableId") Long tableId) {
        genTableService.synchDb(tableId);
        return R.ok();
    }

    /**
     * 批量生成代码
     *
     * @param tableIdStr 表ID串
     */
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/batchGenCode")
    public void batchGenCode(HttpServletResponse response, String tableIdStr) throws IOException {
        String[] tableIds = Convert.toStrArray(tableIdStr);
        byte[] data = genTableService.downloadCode(tableIds);
        genCode(response, data);
    }

    /**
     * 批量代码生成
     *
     * @param tableIdStr 表ID串
     */
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/batchCodeGenerate")
    public void batchCodeGenerate(HttpServletResponse response, String tableIdStr, String templateConfigIds) throws IOException {
        // 表id列表
        List<Long> tableIdList = Arrays.stream(tableIdStr.split(",")).map(Long::parseLong).toList();
        // 模版id列表
        List<Integer> templateConfigIdList = Arrays.stream(templateConfigIds.split(",")).map(Integer::parseInt).toList();

        byte[] data = genTableService.batchCodeGenerate(tableIdList, templateConfigIdList);
        genCode(response, data);
    }

    /**
     * 生成zip文件
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"ruoyi.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IoUtil.write(response.getOutputStream(), false, data);
    }

    /**
     * 查询数据源名称列表
     */
    @SaCheckPermission("tool:gen:list")
    @GetMapping(value = "/getDataNames")
    public R<List<DatasourceConfig>> getCurrentDataSourceNameList() {
        List<DatasourceConfig> datasourceConfigList = datasourceConfigService.listAll();
        return R.ok(datasourceConfigList);
    }

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    @PostMapping("/compare")
    public R<GeneratorCompareDTO> compare(@Validated @RequestBody GeneratorCompareParam generatorCompareParam) {
        GeneratorCompareDTO generatorCompareDTO = genTableService.compareByVersion(generatorCompareParam.getCurrentVersion(), generatorCompareParam.getCompareVersion());
        return R.ok(generatorCompareDTO);
    }

}
