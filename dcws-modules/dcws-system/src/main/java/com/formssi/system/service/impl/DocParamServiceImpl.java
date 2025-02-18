package com.formssi.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.common.core.domain.model.LoginUser;
import com.formssi.system.domain.SysDocInfo;
import com.formssi.system.domain.SysDocParam;
import com.formssi.system.domain.SysEnumInfo;
import com.formssi.system.domain.dto.DocParamDTO;
import com.formssi.system.domain.dto.EnumInfoDTO;
import com.formssi.system.enums.BooleanEnum;
import com.formssi.system.enums.OperationModeEnum;
import com.formssi.system.enums.OperationTypeEnum;
import com.formssi.system.enums.ParamStyleEnum;
import com.formssi.system.mapper.SysDocParamMapper;
import com.formssi.system.service.IDocParamService;
import com.formssi.system.service.IEnumInfoService;
import com.formssi.system.util.DataIdUtil;
import com.formssi.system.util.IdGenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lizhangyu
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DocParamServiceImpl extends ServiceImpl<SysDocParamMapper, SysDocParam> implements IDocParamService {

    private static final List<String> COLLECT_TYPE_LIST = Arrays.asList(
            "array", "list", "set", "collection"
    );
    private static final List<String> BOOLEAN_TYPES = Arrays.asList(
            "boolean", "bool"
    );
    private static final List<String> NUMBER_TYPES = Arrays.asList(
            "byte", "short", "int", "long", "integer",
            "int8", "int16", "int32", "int64", "float", "double", "number"
    );

    private final SysDocParamMapper docParamMapper;
    private final IEnumInfoService enumService;

    /**
     * 根据文档id获取文档参数
     * @param dataId
     * @return
     */
    public SysDocParam getByDataId(String dataId) {
        return lambdaQuery().eq(SysDocParam::getDataId, dataId)
                .eq(SysDocParam::getIsDeleted, BooleanEnum.FALSE.getType())
                .one();
    }

    @Override
    public void saveParams(SysDocInfo sysDocInfo, List<DocParamDTO> docParamDTOS, ParamStyleEnum paramStyleEnum, LoginUser loginUser) {
        // 如果参数是空的，则移除这个类型的所有参数
        if (CollectionUtils.isEmpty(docParamDTOS)) {
            return;
        }
        for (DocParamDTO docParamDTO : docParamDTOS) {
            this.doSave(docParamDTO, 0L, sysDocInfo, paramStyleEnum, loginUser);
        }
    }

    @Override
    public List<SysDocParam> getListByDocId(Long docId) {
        return lambdaQuery().eq(SysDocParam::getDocId, docId)
                .eq(SysDocParam::getIsDeleted, BooleanEnum.FALSE.getType())
                .orderByAsc(SysDocParam::getOrderIndex)
                .list();
    }

    /**
     * 根据文档id获取文档参数
     * @param docId 文档id
     * @param paramStyleEnum 文档参数风格
     * @return 返回文档参数列表
     */
    private List<SysDocParam> listParentParam(long docId, ParamStyleEnum paramStyleEnum) {
        return lambdaQuery().eq(SysDocParam::getDocId, docId)
                .eq(SysDocParam::getStyle, paramStyleEnum.getStyle())
                .eq(SysDocParam::getParentId, 0)
                .list();
    }

    /**
     * 删除参数，同时会删除子节点
     *
     * @param id id
     */
    public void deleteParamDeeply(long id) {
        baseMapper.deleteById(id);
        this.deleteChildrenDeeply(id);
    }

    /**
     * 递归删除下面所有子节点
     *
     * @param parentId 父id
     */
    private void deleteChildrenDeeply(long parentId) {
        List<SysDocParam> children = getListByParentId(parentId);
        for (SysDocParam child : children) {
            this.deleteParamDeeply(child.getId());
        }
    }

    private List<SysDocParam> getListByParentId(Long parentId) {
        return lambdaQuery()
                .eq(SysDocParam::getParentId, parentId)
                .list();
    }

    /**
     * 参数保存
     * @param docParamDTO 文档参数DTO
     * @param parentId 父级ID
     * @param sysDocInfo 文档信息
     * @param paramStyleEnum 参数风格
     * @param loginUser 登录用户信息
     */
    private void doSave(DocParamDTO docParamDTO, long parentId, SysDocInfo sysDocInfo, ParamStyleEnum paramStyleEnum, LoginUser loginUser) {
        SysDocParam sysDocParam = new SysDocParam();
        Long docId = sysDocInfo.getId();
        String dataId = DataIdUtil.getDocParamDataId(docId, parentId, paramStyleEnum.getStyle(), docParamDTO.getName());
        // 如果删除
        if (BooleanEnum.isTrue(docParamDTO.getIsDeleted())) {
            dataId = IdGenUtil.nextId();
        }
        docParamDTO.setParentId(parentId);
        sysDocParam.setId(docParamDTO.getId());
        sysDocParam.setDataId(dataId);
        sysDocParam.setName(docParamDTO.getName());
        sysDocParam.setType(docParamDTO.getType());
        sysDocParam.setRequired(docParamDTO.getRequired());
        sysDocParam.setMaxLength(buildMaxLength(docParamDTO));
        sysDocParam.setExample(docParamDTO.getExample());
        sysDocParam.setDescription(docParamDTO.getDescription());
        sysDocParam.setEnumId(buildEnumId(sysDocInfo.getModuleId(), docParamDTO));
        sysDocParam.setDocId(docId);
        sysDocParam.setParentId(parentId);
        sysDocParam.setStyle(paramStyleEnum.getStyle());
        sysDocParam.setCreatorId(loginUser.getUserId());
        sysDocParam.setCreateMode(OperationTypeEnum.MANUAL_OPERATION.getType());
        sysDocParam.setCreatorName(loginUser.getNickname());
        sysDocParam.setModifierId(loginUser.getUserId());
        sysDocParam.setModifyMode(OperationTypeEnum.MANUAL_OPERATION.getType());
        sysDocParam.setModifierName(loginUser.getNickname());
        sysDocParam.setOrderIndex(docParamDTO.getOrderIndex());
        sysDocParam.setIsDeleted(docParamDTO.getIsDeleted());
        if (sysDocParam.getDescription() == null) {
            sysDocParam.setDescription("");
        }
        SysDocParam savedParam;
        if (sysDocParam.getId() == null) {
            savedParam = this.saveParam(sysDocParam);
        } else {
            baseMapper.updateById(sysDocParam);
            savedParam = sysDocParam;
        }
        // 回填ID
        docParamDTO.setId(savedParam.getId());
        List<DocParamDTO> children = docParamDTO.getChildren();
        if (children != null) {
            Long pid = savedParam.getId();
            // 修复NPE问题
            if (pid == null) {
                SysDocParam exist = getByDataId(savedParam.getDataId());
                if (exist != null) {
                    pid = exist.getId();
                }
            }
            for (DocParamDTO child : children) {
                // 如果父节点被删除，子节点也要删除
                if (sysDocParam.getIsDeleted() == BooleanEnum.TRUE.getType()) {
                    child.setIsDeleted(sysDocParam.getIsDeleted());
                }
                if (pid == null) {
                    continue;
                }
                this.doSave(child, pid, sysDocInfo, paramStyleEnum, loginUser);
            }
        }
    }

    private static String buildMaxLength(DocParamDTO docParamDTO) {
        String maxLength = docParamDTO.getMaxLength();
        if (StringUtils.isEmpty(maxLength) || "0".equals(maxLength)) {
            maxLength = "-";
        }
        return CollectionUtils.isEmpty(docParamDTO.getChildren()) ? maxLength : "";
    }

    /**
     * 构建枚举id
     * @param moduleId 模块id
     * @param docParamDTO 文档参数DTO
     * @return 返回枚举id
     */
    private Long buildEnumId(long moduleId, DocParamDTO docParamDTO) {
        EnumInfoDTO enumInfoDTO = docParamDTO.getEnumInfo();
        if (enumInfoDTO != null) {
            // 如果枚举名称为空则使用字段名称
            if (StringUtils.isEmpty(enumInfoDTO.getName())) {
                String name = docParamDTO.getName();
                enumInfoDTO.setName(name);
            }
            enumInfoDTO.setModuleId(moduleId);
            SysEnumInfo sysEnumInfo = enumService.saveEnumInfo(enumInfoDTO);
            return sysEnumInfo.getId();
        }
        Long enumId = docParamDTO.getEnumId();
        if (enumId == null) {
            enumId = 0L;
        }
        return enumId;
    }

    /**
     * 保存文档参数
     * @param sysDocParam 文档参数
     * @return 返回文档参数
     */
    public SysDocParam saveParam(SysDocParam sysDocParam) {
        if (sysDocParam.getDescription() == null) {
            sysDocParam.setDescription("");
        }
        if (sysDocParam.getExample() == null) {
            sysDocParam.setExample("");
        }
        docParamMapper.saveParam(sysDocParam);
        return sysDocParam;
    }

    /**
     * 根据文档id列表删除参数
     * @param docIdList 文档id列表
     */
    public void deletePushParam(List<Long> docIdList) {
        // 删除文档对应的参数
        lambdaUpdate().in(SysDocParam::getDocId, docIdList)
                .eq(SysDocParam::getCreateMode, OperationModeEnum.OPEN.getType())
                .set(SysDocParam::getIsDeleted, BooleanEnum.TRUE.getType())
                .update();
    }

    /**
     * 创建示例
     * @param docParams 文档参数
     * @return 返回JSON对象
     */
    public static JSONObject createExample(List<DocParamDTO> docParams) {
        return doCreateExample(docParams);
    }

    private static JSONObject doCreateExample(List<DocParamDTO> params) {
        JSONObject responseJson = new JSONObject();
        for (DocParamDTO row : params) {
            if (Objects.equals(row.getIsDeleted(), BooleanEnum.TRUE.getType())) {
                continue;
            }
            Object val;
            List<DocParamDTO> children = row.getChildren();
            if (!ObjectUtils.isEmpty(children)) {
                JSONObject childrenValue = doCreateExample(children);
                if (isArrayType(row.getType())) {
                    val = isNestArrayType(row.getType()) ? buildNestList(childrenValue)
                            : Collections.singletonList(childrenValue);
                } else {
                    val = childrenValue;
                }
            } else {
                // 单值
                String example = row.getExample();
                String type = row.getType();
                Object exampleObj = "";
                if (StringUtils.isEmpty(example)) {
                    exampleObj = getDefaultExample(type);
                } else {
                    // 解析出数字，布尔，数组示例值
                    if (isNumberType(type)) {
                        exampleObj = NumberUtils.toInt(example, 0);
                    } else if (isBooleanType(type)) {
                        exampleObj = BooleanUtils.toBoolean(example);
                    } else if (isNumArray(type, example)) {
                        exampleObj = parseNumArray(example);
                    } else if (isStrArray(type, example)) {
                        exampleObj = parseStrArray(example);
                    } else if (isNestArrayType(type)) {
                        exampleObj = getNestArrayValue(type, example);
                    }
                }
                val = exampleObj;
            }
            responseJson.put(row.getName(), val);
        }
        return responseJson;
    }

    private static Object getNestArrayValue(String type, String example) {
        String type_ = type.toLowerCase();
        if (ObjectUtils.isEmpty(example)) {
            return type_.indexOf("string") > -1 ? buildNestList("string") : buildNestList(1);
        }
        if (type_.indexOf("int") > -1 ||
                type_.indexOf("long") > -1 ||
                type_.indexOf("decimal") > -1 ||
                type_.indexOf("float") > -1 ||
                type_.indexOf("double") > -1 ||
                type_.indexOf("byte") > -1 ||
                type_.indexOf("short") > -1
        ) {
            return buildNestList(NumberUtils.toInt(example));
        }
        if (type_.indexOf("bool") > -1) {
            return buildNestList(true, false);
        }
        return buildNestList(example);
    }

    private static boolean isNumArray(String type, String example) {
        if (isArrayString(example) || (Objects.equals(type, "array"))) {
            example = example.substring(1, example.length() - 1);
            String[] arr = example.split(",");
            for (String num : arr) {
                if (!NumberUtils.isDigits(num)) {
                    return false;
                }
            }
            return true;
        }
        return Objects.equals(type, "num_array");
    }

    private static boolean isStrArray(String type, String example) {
        if (isArrayString(example) || (Objects.equals(type, "array"))) {
            example = example.substring(1, example.length() - 1);
            String[] arr = example.split(",");
            for (String num : arr) {
                if (!NumberUtils.isDigits(num)) {
                    return true;
                }
            }
            return true;
        }
        return Objects.equals(type, "num_array");
    }

    private static List<?> parseStrArray(String val) {
        if (ObjectUtils.isEmpty(val) || Objects.equals(val, "[]")) {
            return Collections.emptyList();
        }
        String str = val;
        if (isArrayString(str)) {
            str = str.substring(1, str.length() - 1);
        }
        String[] arr = str.split(",");
        return Stream.of(arr)
                .map(item -> {
                    String el = item.trim();
                    if (el.startsWith("\"") || el.startsWith("\'")) {
                        el = el.substring(1);
                    }
                    if (el.endsWith("\"") || el.endsWith("\'")) {
                        el = el.substring(0, el.length() - 1);
                    }
                    return el;
                })
                .collect(Collectors.toList());

    }

    private static List<?> parseNumArray(String val) {
        if (ObjectUtils.isEmpty(val) || Objects.equals(val, "[]")) {
            return Collections.emptyList();
        }
        String str = val;
        if (isArrayString(str)) {
            str = str.substring(1, str.length() - 1);
        }
        String[] arr = str.split("\\D+");
        List<Integer> list = Stream.of(arr).map(v -> NumberUtils.toInt(v, 0)).collect(Collectors.toList());
        return list;
    }

    private static boolean isArrayString(String example) {
        if (example == null) {
            return false;
        }
        return example.startsWith("[") && example.endsWith("]");
    }

    private static boolean isArrayType(String type) {
        if (ObjectUtils.isEmpty(type)) {
            return false;
        }
        type = type.toLowerCase();
        for (String t : COLLECT_TYPE_LIST) {
            if (type.contains(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否嵌套list
     */
    private static boolean isNestArrayType(String type) {
        if (ObjectUtils.isEmpty(type)) {
            return false;
        }
        return type.startsWith("List<List<") ||
                type.indexOf("[][]") > -1 ||
                type.startsWith("Collection<Collection<") ||
                type.startsWith("List<Collection<") ||
                type.startsWith("Collection<List<") ||
                type.startsWith("List<Set<") ||
                type.startsWith("Set<Set<");
    }

    private static boolean isNumberType(String type) {
        if (ObjectUtils.isEmpty(type)) {
            return false;
        }
        String typeLower = type.toLowerCase();
        for (String numberType : NUMBER_TYPES) {
            if (Objects.equals(numberType, typeLower)) {
                return true;
            }
        }
        return false;
    }

    private static List<List<?>> buildNestList(Object... val) {
        return Collections.singletonList(Arrays.asList(val));
    }

    private static boolean isBooleanType(String type) {
        if (ObjectUtils.isEmpty(type)) {
            return false;
        }
        String typeLower = type.toLowerCase();
        for (String booleanType : BOOLEAN_TYPES) {
            if (Objects.equals(booleanType, typeLower)) {
                return true;
            }
        }
        return false;
    }


    private static Object getDefaultExample(String type) {
        if (ObjectUtils.isEmpty(type)) {
            return "";
        }
        String typeLower = type.toLowerCase();
        if (isNumberType(type)) {
            return "0";
        }
        if (isBooleanType(type)) {
            return "false";
        }
        if (isNestArrayType(type)) {
            Object val = type.toLowerCase().indexOf("string") > -1 ? "string value" : 1;
            return buildNestList(val);
        }
        Object example;
        switch (typeLower) {
            case "string":
                example = "string";
                break;
            case "map":
            case "hashmap":
            case "dict":
            case "dictionary":
            case "json":
            case "obj":
            case "object":
                example = new HashMap<>();
                break;
            case "collection":
            case "list":
            case "set":
            case "arr":
            case "array":
                example = new ArrayList<>();
                break;
            case "array[string]":
                example = Collections.singletonList("string");
                break;
            case "array[byte]":
            case "array[short]":
            case "array[integer]":
            case "array[long]":
            case "array[decimal]":
                example = Collections.singletonList(0);
                break;
            case "array[float]":
            case "array[double]":
                example = Collections.singletonList(1.2);
                break;
            case "array[boolean]":
                example = Collections.singletonList(false);
                break;
            case "array[object]":
                example = Collections.singletonList(new HashMap<>());
                break;
            default: {
                example = new HashMap<>();
            }
        }
        return example instanceof String ? example : JSONObject.toJSONString(example);
    }

}
