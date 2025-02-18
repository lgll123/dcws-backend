package com.formssi.generator.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.dynamic.datasource.annotation.DSTransactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.generator.domain.*;
import com.formssi.generator.domain.dto.GeneratorCompareDTO;
import com.formssi.generator.mapper.DmTableColumnMapper;
import com.formssi.generator.mapper.DmTableMapper;
import com.formssi.generator.param.GeneratorHistoryParam;
import com.formssi.generator.param.GeneratorParam;
import com.formssi.generator.service.*;
import com.formssi.generator.sql.*;
import com.formssi.generator.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.proxy.ServiceProxy;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import com.formssi.common.core.constant.Constants;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.core.utils.file.FileUtils;
import com.formssi.common.json.utils.JsonUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.generator.constant.GenConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.formssi.generator.util.VelocityUtils.prepareContext;

/**
 * 业务 服务层实现
 *
 * @author lizhangyu
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DmTableServiceImpl extends ServiceImpl<DmTableMapper, DmTable> implements IDmTableService {

    static ExecutorService executorService = Executors.newFixedThreadPool(2);

    private final DmTableMapper baseMapper;
    private final DmTableColumnMapper dmTableColumnMapper;
    private final IdentifierGenerator identifierGenerator;
    private final IDatasourceConfigService datasourceConfigService;
    private final ITypeConfigService typeConfigService;
    private final IGeneratorService generatorService;
    private final IGenerateHistoryService generateHistoryService;
    private final IDmSubTableService dmSubTableService;
    private final ITemplateConfigService templateConfigService;

    private static final String[] TABLE_IGNORE = new String[]{"sj_", "act_", "flw_", "gen_"};

    @Value("${gen.format-xml:false}")
    private String formatXml;

    /**
     * 项目空间路径
     */
    private static final String PROJECT_PATH = "main/java";

    /**
     * 查询业务字段列表
     *
     * @param tableId 业务字段编号
     * @return 业务字段集合
     */
    @Override
    public List<DmTableColumn> selectGenTableColumnListByTableId(Long tableId) {
        return dmTableColumnMapper.selectList(new LambdaQueryWrapper<DmTableColumn>()
                .eq(DmTableColumn::getTableId, tableId)
                .orderByAsc(DmTableColumn::getSort));
    }

    /**
     * 查询业务信息
     *
     * @param id 业务ID
     * @return 业务信息
     */
    @Override
    public DmTable selectGenTableById(Long id) {
        DmTable dmTable = baseMapper.selectGenTableById(id);
        setTableFromOptions(dmTable);
        return dmTable;
    }

    @Override
    public TableDataInfo<DmTable> selectPageGenTableList(DmTable dmTable, PageQuery pageQuery) {
        Page<DmTable> page = baseMapper.selectPage(pageQuery.build(), this.buildGenTableQueryWrapper(dmTable));
        return TableDataInfo.build(page);
    }

    private QueryWrapper<DmTable> buildGenTableQueryWrapper(DmTable dmTable) {
        Map<String, Object> params = dmTable.getParams();
        QueryWrapper<DmTable> wrapper = Wrappers.query();
        wrapper
                .eq(StringUtils.isNotEmpty(dmTable.getDataName()), "data_name", dmTable.getDataName())
                .like(StringUtils.isNotBlank(dmTable.getTableName()), "lower(table_name)", StringUtils.lowerCase(dmTable.getTableName()))
                .like(StringUtils.isNotBlank(dmTable.getTableComment()), "lower(table_comment)", StringUtils.lowerCase(dmTable.getTableComment()))
                .between(params.get("beginTime") != null && params.get("endTime") != null,
                        "create_time", params.get("beginTime"), params.get("endTime"));
        return wrapper;
    }

    /**
     * 查询数据库列表
     *
     * @param dmTable  包含查询条件的GenTable对象
     * @param pageQuery 包含分页信息的PageQuery对象
     * @return 包含分页结果的TableDataInfo对象
     */
    @DS("#dmTable.dataName")
    @Override
    public TableDataInfo<DmTable> selectPageDbTableList(DmTable dmTable, PageQuery pageQuery) {
        // 获取查询条件
        String tableName = dmTable.getTableName();
        String tableComment = dmTable.getTableComment();

        LinkedHashMap<String, Table<?>> tablesMap = ServiceProxy.metadata().tables();
        if (CollUtil.isEmpty(tablesMap)) {
            return TableDataInfo.build();
        }
        List<String> tableNames = baseMapper.selectTableNameList(dmTable.getDataName());
        String[] tableArrays;
        if (CollUtil.isNotEmpty(tableNames)) {
            tableArrays = tableNames.toArray(new String[0]);
        } else {
            tableArrays = new String[0];
        }
        // 过滤并转换表格数据
        List<DmTable> tables = tablesMap.values().stream()
                .filter(x -> !StringUtils.containsAnyIgnoreCase(x.getName(), TABLE_IGNORE))
                .filter(x -> {
                    if (CollUtil.isEmpty(tableNames)) {
                        return true;
                    }
                    return !StringUtils.equalsAnyIgnoreCase(x.getName(), tableArrays);
                })
                .filter(x -> {
                    boolean nameMatches = true;
                    boolean commentMatches = true;
                    // 进行表名称的模糊查询
                    if (StringUtils.isNotBlank(tableName)) {
                        nameMatches = StringUtils.containsIgnoreCase(x.getName(), tableName);
                    }
                    // 进行表描述的模糊查询
                    if (StringUtils.isNotBlank(tableComment)) {
                        commentMatches = StringUtils.containsIgnoreCase(x.getComment(), tableComment);
                    }
                    // 同时匹配名称和描述
                    return nameMatches && commentMatches;
                })
                .map(x -> {
                    DmTable gen = new DmTable();
                    gen.setTableName(x.getName());
                    gen.setTableComment(x.getComment());
                    gen.setCreateTime(x.getCreateTime());
                    gen.setUpdateTime(x.getUpdateTime());
                    return gen;
                }).toList();

        IPage<DmTable> page = pageQuery.build();
        page.setTotal(tables.size());
        // 手动分页 set数据
        page.setRecords(CollUtil.page((int) page.getCurrent() - 1, (int) page.getSize(), tables));
        return TableDataInfo.build(page);
    }

    /**
     * 查询据库列表
     *
     * @param tableNames 表名称组
     * @param dataName   数据源名称
     * @return 数据库表集合
     */
    @DS("#dataName")
    @Override
    public List<DmTable> selectDbTableListByNames(String[] tableNames, String dataName) {
        Set<String> tableNameSet = new HashSet<>(List.of(tableNames));
        LinkedHashMap<String, Table<?>> tablesMap = ServiceProxy.metadata().tables();

        if (CollUtil.isEmpty(tablesMap)) {
            return new ArrayList<>();
        }

        List<Table<?>> tableList = tablesMap.values().stream()
                .filter(x -> !StringUtils.containsAnyIgnoreCase(x.getName(), TABLE_IGNORE))
                .filter(x -> tableNameSet.contains(x.getName())).toList();

        if (CollUtil.isEmpty(tableList)) {
            return new ArrayList<>();
        }
        return tableList.stream().map(x -> {
            DmTable gen = new DmTable();
            gen.setDataName(dataName);
            gen.setTableName(x.getName());
            gen.setTableComment(x.getComment());
            gen.setCreateTime(x.getCreateTime());
            gen.setUpdateTime(x.getUpdateTime());
            return gen;
        }).toList();
    }

    /**
     * 查询所有表信息
     *
     * @return 表信息集合
     */
    @Override
    public List<DmTable> selectGenTableAll() {
        return baseMapper.selectGenTableAll();
    }

    /**
     * 修改业务
     *
     * @param dmTable 业务信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateGenTable(DmTable dmTable) {
        String options = JsonUtils.toJsonString(dmTable.getParams());
        dmTable.setOptions(options);
        int row = baseMapper.updateById(dmTable);
        if (row > 0) {
            // 表对应的列
            for (DmTableColumn cenTableColumn : dmTable.getColumns()) {
                dmTableColumnMapper.updateById(cenTableColumn);
            }

            if (GenConstants.TPL_SUB.equals(dmTable.getTplCategory())) {
                // 修改子表信息
                dmSubTableService.updateSubTableListByTableId(dmTable.getTableId(), dmTable.getSubTableList());
            }

        }
    }

    /**
     * 删除业务对象
     *
     * @param tableIds 需要删除的数据ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteGenTableByIds(Long[] tableIds) {
        List<Long> ids = Arrays.asList(tableIds);
        baseMapper.deleteByIds(ids);
        dmTableColumnMapper.delete(new LambdaQueryWrapper<DmTableColumn>().in(DmTableColumn::getTableId, ids));
    }

    /**
     * 导入表结构
     *
     * @param tableList 导入表列表
     * @param dataName  数据源名称
     */
    @DSTransactional
    @Override
    public void importGenTable(List<DmTable> tableList, String dataName) {
        Long operId = LoginHelper.getUserId();
        try {
            for (DmTable table : tableList) {
                String tableName = table.getTableName();
                GenUtils.initTable(table, operId);
                table.setDataName(dataName);
                int row = baseMapper.insert(table);
                if (row > 0) {
                    // 保存列信息
                    List<DmTableColumn> dmTableColumns = SpringUtils.getAopProxy(this).selectDbTableColumnsByName(tableName, dataName);
                    List<DmTableColumn> saveColumns = new ArrayList<>();
                    for (DmTableColumn column : dmTableColumns) {
                        GenUtils.initColumnField(column, table);
                        saveColumns.add(column);
                    }
                    if (CollUtil.isNotEmpty(saveColumns)) {
                        dmTableColumnMapper.insertBatch(saveColumns);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException("导入失败：" + e.getMessage());
        }
    }

    @Override
    public void importGenTableList(List<TableDefinition> tableDefinitionList, String dataName, Integer dataSourceId) {

        List<DmTable> tableList = buildGenTableList(tableDefinitionList, dataName);
        Long operId = LoginHelper.getUserId();
        try {
            for (DmTable table : tableList) {
                String tableName = table.getTableName();
                GenUtils.initTable(table, operId);
                table.setDataName(dataName);
                table.setDataSourceId(dataSourceId.longValue());
                int row = baseMapper.insert(table);
                if (row > 0) {
                    // 保存列信息
                    List<DmTableColumn> dmTableColumns = buildDbTableColumnsByName(tableName, tableDefinitionList);
                    List<DmTableColumn> saveColumns = new ArrayList<>();
                    for (DmTableColumn column : dmTableColumns) {
                        GenUtils.initColumnField(column, table);
                        saveColumns.add(column);
                    }
                    if (CollUtil.isNotEmpty(saveColumns)) {
                        dmTableColumnMapper.insertBatch(saveColumns);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException("导入失败：" + e.getMessage());
        }
    }

    /**
     * 保存列信息
     *
     * @param tableName           表名
     * @param tableDefinitionList 表对应的列列表
     * @return 返回表栏目列表
     */
    private List<DmTableColumn> buildDbTableColumnsByName(String tableName, List<TableDefinition> tableDefinitionList) {
        TableDefinition tableDefinition = tableDefinitionList.stream().filter(item -> tableName.equals(item.getTableName())).toList().get(0);
        List<ColumnDefinition> columnDefinitionList = tableDefinition.getColumnDefinitions();
        List<DmTableColumn> tableColumnList = new ArrayList<>();
        Integer positon = 0;
        for (int i = 0; i < columnDefinitionList.size(); i++) {
            ColumnDefinition item = columnDefinitionList.get(i);
            DmTableColumn tableColumn = new DmTableColumn();
            tableColumn.setIsPk(item.getIsPk() ? "1" : "0");
            tableColumn.setColumnName(item.getName());
            tableColumn.setColumnComment(item.getComment());
            tableColumn.setColumnType(item.getType().toLowerCase());
            positon = i + 1;
            tableColumn.setSort(positon);
            tableColumn.setIsRequired(item.getIsNullable() ? "1" : "0");
            tableColumn.setIsIncrement(!item.getIsIdentity() ? "1" : "0");
            tableColumnList.add(tableColumn);
        }

        return tableColumnList;
    }

    /**
     * 根据表名称查询列信息
     *
     * @param tableName 表名称
     * @param dataName  数据源名称
     * @return 列信息
     */
    @DS("#dataName")
    @Override
    public List<DmTableColumn> selectDbTableColumnsByName(String tableName, String dataName) {
        LinkedHashMap<String, Column> columns = ServiceProxy.metadata().columns(tableName);
        List<DmTableColumn> tableColumns = new ArrayList<>();
        columns.forEach((columnName, column) -> {
            DmTableColumn tableColumn = new DmTableColumn();
            tableColumn.setIsPk(String.valueOf(column.isPrimaryKey()));
            tableColumn.setColumnName(column.getName());
            tableColumn.setColumnComment(column.getComment());
            tableColumn.setColumnType(column.getTypeName().toLowerCase());
            tableColumn.setSort(column.getPosition());
            tableColumn.setIsRequired(column.isNullable() == 0 ? "1" : "0");
            tableColumn.setIsIncrement(column.isAutoIncrement() == -1 ? "0" : "1");
            tableColumns.add(tableColumn);
        });
        return tableColumns;
    }

    /**
     * 预览代码
     *
     * @param tableId 表编号
     * @return 预览数据列表
     */
    @Override
    public Map<String, String> previewCode(Long tableId) {
        Map<String, String> dataMap = new LinkedHashMap<>();
        // 查询表信息
        DmTable table = baseMapper.selectGenTableById(tableId);
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            menuIds.add(identifierGenerator.nextId(null).longValue());
        }
        table.setMenuIds(menuIds);
        // 设置主键列信息
        setPkColumn(table);
        VelocityInitializer.initVelocity();

        VelocityContext context = prepareContext(table);

        // 获取模板列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            dataMap.put(template, sw.toString());
        }
        return dataMap;
    }

    /**
     * 生成代码（下载方式）
     *
     * @param tableId 表名称
     * @return 数据
     */
    @Override
    public byte[] downloadCode(Long tableId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        generatorCode(tableId, zip);
        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

    /**
     * 生成代码（自定义路径）
     *
     * @param tableId 表名称
     */
    @Override
    public void generatorCode(Long tableId) {
        // 查询表信息
        DmTable table = baseMapper.selectGenTableById(tableId);
        // 设置主键列信息
        setPkColumn(table);

        VelocityInitializer.initVelocity();

        VelocityContext context = prepareContext(table);

        // 获取模板列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        for (String template : templates) {
            if (!StringUtils.containsAny(template, "sql.vm", "api.ts.vm", "types.ts.vm", "index.vue.vm", "index-tree.vue.vm")) {
                // 渲染模板
                StringWriter sw = new StringWriter();
                Template tpl = Velocity.getTemplate(template, Constants.UTF8);
                tpl.merge(context, sw);
                try {
                    String path = getGenPath(table, template);
                    FileUtils.writeUtf8String(sw.toString(), path);
                } catch (Exception e) {
                    throw new ServiceException("渲染模板失败，表名：" + table.getTableName());
                }
            }
        }
    }

    /**
     * 同步数据库
     *
     * @param tableId 表名称
     */
    @DSTransactional
    @Override
    public void synchDb(Long tableId) {
        DmTable table = baseMapper.selectGenTableById(tableId);
        List<DmTableColumn> tableColumns = table.getColumns();
        Map<String, DmTableColumn> tableColumnMap = StreamUtils.toIdentityMap(tableColumns, DmTableColumn::getColumnName);

        String tableName = table.getTableName();
        List<TableDefinition> tableDefinitionList = getListByDatasourceConfigIdAndTableNames(table.getDataSourceId().intValue(), List.of(table.getTableName()));
        if (CollectionUtils.isEmpty(tableDefinitionList) || CollectionUtils.isEmpty(tableDefinitionList.get(0).getColumnDefinitions())) {
            throw new ServiceException("同步数据失败，原表结构不存在");
        }

        // 保存列信息
        List<DmTableColumn> dbTableColumns = buildDbTableColumnsByName(tableName, tableDefinitionList);

        List<String> dbTableColumnNames = StreamUtils.toList(dbTableColumns, DmTableColumn::getColumnName);

        List<DmTableColumn> saveColumns = new ArrayList<>();
        dbTableColumns.forEach(column -> {
            GenUtils.initColumnField(column, table);
            if (tableColumnMap.containsKey(column.getColumnName())) {
                DmTableColumn prevColumn = tableColumnMap.get(column.getColumnName());
                column.setColumnId(prevColumn.getColumnId());
                if (column.isList()) {
                    // 如果是列表，继续保留查询方式/字典类型选项
                    column.setDictType(prevColumn.getDictType());
                    column.setQueryType(prevColumn.getQueryType());
                }
                if (StringUtils.isNotEmpty(prevColumn.getIsRequired()) && !column.isPk()
                        && (column.isInsert() || column.isEdit())
                        && ((column.isUsableColumn()) || (!column.isSuperColumn()))) {
                    // 如果是(新增/修改&非主键/非忽略及父属性)，继续保留必填/显示类型选项
                    column.setIsRequired(prevColumn.getIsRequired());
                    column.setHtmlType(prevColumn.getHtmlType());
                }
            }
            saveColumns.add(column);
        });
        if (CollUtil.isNotEmpty(saveColumns)) {
            dmTableColumnMapper.insertOrUpdateBatch(saveColumns);
        }
        List<DmTableColumn> delColumns = StreamUtils.filter(tableColumns, column -> !dbTableColumnNames.contains(column.getColumnName()));
        if (CollUtil.isNotEmpty(delColumns)) {
            List<Long> ids = StreamUtils.toList(delColumns, DmTableColumn::getColumnId);
            if (CollUtil.isNotEmpty(ids)) {
                dmTableColumnMapper.deleteByIds(ids);
            }
        }
    }

    /**
     * 批量生成代码（下载方式）
     *
     * @param tableIds 表ID数组
     * @return 数据
     */
    @Override
    public byte[] downloadCode(String[] tableIds) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String tableId : tableIds) {
            generatorCode(Long.parseLong(tableId), zip);
        }
        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

//    @Override
//    public byte[] batchCodeGenerate(List<Long> tableIdList, List<Integer> templateConfigIdList) {
//        List<DmTable> dmTableList = genTableListByTableIds(tableIdList);
//
//        // 转换为Map<String, List<DmTable>>
//        Map<Long, List<DmTable>> genTableMap = dmTableList.stream()
//                .collect(Collectors.groupingBy(DmTable::getDataSourceId));
//
//        List<CodeFile> generateCodeFileList = new ArrayList<>();
//
//        GeneratorHistoryParam generatorHistoryParam = new GeneratorHistoryParam();
//        List<Integer> datasourceConfigIdList = new ArrayList<>();
//        List<String> tableNameList = new ArrayList<>();
//
//        // genTableMap遍历
//        genTableMap.forEach((key, value) -> {
//            datasourceConfigIdList.add(key.intValue());
//            List<String> tableNames = value.stream().map(DmTable::getTableName).toList();
//            tableNameList.addAll(tableNames);
//            GeneratorParam generatorParam = buildGeneratorParam(key, value, templateConfigIdList);
//            DatasourceConfig datasourceConfig = datasourceConfigService.getById(key.intValue());
//            GeneratorConfig generatorConfig = GeneratorConfig.build(datasourceConfig);
//            // 获取生成对应的表内容
//            List<CodeFile> codeFileList = generatorService.generate(generatorParam, generatorConfig);
//            generateCodeFileList.addAll(codeFileList);
//        });
//
//        // 代码生成历史
//        generatorHistoryParam.setDatasourceConfigIds(datasourceConfigIdList);
//        generatorHistoryParam.setTableIdList(tableIdList);
//        generatorHistoryParam.setTableNames(tableNameList);
//        generatorHistoryParam.setTemplateConfigIdList(templateConfigIdList);
//        executorService.execute(() -> {
//            generateHistoryService.saveGenerateHistory(generatorHistoryParam);
//        });
//
//        return batchDownloadCode(generateCodeFileList);
//    }

    public byte[] batchCodeGenerate(List<Long> tableIdList, List<Integer> templateConfigIdList) {
        List<CodeFile> generateCodeFileList = new ArrayList<>();
        List<Integer> datasourceConfigIdList = new ArrayList<>();
        List<String> tableNameList = new ArrayList<>();
        for (Long tableId : tableIdList) {
            List<CodeFile> codeFileList = codeGenerate(tableId, templateConfigIdList, datasourceConfigIdList, tableNameList);
            generateCodeFileList.addAll(codeFileList);
        }

        // 代码生成历史
        GeneratorHistoryParam generatorHistoryParam = new GeneratorHistoryParam();
        generatorHistoryParam.setDatasourceConfigIds(datasourceConfigIdList);
        generatorHistoryParam.setTableIdList(tableIdList);
        generatorHistoryParam.setTableNames(tableNameList);
        generatorHistoryParam.setTemplateConfigIdList(templateConfigIdList);
        executorService.execute(() -> {
            generateHistoryService.saveGenerateHistory(generatorHistoryParam);
        });

        return batchDownloadCode(generateCodeFileList);
    }

    @Override
    public List<CodeFile> getCodeListByTableIdsAndTemplateConfigIds(List<Long> tableIdList, List<Integer> templateConfigIdList) {
        List<DmTable> dmTableList = genTableListByTableIds(tableIdList);

        // 转换为Map<String, List<DmTable>>
        Map<Long, List<DmTable>> genTableMap = dmTableList.stream()
                .collect(Collectors.groupingBy(DmTable::getDataSourceId));

        List<CodeFile> generateCodeFileList = new ArrayList<>();

        // genTableMap遍历
        genTableMap.forEach((key, value) -> {
            GeneratorParam generatorParam = buildGeneratorParam(key, value, templateConfigIdList);
            DatasourceConfig datasourceConfig = datasourceConfigService.getById(key.intValue());
            GeneratorConfig generatorConfig = GeneratorConfig.build(datasourceConfig);
            // 获取生成对应的表内容
            List<CodeFile> codeFileList = generatorService.generate(generatorParam, generatorConfig);
            generateCodeFileList.addAll(codeFileList);
        });

        return generateCodeFileList;
    }

    @Override
    public GeneratorCompareDTO compareByVersion(Integer currentVersion, Integer compareVersion) {
        List<Integer> versionList = Arrays.asList(currentVersion, compareVersion);
        // 获取生成历史记录
        List<GenerateHistory> generateHistoryList = generateHistoryService.getListByVersionList(versionList);

        // 校验版本是否存在
        GenerateHistory currentGenerateHistory = checkVersionExist(generateHistoryList, currentVersion);
        GenerateHistory compareGenerateHistory = checkVersionExist(generateHistoryList, compareVersion);

        // 获取代码map
        GeneratorCompareDTO compareDTO = new GeneratorCompareDTO();
        compareDTO.setCurrentVersionMap(GetCodeMap(currentGenerateHistory));
        compareDTO.setCompareVersionMap(GetCodeMap(compareGenerateHistory));
        return compareDTO;
    }

    /**
     * 生成代码
     * @param tableId 表id
     * @param templateConfigIdList 模版id
     * @param datasourceConfigIdList 数据源列表
     * @param tableNameList 表名
     * @return 返回
     */
    public List<CodeFile> codeGenerate(Long tableId, List<Integer> templateConfigIdList, List<Integer> datasourceConfigIdList, List<String> tableNameList) {
        // 查询表信息
        DmTable table = baseMapper.selectGenTableById(tableId);

        // 查询子表信息
        List<DmSubTable> subTableList = dmSubTableService.getDmSubTableListByTableId(tableId);

        // 设置子表栏目信息
        if (CollectionUtils.isNotEmpty(subTableList)) {
            table.setSubTableList(subTableList);
            for (DmSubTable dmSubTable : subTableList) {
                List<DmTableColumn> dmSubTableColumnList = selectGenTableColumnListByTableId(dmSubTable.getSubTableId());
                dmSubTable.setColumns(dmSubTableColumnList);
            }
        }

        // 添加数据源和表名到列表中
        datasourceConfigIdList.add(table.getDataSourceId().intValue());
        tableNameList.add(table.getTableName());

        // 设置主键列信息
        setPkColumn(table);

        VelocityInitializer.initVelocity();

        VelocityContext context = prepareContext(table);

        List<CodeFile> codeFileList = new ArrayList<>();

        for (int tcId : templateConfigIdList) {
            TemplateConfig template = templateConfigService.getById(tcId);
            String templeName = template.getFileName();

            // 如果是树表（增删改查）并且模版是index.vue，则跳过
            if (GenConstants.TPL_TREE.equals(table.getTplCategory()) && templeName.contains("index.vue")) {
                continue;
            }

            // 如果不是树表（增删改查）并且模版是index-tree.vue，则跳过
            if (!GenConstants.TPL_TREE.equals(table.getTplCategory()) && templeName.contains("index-tree.vue")) {
                continue;
            }

            // 如果不是主子表（增删改查）并且模版是sub-开头，则跳过
            if (!GenConstants.TPL_SUB.equals(table.getTplCategory()) && templeName.contains("sub-")) {
                continue;
            }

            String folder = template.getFolder();
            if (org.apache.commons.lang.StringUtils.isEmpty(folder)) {
                folder = template.getName();
            }else{
                // 文件目录可以使用变量
                folder = doGenerator(context, folder);
            }

            String fileName = "";
            // 如果是子表类型并且类名为sub-domain
            if (GenConstants.TPL_SUB.equals(table.getTplCategory()) && checkSubTempleName(templeName)) {
                if (CollectionUtils.isNotEmpty(subTableList)) {
                    for (DmSubTable dmSubTable : subTableList) {
                        // 文件名
                        fileName = getSubFileName(table.getPackageName(), template.getFileName(), GenUtils.convertClassName(dmSubTable.getSubTableName()));
                        // 子类赋值
                        VelocityUtils.setSubVelocityContext(context, dmSubTable);
                        buildCodeList(context, fileName, folder, template, codeFileList);
                    }
                }
            }else {
                fileName = VelocityUtils.getFileName(template.getFileName(), table);
                buildCodeList(context, fileName, folder, template, codeFileList);
            }

        }

        return codeFileList;

    }

    @Override
    public DmTable selectDbTableByNamesAndDatasourceConfigId(String tableName, Long datasourceConfigId) {
        return lambdaQuery().eq(DmTable::getTableName, tableName)
                .eq(DmTable::getDataSourceId, datasourceConfigId)
                .one();
    }

    @Override
    public Map<String, String> codePreview(GeneratorParam generatorParam, GeneratorConfig generatorConfig, int datasourceConfigId) {
        List<Integer> datasourceConfigIdList = new ArrayList<>();
        List<String> tableNameList = new ArrayList<>();
        DmTable dmTable = selectDbTableByNamesAndDatasourceConfigId(generatorParam.getTableNames().get(0), (long) datasourceConfigId);
        List<CodeFile> codeFileList = codeGenerate(dmTable.getTableId(), generatorParam.getTemplateConfigIdList(), datasourceConfigIdList, tableNameList);
        return codeFileList.stream().collect(Collectors.toMap(CodeFile::getFileName, CodeFile::getContent));
    }

    /**
     * 校验子类名字
     * @param templeName 模版名称
     * @return 返回boolean值
     */
    private boolean checkSubTempleName(String templeName) {
        return templeName.contains("sub-domain.java") || templeName.contains("sub-bo.java") || templeName.contains("sub-vo.java");
    }

    /**
     * 获取子表文件名称
     * @param packageName 包名
     * @param template 模板
     * @param className 类名
     * @return 返回文件名
     */
    private String getSubFileName(String packageName, String template, String className) {
        // 文件名称
        String fileName = "";

        String javaPath = PROJECT_PATH + "/" + StringUtils.replace(packageName, ".", "/");

        if (template.contains("sub-domain.java.vm") || template.contains("sub-domain.java")) {
            fileName = StringUtils.format("{}/domain/{}.java", javaPath, className);
        } else if (template.contains("sub-bo.java.vm") || template.contains("sub-bo.java")) {
            fileName = StringUtils.format("{}/domain/bo/{}Bo.java", javaPath, className);
        } else if (template.contains("sub-vo.vm") || template.contains("sub-vo.java")) {
            fileName = StringUtils.format("{}/domain/vo/{}Vo.java", javaPath, className);
        }else {
            fileName = template;
        }

        return fileName;
    }

    private List<CodeFile> buildCodeList(VelocityContext context, String fileName, String folder, TemplateConfig template, List<CodeFile> codeFileList) {
        // 格式化代码
        String content = this.formatCode(fileName, template.getContent());

        content = doGenerator(context, content);
        CodeFile codeFile = new CodeFile();
        codeFile.setFolder(folder);
        codeFile.setFileName(fileName);
        codeFile.setContent(content);
        codeFileList.add(codeFile);

        return codeFileList;
    }

    // 格式化代码
    private String formatCode(String fileName, String content) {
        if (Objects.equals("true", formatXml) && fileName.endsWith(".xml")) {
            return FormatUtil.formatXml(content);
        }
        return content;
    }

    private String doGenerator(VelocityContext context, String template) {
        if (template == null) {
            return "";
        }

        return VelocityUtil.generate(context, template);
    }

    /**
     * 校验版本数据是否存在
     * @param generateHistoryList 代码生成历史
     * @param version 版本
     * @return 返回生成历史记录
     */
    private GenerateHistory checkVersionExist(List<GenerateHistory> generateHistoryList, Integer version) {
        List<GenerateHistory> generateHistories = generateHistoryList.stream().filter(item -> version.equals(item.getVersion())).toList();
        if (org.springframework.util.CollectionUtils.isEmpty(generateHistories)) {
            throw new ServiceException("版本为：{}的代码生成历史记录不存在", version);
        }

        return generateHistories.get(0);
    }

    /**
     * 根据生成历史获取代码map
     * @param generateHistory 生成历史
     * @return 返回map
     */
    private Map<String, String> GetCodeMap(GenerateHistory generateHistory) {
        String content = generateHistory.getConfigContent();
        GeneratorHistoryParam param = JSON.parseObject(content, GeneratorHistoryParam.class);
        List<Long> tableIdList = param.getTableIdList();
        List<Integer> templateConfigIdList = param.getTemplateConfigIdList();
        List<CodeFile> codeFileList = getCodeListByTableIdsAndTemplateConfigIds(tableIdList, templateConfigIdList);
        return codeFileList.stream().collect(Collectors.toMap(CodeFile::getFileName, CodeFile::getContent));
    }

    /**
     * 批量下载代码
     *
     * @param codeFileList 文件列表
     * @return 返回文件字节
     */
    private byte[] batchDownloadCode(List<CodeFile> codeFileList) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (CodeFile codeFile : codeFileList) {
            try {
                // 添加到zip
                StringWriter sw = new StringWriter();
                sw.write(codeFile.getContent());
                zip.putNextEntry(new ZipEntry(codeFile.getFileName()));
                IoUtil.write(zip, StandardCharsets.UTF_8, false, sw.toString());
                IoUtil.close(sw);
                zip.flush();
                zip.closeEntry();
            } catch (IOException e) {
                log.error("渲染模板失败，文件名为：" + codeFile.getFileName(), e);
            }
        }

        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

    /**
     * 构建代码生成参数
     *
     * @param datasourceConfigId   数据源id
     * @param dmTableList         生成表列表
     * @param templateConfigIdList 模版列表
     * @return 返回生成参数
     */
    private GeneratorParam buildGeneratorParam(Long datasourceConfigId, List<DmTable> dmTableList, List<Integer> templateConfigIdList) {
        GeneratorParam generatorParam = new GeneratorParam();
        generatorParam.setDatasourceConfigId(datasourceConfigId.intValue());
        List<String> tableNames = dmTableList.stream().map(DmTable::getTableName).toList();
        generatorParam.setTableNames(tableNames);
        generatorParam.setTemplateConfigIdList(templateConfigIdList);
        return generatorParam;
    }


    /**
     * 根据tableIds获取表列表
     *
     * @param tableIds 表id列表
     * @return 返回表列表
     */
    private List<DmTable> genTableListByTableIds(List<Long> tableIds) {
        if (CollectionUtils.isEmpty(tableIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(DmTable::getTableId, tableIds).list();
    }


    /**
     * 查询表信息并生成代码
     */
    private void generatorCode(Long tableId, ZipOutputStream zip) {
        // 查询表信息
        DmTable table = baseMapper.selectGenTableById(tableId);
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            menuIds.add(identifierGenerator.nextId(null).longValue());
        }
        table.setMenuIds(menuIds);
        // 设置主键列信息
        setPkColumn(table);

        VelocityInitializer.initVelocity();

        VelocityContext context = prepareContext(table);

        // 获取模板列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            try {
                // 添加到zip
                zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
                IoUtil.write(zip, StandardCharsets.UTF_8, false, sw.toString());
                IoUtil.close(sw);
                zip.flush();
                zip.closeEntry();
            } catch (IOException e) {
                log.error("渲染模板失败，表名：" + table.getTableName(), e);
            }
        }
    }

    /**
     * 修改保存参数校验
     *
     * @param dmTable 业务信息
     */
    @Override
    public void validateEdit(DmTable dmTable) {
        if (GenConstants.TPL_TREE.equals(dmTable.getTplCategory())) {
            String options = JsonUtils.toJsonString(dmTable.getParams());
            Dict paramsObj = JsonUtils.parseMap(options);
            if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_CODE))) {
                throw new ServiceException("树编码字段不能为空");
            } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_PARENT_CODE))) {
                throw new ServiceException("树父编码字段不能为空");
            } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_NAME))) {
                throw new ServiceException("树名称字段不能为空");
            }
        }
