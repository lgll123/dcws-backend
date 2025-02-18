package com.formssi.chorflow.db;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.formssi.system.domain.SysDocInfo;
import com.formssi.system.domain.SysDocParam;
import com.formssi.system.domain.SysModule;
import com.formssi.system.domain.SysProject;
import java.util.Map.Entry;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * 初始化数据库 测试
 *
 * @author joey
 * @date 2025.1.7
 */
@Slf4j
public class InsertDbTest extends DbBaseTest {


  @Test
  @Order(1)
  void insertSysProject() {
    // INSERT INTO low_code_engine.project( id, name, description, space_id, is_private, creator_id, creator_name, modifier_id, modifier_name, order_index, is_deleted, gmt_create, gmt_modified) VALUES( 1001, '流程编排', '流程编排', 20, 1, 16, '超级管理员', 16, '超级管理员', 0, 0, '2024-11-03 20:25:20', '2024-11-03 20:25:20');
    SysProject sysProject = new SysProject();
    sysProject.setId(PROJECT_ID);
    sysProject.setName("流程编排");
    sysProject.setDescription("流程编排");
    sysProject.setSpaceId(20L);
    sysProject.setIsPrivate((byte) 1);

    boolean save = iProjectService.saveOrUpdate(sysProject);
    Assertions.assertTrue(save);
  }


  @Test
  @Order(2)
  void insertSysModule() {
    // INSERT INTO low_code_engine.module(id, name, project_id, `type`, import_url, basic_auth_username, basic_auth_password, token, create_mode, modify_mode, creator_id, modifier_id, order_index, is_deleted, gmt_create, gmt_modified) VALUES(1001, '用户流程编排模块', 1001, 0, '', '', '', '1001', 0, 0, 0, 0, 1, 0, '2024-11-27 10:39:27', '2024-11-27 10:41:20');
    SysModule sysModule = new SysModule();
    sysModule.setName("用户流程编排模块");
    sysModule.setProjectId(PROJECT_ID);
    sysModule.setType((byte) 0);

    boolean save = moduleService.saveOrUpdate(sysModule);
    Assertions.assertTrue(save);
    moduleId = sysModule.getId();
    log.info("moduleId:{}", moduleId);

  }

