package com.formssi.generator.service.impl;

import com.formssi.common.core.utils.DateUtils;
import com.formssi.generator.domain.DmTable;
import com.formssi.generator.domain.TemplateConfig;
import com.formssi.generator.param.GeneratorParam;
import com.formssi.generator.service.*;
import com.formssi.generator.sql.*;
import com.formssi.generator.util.FormatUtil;
import com.formssi.generator.util.VelocityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 生成代码逻辑
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GeneratorServiceImpl implements IGeneratorService {

    static ExecutorService executorService = Executors.newFixedThreadPool(2);

    private final ITemplateConfigService templateConfigService;

    private final IGenerateHistoryService generateHistoryService;

    private final ITypeConfigService typeConfigService;

    @Value("${gen.format-xml:false}")
    private String formatXml;

    /**
     * 生成代码内容,map的
     *
     * @param generatorParam 生成参数
     * @param generatorConfig 数据源配置
     * @return 一张表对应多个模板
     */
    @Override
    public List<CodeFile> generate(GeneratorParam generatorParam, GeneratorConfig generatorConfig) {
        List<SQLContext> contextList = this.buildSQLContextList(generatorParam, generatorConfig);
        List<CodeFile> codeFileList = new ArrayList<>();

        for (SQLContext sqlContext : contextList) {
            setPackageName(sqlContext, generatorParam.getPackageName());
            setDelPrefix(sqlContext, generatorParam.getDelPrefix());
            setAuthor(sqlContext, generatorParam.getAuthor());
            for (int tcId : generatorParam.getTemplateConfigIdList()) {
                TemplateConfig template = templateConfigService.getById(tcId);
                String folder = template.getFolder();
                if (StringUtils.isEmpty(folder)) {
                    folder = template.getName();
                }else{
                    // 文件目录可以使用变量
                    folder = doGenerator(sqlContext, folder);
                }

                setFolder(sqlContext, folder);

                //获取文件名
                String fileName = doGenerator(sqlContext, template.getFileName());
                String content = doGenerator(sqlContext, template.getContent());
                content = this.formatCode(fileName, content);
                CodeFile codeFile = new CodeFile();
                codeFile.setFolder(folder);
                codeFile.setFileName(fileName);
                codeFile.setContent(content);
                codeFileList.add(codeFile);
            }
        }

        return codeFileList;
    }

    @Override
    public Map<String, String> codePreview(GeneratorParam generatorParam, GeneratorConfig generatorConfig, int datasourceConfigId) {
        List<CodeFile> codeFileList = generate(generatorParam, generatorConfig);
        return codeFileList.stream().collect(Collectors.toMap(CodeFile::getFileName, CodeFile::getContent));
    }

    // 格式化代码
    private String formatCode(String fileName, String content) {
        if (Objects.equals("true", formatXml) && fileName.endsWith(".xml")) {
            return FormatUtil.formatXml(content);
        }
        return content;
    }

    /**
     * 返回SQL上下文列表
     *
     * @param generatorParam 参数
     * @param generatorConfig 配置
     * @return 返回SQL上下文
     */
    private List<SQLContext> buildSQLContextList(GeneratorParam generatorParam, GeneratorConfig generatorConfig) {

        List<String> tableNames = generatorParam.getTableNames();
        List<SQLContext> contextList = new ArrayList<>();
        SQLService service = SQLServiceFactory.build(generatorConfig);

        TableSelector tableSelector = service.getTableSelector(generatorConfig);
        tableSelector.setSchTableNames(tableNames);
        tableSelector.setColumnTypeConverter(typeConfigService.buildColumnTypeConverter());

        List<TableDefinition> tableDefinitions = tableSelector.getTableDefinitions();

        for (TableDefinition tableDefinition : tableDefinitions) {
            SQLContext sqlContext = new SQLContext(tableDefinition);
            sqlContext.setDbName(generatorConfig.getDbName());
            contextList.add(sqlContext);
        }

        return contextList;
    }

    private void setPackageName(SQLContext sqlContext, String packageName) {
        if (StringUtils.isNotBlank(packageName)) {
            sqlContext.setPackageName(packageName);
        }
    }

    private void setFolder(SQLContext sqlContext, String folder) {
        if (StringUtils.isNotBlank(folder)) {
            sqlContext.setPackageSubPath(folder);
        }
    }

    private void setDelPrefix(SQLContext sqlContext, String delPrefix) {
        if (StringUtils.isNotBlank(delPrefix)) {
            sqlContext.setDelPrefix(delPrefix);
        }
    }

    private void setAuthor(SQLContext sqlContext, String author) {
        if (StringUtils.isNotBlank(author)) {
            sqlContext.setAuthor(author);
        }
    }

    private String doGenerator(SQLContext sqlContext, String template) {
        if (template == null) {
            return "";
        }
        VelocityContext context = new VelocityContext();
        Object pkColumn = sqlContext.getTableDefinition().getPkColumn();
        if (pkColumn == null) {
            pkColumn = Collections.emptyMap();
        }

//        String packageName = dmTable.getPackageName();

        context.put("context", sqlContext);
        context.put("table", sqlContext.getTableDefinition());
        context.put("pk", pkColumn);
        context.put("columns", sqlContext.getTableDefinition().getColumnDefinitions());
        context.put("csharpColumns", sqlContext.getTableDefinition().getCsharpColumnDefinitions());

//        context.put("tplCategory", dmTable.getTplCategory());
//        context.put("tableName", dmTable.getTableName());
//        context.put("functionName", com.formssi.common.core.utils.StringUtils.isNotEmpty(functionName) ? functionName : "【请填写功能名称】");
//        context.put("ClassName", dmTable.getClassName());
//        context.put("className", com.formssi.common.core.utils.StringUtils.uncapitalize(dmTable.getClassName()));
//        context.put("moduleName", dmTable.getModuleName());
//        context.put("BusinessName", com.formssi.common.core.utils.StringUtils.capitalize(dmTable.getBusinessName()));
//        context.put("businessName", dmTable.getBusinessName());
//        context.put("basePackage", getPackagePrefix(packageName));
//        context.put("packageName", packageName);
//        context.put("author", dmTable.getFunctionAuthor());
//        context.put("datetime", DateUtils.getDate());
//        context.put("pkColumn", dmTable.getPkColumn());
//        context.put("importList", getImportList(dmTable));
//        context.put("permissionPrefix", getPermissionPrefix(moduleName, businessName));
        context.put("columns", sqlContext.getTableDefinition().getColumnDefinitions());
//        context.put("table", dmTable);
//        context.put("dicts", getDicts(dmTable));

        return VelocityUtil.generate(context, template);
    }

    /**
     * 获取包前缀
     *
     * @param packageName 包名称
     * @return 包前缀名称
     */
    public static String getPackagePrefix(String packageName) {
        int lastIndex = packageName.lastIndexOf(".");
        return com.formssi.common.core.utils.StringUtils.substring(packageName, 0, lastIndex);
    }

}