//        else if (GenConstants.TPL_SUB.equals(dmTable.getTplCategory())) {
//            if (StringUtils.isEmpty(dmTable.getSubTableName()))
//            {
//                throw new ServiceException("关联子表的表名不能为空");
//            }
//            else if (StringUtils.isEmpty(dmTable.getSubTableFkName()))
//            {
//                throw new ServiceException("子表关联的外键名不能为空");
//            }
//        }
    }

    @Override
    public List<TableDefinition> getListByDatasourceConfigIdAndTableNames(Integer datasourceConfigId, List<String> tableNames) {
        DatasourceConfig dataSourceConfig = datasourceConfigService.getById(datasourceConfigId);
        GeneratorConfig generatorConfig = GeneratorConfig.build(dataSourceConfig);
        SQLService service = SQLServiceFactory.build(generatorConfig);
        TableSelector tableSelector = service.getTableSelector(generatorConfig);
        tableSelector.setSchTableNames(tableNames);
        tableSelector.setColumnTypeConverter(typeConfigService.buildColumnTypeConverter());

        return tableSelector.getTableDefinitions();
    }

    private List<DmTable> buildGenTableList(List<TableDefinition> tableDefinitionList, String dbName) {
        List<DmTable> dmTableList = new ArrayList<>();
        tableDefinitionList.forEach(item -> {
            DmTable dmTable = new DmTable();
            dmTable.setDataName(dbName);
            dmTable.setTableName(item.getTableName());
            dmTable.setTableComment(item.getComment());
            dmTable.setCreateTime(new Date());
            dmTable.setUpdateTime(new Date());
            dmTableList.add(dmTable);
        });
        return dmTableList;
    }

    /**
     * 设置主键列信息
     *
     * @param table 业务表信息
     */
    public void setPkColumn(DmTable table) {
        for (DmTableColumn column : table.getColumns()) {
            if (column.isPk()) {
                table.setPkColumn(column);
                break;
            }
        }
        if (ObjectUtil.isNull(table.getPkColumn())) {
            table.setPkColumn(table.getColumns().get(0));
        }

    }

    /**
     * 设置代码生成其他选项值
     *
     * @param dmTable 设置后的生成对象
     */
    public void setTableFromOptions(DmTable dmTable) {
        Dict paramsObj = JsonUtils.parseMap(dmTable.getOptions());
        if (ObjectUtil.isNotNull(paramsObj)) {
            String treeCode = paramsObj.getStr(GenConstants.TREE_CODE);
            String treeParentCode = paramsObj.getStr(GenConstants.TREE_PARENT_CODE);
            String treeName = paramsObj.getStr(GenConstants.TREE_NAME);
            String parentMenuId = paramsObj.getStr(GenConstants.PARENT_MENU_ID);
            String parentMenuName = paramsObj.getStr(GenConstants.PARENT_MENU_NAME);

            dmTable.setTreeCode(treeCode);
            dmTable.setTreeParentCode(treeParentCode);
            dmTable.setTreeName(treeName);
            dmTable.setParentMenuId(parentMenuId);
            dmTable.setParentMenuName(parentMenuName);
        }
    }

    /**
     * 获取代码生成地址
     *
     * @param table    业务表信息
     * @param template 模板文件路径
     * @return 生成地址
     */
    public static String getGenPath(DmTable table, String template) {
        String genPath = table.getGenPath();
        if (StringUtils.equals(genPath, "/")) {
            return System.getProperty("user.dir") + File.separator + "src" + File.separator + VelocityUtils.getFileName(template, table);
        }
        return genPath + File.separator + VelocityUtils.getFileName(template, table);
    }
}