  @Test
  @Order(3)
  void insertSysDocInfo() {

    String readUtf8String = FileUtil.readUtf8String("json/InsertDb.json");
    JSONObject jsonObject = JSONUtil.parseObj(readUtf8String);
    JSONObject paths = jsonObject.getJSONObject("paths");
    String prefix = "http://10.101.68.4/prod-api";
    paths.forEach((url, value) -> {
      JSONObject methodJsonObject = (JSONObject) value;
      Optional<String> first = methodJsonObject.keySet().stream().findFirst();
      if (first.isPresent()) {
        String method = first.get();
        JSONObject methodValue = methodJsonObject.getJSONObject(method);

        SysDocInfo sysDocInfo = new SysDocInfo();
        sysDocInfo.setDataId(UUID.randomUUID().toString());
        sysDocInfo.setDocKey(UUID.randomUUID().toString());
        sysDocInfo.setMd5(String.valueOf(moduleId));
        sysDocInfo.setName(methodValue.getStr("summary"));
        sysDocInfo.setDescription(methodValue.getStr("description"));
        sysDocInfo.setAuthor("generator");
        sysDocInfo.setType((byte) 0);
        sysDocInfo.setUrl(prefix + url);
        sysDocInfo.setHttpMethod(method);
        if (methodValue.containsValue("requestBody")) {
          sysDocInfo.setContentType("application/json");
        }
        sysDocInfo.setDeprecated("");
        sysDocInfo.setIsFolder((byte) 0);
        sysDocInfo.setParentId(0L);
        sysDocInfo.setModuleId(moduleId);

        boolean save = docInfoService.save(sysDocInfo);
        Assertions.assertTrue(save);
        Long docInfoId = sysDocInfo.getId();

        // save parameters and headers
        JSONArray parameters = methodValue.getJSONArray("parameters");
        if (CollectionUtil.isNotEmpty(parameters)) {
          for (Object parameter : parameters) {

            JSONObject parameterJSONObject = (JSONObject) parameter;
            String in = parameterJSONObject.getStr("in");
            if (StrUtil.equalsAny(in, "query", "header")) {
              // 处理query参数
              SysDocParam sysDocParam = new SysDocParam();
              sysDocParam.setDataId(UUID.randomUUID().toString());
              sysDocParam.setName(parameterJSONObject.getStr("name"));
              sysDocParam.setType(
                  JSONUtil.getByPath(parameterJSONObject, "schema.type").toString());
              sysDocParam.setRequired((byte) 1);
              sysDocParam.setMaxLength("64");
              sysDocParam.setExample("tom");
              sysDocParam.setDescription(parameterJSONObject.getStr("description"));
              sysDocParam.setEnumId(0L);
              sysDocParam.setDocId(docInfoId);
              sysDocParam.setParentId(0L);

              // 0：path, 1：header， 2：body参数，3：返回参数，4：错误码, 5：query参数
              if (in.equals("query")) {
                sysDocParam.setStyle((byte) 5);
              } else if (in.equals("header")) {
                sysDocParam.setStyle((byte) 1);
              }

              save = docParamService.save(sysDocParam);
              Assertions.assertTrue(save);
            }

          }
        }

        // save requestBody
        JSONObject properties = (JSONObject) JSONUtil.getByPath(methodValue,
            "requestBody.content.application/json.schema.properties");
        if (null != properties) {
          for (Entry<String, Object> property : properties) {

            SysDocParam sysDocParam = new SysDocParam();
            sysDocParam.setDataId(UUID.randomUUID().toString());
            String paramName = property.getKey();
            JSONObject nameValue = properties.getJSONObject(paramName);
            sysDocParam.setName(paramName);
            sysDocParam.setType(nameValue.getStr("type"));
            sysDocParam.setRequired((byte) 1);
            sysDocParam.setMaxLength("64");
            sysDocParam.setExample("tom");
            sysDocParam.setDescription(nameValue.getStr("description"));
            sysDocParam.setEnumId(0L);
            sysDocParam.setDocId(docInfoId);
            sysDocParam.setParentId(0L);
            sysDocParam.setStyle((byte) 2);

            save = docParamService.save(sysDocParam);
            Assertions.assertTrue(save);

          }
        }

        // save response
        JSONObject responsesProperties = (JSONObject) JSONUtil.getByPath(methodValue,
            "responses.200.content.application/json.schema.properties");
        if (null != responsesProperties) {
          for (Entry<String, Object> property : responsesProperties) {

            SysDocParam sysDocParam = new SysDocParam();
            sysDocParam.setDataId(UUID.randomUUID().toString());
            String name = property.getKey();
            JSONObject nameValue = (JSONObject) property.getValue();
            sysDocParam.setName(name);
            String type = nameValue.getStr("type");
            sysDocParam.setType(null != type ? type : "string");
            sysDocParam.setDescription(
                null != nameValue.getStr("title") ? nameValue.getStr("title") : name);
            sysDocParam.setRequired((byte) 1);
            sysDocParam.setMaxLength("64");
            sysDocParam.setExample("tom");
            sysDocParam.setEnumId(0L);
            sysDocParam.setDocId(docInfoId);
            sysDocParam.setParentId(0L);
            // 0：path, 1：header， 2：请求参数，3：返回参数，4：错误码, 数据库字段：style
            sysDocParam.setStyle((byte) 3);

            save = docParamService.save(sysDocParam);
            Assertions.assertTrue(save);

            Long sysDocParamId = sysDocParam.getId();
            saveSub(type, nameValue, sysDocParamId, docInfoId);
          }
        }
      }

    });

  }

  /**
   * 保存子节点数据
   */
  private void saveSub(String type, JSONObject nameValue, Long sysDocParamId, Long docInfoId) {
    if ("object".equals(type)) {
      // 如果是嵌套结构
      JSONObject subProperties = nameValue.getJSONObject("properties");
      for (Entry<String, Object> subProperty : subProperties) {

        SysDocParam sysDocParam = new SysDocParam();
        sysDocParam.setDataId(UUID.randomUUID().toString());
        String name = subProperty.getKey();
        JSONObject subNameValue = (JSONObject) subProperty.getValue();
        sysDocParam.setName(name);
        String subType = subNameValue.getStr("type");
        sysDocParam.setType(null != subType ? subType : "string");
        sysDocParam.setDescription(
            null != subNameValue.getStr("title") ? subNameValue.getStr("title") : name);
        sysDocParam.setRequired((byte) 1);
        sysDocParam.setMaxLength("64");
        sysDocParam.setExample("tom");
        sysDocParam.setEnumId(0L);
        sysDocParam.setDocId(docInfoId);
        sysDocParam.setParentId(sysDocParamId);
        // 0：path, 1：header， 2：请求参数，3：返回参数，4：错误码, 数据库字段：style
        sysDocParam.setStyle((byte) 3);

        boolean save = docParamService.save(sysDocParam);
        Assertions.assertTrue(save);

        saveSub(subType, subNameValue, sysDocParam.getId(), docInfoId);
      }
    }
  }


}
