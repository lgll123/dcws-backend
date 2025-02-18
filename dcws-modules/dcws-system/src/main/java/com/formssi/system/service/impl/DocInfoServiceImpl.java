package com.formssi.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.common.core.domain.model.LoginUser;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.CopyUtil;
import com.formssi.system.domain.*;
import com.formssi.system.domain.dto.*;
import com.formssi.system.domain.vo.DocInfoVo;
import com.formssi.system.domain.vo.ModuleDocInfoVo;
import com.formssi.system.enums.*;
import com.formssi.system.manager.DocMd5BuilderManager;
import com.formssi.system.mapper.SysDocInfoMapper;
import com.formssi.system.service.*;
import com.formssi.system.util.IdGenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/22 10:33
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DocInfoServiceImpl extends ServiceImpl<SysDocInfoMapper, SysDocInfo> implements IDocInfoService {

    private static final String REGEX_BR = "<br\\s*/*>";

    private final SysDocInfoMapper sysDocInfoMapper;
    private final IDocParamService docParamService;
    private final IPropService propService;
    private final IModuleService moduleService;
    private final IProjectService projectService;
    private final IModuleEnvironmentService moduleEnvironmentService;
    private final IEnumInfoService enumInfoService;
    private final IEnumItemService enumItemService;
    private final IModuleConfigService moduleConfigService;

    @Override
    public List<SysDocInfo> listModuleTableDoc(long moduleId) {
        List<SysDocInfo> sysDocInfoList = getDocInfoListByModuleId(moduleId);
        sortDocInfo(sysDocInfoList);
        return sysDocInfoList;
    }

    @Override
    public SysDocInfo getByDataId(String dataId) {
        return lambdaQuery()
                .eq(SysDocInfo::getDataId, dataId)
                .eq(SysDocInfo::getIsDeleted, 0)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDocInfo saveDocInfo(DocInfoDTO docInfoDTO, LoginUser loginUser) {
        return doSaveDocInfo(docInfoDTO, loginUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDocInfo updateDocInfo(DocInfoDTO docInfoDTO, LoginUser loginUser) {
        if (ObjectUtils.isEmpty(docInfoDTO.getParentId())) {
            docInfoDTO.setParentId(0L);
        }
        return doUpdateDocInfo(docInfoDTO, loginUser);
    }

    @Override
    public void deleteDocInfo(long id, LoginUser loginUser) {
        lambdaUpdate().eq(SysDocInfo::getId, id)
                .set(SysDocInfo::getModifyMode, OperationModeEnum.MANUAL.getType())
                .set(SysDocInfo::getModifierId, loginUser.getUserId())
                .set(SysDocInfo::getIsDeleted, BooleanEnum.TRUE.getType())
                .set(SysDocInfo::getDataId, IdGenUtil.nextId()) // 设置一个dataId，不与其它文档冲突
                .update();
    }

    @Override
    public DocInfoDTO getDocDetail(long docId) {
        SysDocInfo docInfo = getById(docId);
        return getDocDetail(docInfo);
    }

    @Override
    public List<SysDocInfo> listFolders(long moduleId) {
        // 过滤掉不是文件目录的文档
        return getListByModuleId(moduleId)
                .stream()
                .filter(docInfo -> docInfo.getIsFolder() == BooleanEnum.TRUE.getType())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysDocInfo createDocFolder(String folderName, long moduleId, LoginUser loginUser, Long parentId) {
        if (parentId == null) {
            parentId = 0L;
        }
        DocFolderCreateDTO docFolderCreateDTO = new DocFolderCreateDTO();
        docFolderCreateDTO.setName(folderName);
        docFolderCreateDTO.setModuleId(moduleId);
        docFolderCreateDTO.setParentId(parentId);
        docFolderCreateDTO.setLoginUser(loginUser);
        docFolderCreateDTO.setDocTypeEnum(DocTypeEnum.HTTP);
        return createDocFolder(docFolderCreateDTO);
    }

    @Override
    public void updateDocFolderName(UpdateDocFolderDTO updateDocFolderDTO) {
        Long id = updateDocFolderDTO.getId();
        String name = updateDocFolderDTO.getName();
        LoginUser loginUser = updateDocFolderDTO.getLoginUser();
        Long parentId = updateDocFolderDTO.getParentId();
        if (parentId == null) {
            parentId = 0L;
        }

        SysDocInfo folder = getById(id);
        Assert.notNull(folder, name + " 分类不存在");
        Long moduleId = folder.getModuleId();
        if (isExistFolderForUpdate(id, name, moduleId, 0)) {
            throw new ServiceException(name + " 已存在");
        }
        folder.setName(name);
        folder.setModifyMode(OperationModeEnum.MANUAL.getType());
        folder.setModifierId(loginUser.getUserId());
        folder.setIsDeleted(BooleanEnum.FALSE.getType());
        folder.setParentId(parentId);
        baseMapper.updateById(folder);
    }

    @Override
    public List<DocInfoDTO> listDocDetail(List<Long> docIdList) {
        if (CollectionUtils.isEmpty(docIdList)) {
            return Collections.emptyList();
        }
        List<SysDocInfo> docInfos = this.listDocByIds(docIdList);
        return docInfos.stream()
                .map(this::getDocDetail)
                .collect(Collectors.toList());
    }

    @Override
    public void updateVersion(Long docId, String version) {
        if (version == null) {
            version = "";
        }
        SysDocInfo docInfo = getById(docId);
        DocInfoDTO docInfoDTO = CopyUtil.copyBean(docInfo, DocInfoDTO::new);
        docInfoDTO.setVersion(version);
        // 重新设置下dataId
        String dataId = docInfoDTO.buildDataId();
        if (checkExist(dataId, docInfo.getId())) {
            throw new ServiceException("相同版本号已存在");
        }
        docInfo.setDataId(dataId);
        docInfo.setVersion(version);
        baseMapper.updateById(docInfo);
    }

    @Override
    public void updateStatus(long docId, byte status, LoginUser loginUser) {
        DocInfoDTO docDetail = getDocForm(docId);
        if (Objects.equals(status, docDetail.getStatus())) {
            return;
        }
        docDetail.setStatus(status);
        doUpdateDocBaseInfo(docDetail, loginUser);
    }

    @Override
    public List<ModuleDocInfoVo> getListByProjectId(Long projectId) {
        List<SysModule> moduleList = moduleService.listProjectModules(projectId);

        if (CollectionUtils.isEmpty(moduleList)) {
            return Collections.emptyList();
        }

        List<ModuleDocInfoVo> moduleDocInfoVoList = new ArrayList<>();

        // 遍历
        moduleList.forEach(item -> {
            ModuleDocInfoVo moduleDocInfoVo = CopyUtil.copyBean(item, ModuleDocInfoVo::new);
            // 根据模块id获取模块下的接口文档
            List<SysDocInfo> sysDocInfos = listModuleTableDoc(item.getId());
            List<DocInfoVo> docInfoVos = CopyUtil.copyList(sysDocInfos, DocInfoVo::new);
            moduleDocInfoVo.setDocInfoVoList(docInfoVos);
            moduleDocInfoVoList.add(moduleDocInfoVo);
        });

        return moduleDocInfoVoList;
    }

    /**
     * 创建文件夹
     * @param docFolderCreateDTO 文件夹创建DTO
     * @return 返回文本信息
     */
    public SysDocInfo createDocFolder(DocFolderCreateDTO docFolderCreateDTO) {
        DocInfoDTO docInfoDTO = new DocInfoDTO();
        docInfoDTO.setName(docFolderCreateDTO.getName());
        docInfoDTO.setModuleId(docFolderCreateDTO.getModuleId());
        docInfoDTO.setParentId(docFolderCreateDTO.getParentId());
        if (docFolderCreateDTO.getDocTypeEnum() != null) {
            docInfoDTO.setType(docFolderCreateDTO.getDocTypeEnum().getType());
        }
        docInfoDTO.setIsFolder(BooleanEnum.TRUE.getType());
        docInfoDTO.setAuthor(docFolderCreateDTO.getAuthor());
        docInfoDTO.setOrderIndex(docFolderCreateDTO.getOrderIndex());
        SysDocInfo sysDocInfo = insertDocInfo(docInfoDTO, docFolderCreateDTO.getLoginUser());
        Map<String, ?> props = docFolderCreateDTO.getProps();
        propService.saveProps(props, sysDocInfo.getId(), PropTypeEnum.DOC_INFO_PROP);
        return sysDocInfo;
    }

    /**
     * 执行更新文档信息
     * @param docInfoDTO 文档信息DTO
     * @param loginUser 登录用户信息
     * @return 返回文档信息
     */
    public SysDocInfo doUpdateDocInfo(DocInfoDTO docInfoDTO, LoginUser loginUser) {
        SysDocInfo sysDocInfoOld = getById(docInfoDTO.getId());
        String oldMd5 = sysDocInfoOld.getMd5();
        // 修改基本信息
        SysDocInfo sysDocInfo = this.modifyDocInfo(sysDocInfoOld, docInfoDTO, loginUser);
        // 修改参数
        doUpdateParams(sysDocInfo, docInfoDTO, loginUser);
        return sysDocInfo;
    }

    /**
     * 修改文档基本信息。参数除外
     *
     * @param docInfoDTO
     * @param loginUser
     * @return
     */
    private SysDocInfo doUpdateDocBaseInfo(DocInfoDTO docInfoDTO, LoginUser loginUser) {
        SysDocInfo docInfoOld = getById(docInfoDTO.getId());
        // 修改基本信息
        SysDocInfo docInfo = this.modifyDocInfo(docInfoOld, docInfoDTO, loginUser);
        return docInfo;
    }

    /**
     * 返回文档详情
     *
     * @param docId 文档id
     * @return 返回文档详情
     */
    private DocInfoDTO getDocForm(long docId) {
        SysDocInfo docInfo = getById(docId);
        return getDocInfoDTO(docInfo);
    }

    /**
     * 根据主键id和数据id查询文档
     * @param dataId 数据id
     * @param id 主键id
     * @return 返回结果
     */
    private boolean checkExist(String dataId, Long id) {
        SysDocInfo sysDocInfo = lambdaQuery().eq(SysDocInfo::getDataId, dataId)
                .eq(SysDocInfo::getId, id)
                .eq(SysDocInfo::getIsDeleted, BooleanEnum.FALSE.getType())
                .one();

        return sysDocInfo != null;
    }

    /**
     * 获取文档详情
     * @param docInfo 文档信息
     * @return 返回文档详情
     */
    private DocInfoDTO getDocDetail(SysDocInfo docInfo) {
        DocInfoDTO docInfoDTO = this.getDocInfoDTO(docInfo);
        Long moduleId = docInfoDTO.getModuleId();
        List<SysDocParam> globalHeaders = moduleConfigService.listGlobalHeaders(moduleId);
        List<SysDocParam> globalParams = moduleConfigService.listGlobalParams(moduleId);
        List<SysDocParam> globalReturns = moduleConfigService.listGlobalReturns(moduleId);
        docInfoDTO.setGlobalHeaders(CopyUtil.copyList(globalHeaders, DocParamDTO::new));
        docInfoDTO.setGlobalParams(CopyUtil.copyList(globalParams, DocParamDTO::new));
        docInfoDTO.setGlobalReturns(CopyUtil.copyList(globalReturns, DocParamDTO::new));
        docInfoDTO.getGlobalHeaders().forEach(docParamDTO -> docParamDTO.setGlobal(true));
        return docInfoDTO;
    }

    /**
     * 获取文档信息DTO
     * @param docInfo 文档信息
     * @return 返回文档信息DTO
     */
    private DocInfoDTO getDocInfoDTO(SysDocInfo docInfo) {
        Assert.notNull(docInfo, () -> "文档不存在");
        DocInfoDTO docInfoDTO = CopyUtil.copyBean(docInfo, DocInfoDTO::new);

        // 获取模块信息
        Long moduleId = docInfo.getModuleId();
        SysModule module = moduleService.getById(moduleId);

        // 获取项目信息
        docInfoDTO.setSpaceId(projectService.getSpaceId(module.getProjectId()));
        docInfoDTO.setProjectId(module.getProjectId());
        docInfoDTO.setModuleType(module.getType());

        // 获取模块运行环境信息
        List<SysModuleEnvironment> debugEnvs = moduleEnvironmentService.listModuleEnvironment(moduleId);
        docInfoDTO.setDebugEnvs(CopyUtil.copyList(debugEnvs, ModuleEnvironmentDTO::new));

        // 获取文档参数
        List<SysDocParam> params = docParamService.getListByDocId(docInfo.getId());
        params.sort(Comparator.comparing(SysDocParam::getOrderIndex));
        Map<Byte, List<SysDocParam>> paramsMap = params.stream().collect(Collectors.groupingBy(SysDocParam::getStyle));
        List<SysDocParam> pathParams = paramsMap.getOrDefault(ParamStyleEnum.PATH.getStyle(), Collections.emptyList());
        List<SysDocParam> headerParams = paramsMap.getOrDefault(ParamStyleEnum.HEADER.getStyle(), Collections.emptyList());
        List<SysDocParam> queryParams = paramsMap.getOrDefault(ParamStyleEnum.QUERY.getStyle(), Collections.emptyList());
        List<SysDocParam> requestParams = paramsMap.getOrDefault(ParamStyleEnum.REQUEST.getStyle(), Collections.emptyList());
        List<SysDocParam> responseParams = paramsMap.getOrDefault(ParamStyleEnum.RESPONSE.getStyle(), Collections.emptyList());
        List<SysDocParam> errorCodeParams = paramsMap.getOrDefault(ParamStyleEnum.ERROR_CODE.getStyle(), new ArrayList<>(0));
        docInfoDTO.setPathParams(CopyUtil.copyList(pathParams, DocParamDTO::new));
        docInfoDTO.setHeaderParams(CopyUtil.copyList(headerParams, DocParamDTO::new));
        docInfoDTO.setHeaderParamsRaw(CopyUtil.copyList(headerParams, DocParamDTO::new));
        docInfoDTO.setQueryParams(CopyUtil.copyList(queryParams, DocParamDTO::new));
        docInfoDTO.setRequestParams(CopyUtil.copyList(requestParams, DocParamDTO::new));
        docInfoDTO.setResponseParams(CopyUtil.copyList(responseParams, DocParamDTO::new));
        docInfoDTO.setErrorCodeParams(CopyUtil.copyList(errorCodeParams, DocParamDTO::new));

        // 绑定枚举信息
        bindEnumInfo(docInfoDTO.getQueryParams());
        bindEnumInfo(docInfoDTO.getRequestParams());

        // 构建DubboInfoDTO
        DubboInfoDTO dubboInfoDTO = buildDubboInfoDTO(docInfo);
        docInfoDTO.setDubboInfo(dubboInfoDTO);
        return docInfoDTO;
    }

    /**
     * 构建DubboInfoDTO
     * @param docInfo 文档信息
     * @return 返回DubboInfoDTO
     */
    private DubboInfoDTO buildDubboInfoDTO(SysDocInfo docInfo) {
        if (docInfo.getType() == DocTypeEnum.DUBBO.getType()) {
            Map<String, String> docProps = propService.getDocProps(docInfo.getParentId());
            DubboInfoDTO dubboInfoDTO = new DubboInfoDTO();
            dubboInfoDTO.setProtocol(docProps.get("protocol"));
            dubboInfoDTO.setDependency(docProps.get("dependency"));
            dubboInfoDTO.setAuthor(docProps.get("author"));
            dubboInfoDTO.setInterfaceName(docProps.get("interfaceName"));
            return dubboInfoDTO;
        }
        return null;
    }

    /**
     * 绑定枚举信息
     *
     * @param docParamDTOS 文档参数DTO列表
     */
    private void bindEnumInfo(List<DocParamDTO> docParamDTOS) {
        for (DocParamDTO docParamDTO : docParamDTOS) {
            Long enumId = docParamDTO.getEnumId();
            if (enumId != null && enumId > 0) {
                SysEnumInfo enumInfo = enumInfoService.getById(enumId);
                if (enumInfo == null) {
                    continue;
                }
                EnumInfoDTO enumInfoDTO = CopyUtil.copyBean(enumInfo, EnumInfoDTO::new);
                List<EnumItemDTO> enumItemDTOS = enumItemService.getListByEnumId(enumId);
                enumInfoDTO.setItems(enumItemDTOS);
                docParamDTO.setEnumInfo(enumInfoDTO);
            } else if (DataType.ENUM.getType().equalsIgnoreCase(docParamDTO.getType()) && StringUtils.hasText(docParamDTO.getDescription())) {
                String description = docParamDTO.getDescription();
                EnumInfoDTO enumInfoDTO = new EnumInfoDTO();
                String[] arr;
                if (description.contains("<br")) {
                    arr = description.split(REGEX_BR);
                } else if (description.contains("、")) {
                    arr = description.split("、");
                } else {
                    arr = new String[]{description};
                }
                List<EnumItemDTO> items = Arrays.stream(arr)
                        .map(val -> {
                            EnumItemDTO enumItemDTO = new EnumItemDTO();
                            enumItemDTO.setName(val);
                            enumItemDTO.setValue(val);
                            return enumItemDTO;
                        })
                        .collect(Collectors.toList());
                enumInfoDTO.setItems(items);
                docParamDTO.setEnumInfo(enumInfoDTO);
            }

        }
    }

    /**
     * 根据文档id列表获取文档信息列表
     * @param docIdList 文档id列表
     * @return 返回文档信息列表
     */
    private List<SysDocInfo> listDocByIds(List<Long> docIdList) {
        if (CollectionUtils.isEmpty(docIdList)) {
            return Collections.emptyList();
        }
        // 根据主键id列表查询
        List<SysDocInfo> list = lambdaQuery()
                .eq(SysDocInfo::getIsDeleted, BooleanEnum.FALSE.getType())
                .in(SysDocInfo::getId, docIdList)
                .list();
        sortDocInfo(list);
        return list;
    }

    /**
     * 校验文件夹是否存在
     * @param id 主键id
     * @param folderName 文件夹名称
     * @param moduleId 模块id
     * @param parentId 父级id
     * @return 返回值
     */
    private boolean isExistFolderForUpdate(long id, String folderName, long moduleId, long parentId) {
        SysDocInfo docInfo = getByModuleIdAndParentIdAndName(moduleId, parentId, folderName);
        return docInfo != null && docInfo.getId() != id;
    }

    /**
     * 根据模块id、父级id、文档名称获取文档信息
     * @param moduleId 模块id
     * @param parentId 父级id
     * @param name 文档名称
     * @return 返回文档信息
     */
    private SysDocInfo getByModuleIdAndParentIdAndName(long moduleId, long parentId, String name) {
        return lambdaQuery().eq(SysDocInfo::getModuleId, moduleId)
                .eq(SysDocInfo::getParentId, parentId)
                .eq(SysDocInfo::getName, name)
                .eq(SysDocInfo::getIsDeleted, BooleanEnum.FALSE.getType())
                .one();
    }

    /**
     * 根据模块id获取文档信息列表
     * @param moduleId 模块id
     * @return 返回文档信息列表
     */
    private List<SysDocInfo> getListByModuleId(Long moduleId) {
        List<SysDocInfo> sysDocInfoList = lambdaQuery()
                .eq(SysDocInfo::getModuleId, moduleId)
                .eq(SysDocInfo::getIsDeleted, BooleanEnum.FALSE.getType())
                .list();
        sortDocInfo(sysDocInfoList);
        return sysDocInfoList;
    }

    /**
     * 修改文档信息
     * @param sysDocInfo 文档信息
     * @param docInfoDTO 文档信息DTO
     * @param loginUser 用户登录信息
     * @return 返回文档信息
     */
    private SysDocInfo modifyDocInfo(SysDocInfo sysDocInfo, DocInfoDTO docInfoDTO, LoginUser loginUser) {
        String newMd5 = getDocMd5(docInfoDTO);
        CopyUtil.copyPropertiesIgnoreNull(docInfoDTO, sysDocInfo);
        sysDocInfo.setMd5(newMd5);
        // 手动赋值
        sysDocInfo.setCreateMode(OperationModeEnum.MANUAL.getType());
        sysDocInfo.setModifyMode(OperationModeEnum.MANUAL.getType());
        sysDocInfo.setCreatorId(loginUser.getUserId());
        sysDocInfo.setCreatorName(loginUser.getNickname());
        sysDocInfo.setModifierId(loginUser.getUserId());
        sysDocInfo.setModifierName(loginUser.getNickname());
        sysDocInfo.setDataId(docInfoDTO.buildDataId());
        sysDocInfo.setDocKey(docInfoDTO.buildDocKey());
        if (sysDocInfo.getDescription() == null) {
            sysDocInfo.setDescription("");
        }
        if (sysDocInfo.getDeprecated() == null) {
            sysDocInfo.setDeprecated("$false$");
        }
        baseMapper.updateById(sysDocInfo);
        return sysDocInfo;
    }

    /**
     * 执行保存文档信息
     * @param docInfoDTO 文档信息DTO
     * @param loginUser 登录用户信息
     * @return 返回文档信息
     */
    private SysDocInfo doSaveDocInfo(DocInfoDTO docInfoDTO, LoginUser loginUser) {
        // 修改基本信息
        SysDocInfo sysDocInfo = this.saveBaseInfo(docInfoDTO, loginUser);
        // 修改参数
        doUpdateParams(sysDocInfo, docInfoDTO, loginUser);
        return sysDocInfo;
    }

    /**
     * 更新参数
     * @param sysDocInfo 文档信息
     * @param docInfoDTO 文档信息DTO
     * @param loginUser 用户登录信息
     */
    private void doUpdateParams(SysDocInfo sysDocInfo, DocInfoDTO docInfoDTO, LoginUser loginUser) {
        docParamService.

                saveParams(sysDocInfo, docInfoDTO.getPathParams(), ParamStyleEnum.PATH, loginUser);
        docParamService.saveParams(sysDocInfo, docInfoDTO.getHeaderParams(), ParamStyleEnum.HEADER, loginUser);
        docParamService.saveParams(sysDocInfo, docInfoDTO.getQueryParams(), ParamStyleEnum.QUERY, loginUser);
        docParamService.saveParams(sysDocInfo, docInfoDTO.getRequestParams(), ParamStyleEnum.REQUEST, loginUser);
        docParamService.saveParams(sysDocInfo, docInfoDTO.getResponseParams(), ParamStyleEnum.RESPONSE, loginUser);
        docParamService.saveParams(sysDocInfo, docInfoDTO.getErrorCodeParams(), ParamStyleEnum.ERROR_CODE, loginUser);
    }

    /**
     * 保存文档
     * @param docInfoDTO 文档信息DTO
     * @param loginUser 登录用户信息
     * @return 返回文档信息
     */
    private SysDocInfo saveBaseInfo(DocInfoDTO docInfoDTO, LoginUser loginUser) {
        return this.insertDocInfo(docInfoDTO, loginUser);
    }

    /**
     * 插入文档信息
     * @param docInfoDTO 文档信息DTO
     * @param loginUser 登录用户信息
     * @return 返回文档信息
     */
    private SysDocInfo insertDocInfo(DocInfoDTO docInfoDTO, LoginUser loginUser) {
        SysDocInfo sysDocInfo = buildDocInfo(docInfoDTO, loginUser);
        String docMd5 = getDocMd5(docInfoDTO);
        sysDocInfo.setMd5(docMd5);
        sysDocInfoMapper.saveDocInfo(sysDocInfo);
        Long id = sysDocInfo.getId();
        // 修复使用非MYSQL数据库插入数据id不返回问题
        if (id == null) {
            SysDocInfo one = this.getByDataId(sysDocInfo.getDataId());
            if (one != null) {
                id = one.getId();
                sysDocInfo.setId(id);
            }
        }
        if (id != null) {
            docInfoDTO.setId(id);
        }
        return sysDocInfo;
    }

    public String getDocMd5(DocInfoDTO docInfoDTO) {
        return DocMd5BuilderManager.getBuilder().buildMd5(docInfoDTO);
    }

    /**
     * 构建文本信息
     * @param docInfoDTO 文本
     * @param loginUser
     * @return
     */
    private SysDocInfo buildDocInfo(DocInfoDTO docInfoDTO, LoginUser loginUser) {
        SysDocInfo sysDocInfo = CopyUtil.copyBean(docInfoDTO, SysDocInfo::new);
        // 手动赋值
        sysDocInfo.setCreateMode(OperationTypeEnum.MANUAL_OPERATION.getType());
        sysDocInfo.setModifyMode(OperationTypeEnum.MANUAL_OPERATION.getType());
        sysDocInfo.setCreatorId(loginUser.getUserId());
        sysDocInfo.setCreatorName(loginUser.getNickname());
        sysDocInfo.setModifierId(loginUser.getUserId());
        sysDocInfo.setModifierName(loginUser.getNickname());
        sysDocInfo.setDataId(docInfoDTO.buildDataId());
        sysDocInfo.setDocKey(docInfoDTO.buildDocKey());
        if (sysDocInfo.getDescription() == null) {
            sysDocInfo.setDescription("");
        }

        if (sysDocInfo.getDeprecated() == null) {
            sysDocInfo.setDeprecated("$false$");
        }

        return sysDocInfo;
    }

    /**
     * 文档排序
     * @param sysDocInfoList 文档列表
     */
    private void sortDocInfo(List<SysDocInfo> sysDocInfoList) {
        if (CollectionUtils.isEmpty(sysDocInfoList)) {
            return;
        }
        String value = DocSortTypeEnum.BY_ORDER.getType();
        Comparator<SysDocInfo> comparator;
        switch (DocSortTypeEnum.of(value)) {
            case BY_URL:
                comparator = Comparator.comparing(SysDocInfo::getUrl).thenComparing(SysDocInfo::getOrderIndex);
                break;
            case BY_NAME:
                comparator = Comparator.comparing(SysDocInfo::getName).thenComparing(SysDocInfo::getOrderIndex);
                break;
            default: {
                comparator = Comparator.comparing(SysDocInfo::getOrderIndex);
            }
        }
        sysDocInfoList.sort(comparator);
    }

    /**
     * 根据模块id获取文档信息列表
     * @param moduleId 模块id
     * @return 返回文档信息列表
     */
    private List<SysDocInfo> getDocInfoListByModuleId(Long moduleId) {
        return lambdaQuery()
                .eq(SysDocInfo::getModuleId, moduleId)
                .eq(SysDocInfo::getIsShow, BooleanEnum.TRUE.getType())
                .eq(SysDocInfo::getIsDeleted, 0)
                .list();
    }

}
