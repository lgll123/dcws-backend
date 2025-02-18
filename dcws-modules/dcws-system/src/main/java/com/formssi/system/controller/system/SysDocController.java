package com.formssi.system.controller.system;

import com.formssi.common.core.domain.R;
import com.formssi.common.core.domain.model.LoginUser;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.CopyUtil;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.system.domain.SysDocInfo;
import com.formssi.system.domain.SysModuleEnvironment;
import com.formssi.system.domain.SysModuleEnvironmentParam;
import com.formssi.system.domain.bo.*;
import com.formssi.system.domain.dto.DocInfoDTO;
import com.formssi.system.domain.dto.DocParamDTO;
import com.formssi.system.domain.dto.UpdateDocFolderDTO;
import com.formssi.system.domain.vo.*;
import com.formssi.system.enums.DocTypeEnum;
import com.formssi.system.enums.ParamStyleEnum;
import com.formssi.system.service.IDocInfoService;
import com.formssi.system.service.IModuleEnvironmentParamService;
import com.formssi.system.service.IModuleEnvironmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author lizhangyu
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/doc")
public class SysDocController {

    private final IDocInfoService docInfoService;
    private final IModuleEnvironmentService moduleEnvironmentService;
    private final IModuleEnvironmentParamService moduleEnvironmentParamService;

    /**
     * 获取项目文档目录，可用于文档菜单
     *
     * @param projectId 项目id
     * @return 返回结果
     */
    @GetMapping("/getListByProjectId")
    public R<List<ModuleDocInfoVo>> getListByProjectId(Long projectId) {
        List<ModuleDocInfoVo> projectModuleVoList = docInfoService.getListByProjectId(projectId);
        return R.ok(projectModuleVoList);
    }

    /**
     * 获取项目文档目录，可用于文档菜单
     *
     * @param moduleId 模块id
     * @return 返回结果
     */
    @GetMapping("/list")
    public R<List<DocInfoVo>> listProjectDoc(Long moduleId) {
        List<SysDocInfo> sysDocInfos = docInfoService.listModuleTableDoc(moduleId);
        List<DocInfoVo> docInfoVos = CopyUtil.copyList(sysDocInfos, DocInfoVo::new);
        return R.ok(docInfoVos);
    }

    /**
     * 保存文档信息
     *
     * @param param 保存文档请求参数
     * @return 返回
     */
    @PostMapping("/save")
    public R<IdVo> save(@RequestBody @Valid DocInfoSaveBo param) {
        DocInfoDTO docInfoDTO = CopyUtil.deepCopy(param, DocInfoDTO.class);
        LoginUser loginUser = LoginHelper.getLoginUser();

        if (loginUser == null) {
            throw new ServiceException("用户未登录，请先登录");
        }

        if (StringUtils.isEmpty(docInfoDTO.getAuthor())) {
            docInfoDTO.setAuthor(loginUser.getNickname());
        }
        Long id = docInfoDTO.getId();
        SysDocInfo sysDocInfo;
        if (id == null) {
            // 检查是否存在
            String dataId = docInfoDTO.buildDataId();
            SysDocInfo exist = docInfoService.getByDataId(dataId);
            if (exist != null) {
                Byte type = param.getType();
                if (Objects.equals(type, DocTypeEnum.HTTP.getType())) {
                    String tpl = "【%s】%s";
                    throw new ServiceException(String.format(tpl, exist.getHttpMethod(), exist.getUrl()) + " 已存在");
                } else {
                    String tpl = "文档标题 %s";
                    throw new ServiceException(String.format(tpl, param.getName()) + " 已存在");
                }
            }
            this.nullParamsId(docInfoDTO);
            // 不存在则保存文档
            sysDocInfo = docInfoService.saveDocInfo(docInfoDTO, loginUser);
        } else {
            sysDocInfo = docInfoService.updateDocInfo(docInfoDTO, loginUser);
        }
        return R.ok(new IdVo(sysDocInfo.getId()));
    }

    /**
     * 删除
     * @param param 请求参数
     * @return 返回
     */
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody @Valid IdParamBo param) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        docInfoService.deleteDocInfo(param.getId(), loginUser);
        return R.ok();
    }

    /**
     * 查询文档详细信息
     *
     * @param docId 文档id
     * @return 返回记录
     */
    @GetMapping("/detail")
    public R<DocInfoDTO> detail(Long docId) {
        DocInfoDTO docInfoDTO = docInfoService.getDocDetail(docId);
        return R.ok(docInfoDTO);
    }

    /**
     * 获取模块分类
     *
     * @param moduleId 模块id
     * @return 返回文档信息列表
     */
    @GetMapping("/folder/list")
    public R<List<DocInfoDTO>> list(Long moduleId) {
        List<SysDocInfo> folders = docInfoService.listFolders(moduleId);
        return R.ok(CopyUtil.copyList(folders, DocInfoDTO::new));
    }

    /**
     * 添加分类
     *
     * @param param 文件夹添加请求参数
     * @return 返回
     */
    @PostMapping("/folder/add")
    public R<Void> addFolder(@RequestBody @Valid DocFolderAddParamBo param) {
        String name = param.getName();
        Long moduleId = param.getModuleId();
        LoginUser loginUser = LoginHelper.getLoginUser();
        docInfoService.createDocFolder(name, moduleId, loginUser, param.getParentId());
        return R.ok();
    }

    /**
     * 修改分类名称
     *
     * @param param 文件夹修改请求参数
     * @return 返回
     */
    @PostMapping("/folder/update")
    public R<Void> updateFolder(@RequestBody @Valid DocFolderUpdateParamBo param) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        UpdateDocFolderDTO updateDocFolderDTO = CopyUtil.copyBean(param, UpdateDocFolderDTO::new);
        updateDocFolderDTO.setLoginUser(loginUser);
        docInfoService.updateDocFolderName(updateDocFolderDTO);
        return R.ok();
    }

    /**
     * 文档搜索
     * @param docInfoSearch 文档搜索
     * @return 返回文档列表
     */
    @PostMapping("/detail/search")
    public R<List<DocInfoDTO>> listDocInfoDetail(@RequestBody DocInfoSearchBo docInfoSearch) {
        List<Long> docIdList = docInfoSearch.getDocIdList();
        if (CollectionUtils.isEmpty(docIdList)) {
            return R.ok(Collections.emptyList());
        }
        List<DocInfoDTO> docInfoDTOList = docInfoService.listDocDetail(docIdList);
        return R.ok(docInfoDTOList);
    }

    /**
     * 排序索引修改
     * @param param 排序索引请求参数
     * @return 返回
     */
    @PostMapping("/orderindex/update")
    public R<Void> updateOrderIndex(@RequestBody UpdateOrderIndexParamBo param) {
        SysDocInfo docInfo = docInfoService.getById(param.getId());
        docInfo.setOrderIndex(param.getOrderIndex());
        docInfoService.updateById(docInfo);
        return R.ok();
    }

    /**
     * 版本修改
     * @param param 请求参数
     * @return 返回
     */
    @PostMapping("/version/update")
    public R<Void> updateVersion(@RequestBody UpdateVersionParamBo param) {
        docInfoService.updateVersion(param.getId(), param.getVersion());
        return R.ok();
    }

    /**
     * 根据环境id获取全局请求参数
     * @param environmentId 环境id
     * @return 返回
     */
    @GetMapping("/headers/global")
    public R<List<DocParamDTO>> headersGlobal(Long environmentId) {
        List<SysModuleEnvironmentParam> moduleEnvironmentParams = moduleEnvironmentParamService.listByEnvironmentAndStyle(environmentId, ParamStyleEnum.HEADER.getStyle());
        List<DocParamDTO> docParamDTOS = CopyUtil.copyList(moduleEnvironmentParams, DocParamDTO::new, docParamDTO -> docParamDTO.setGlobal(true));
        return R.ok(docParamDTOS);
    }

    /**
     * 获取全局参数
     * @param moduleId 模块id
     * @return 返回全局参数
     */
    @GetMapping("/globals")
    public R<ModuleGlobalParamsVo> globals(Long moduleId) {
        SysModuleEnvironment moduleEnvironment = moduleEnvironmentService.getFirst(moduleId);
        if (moduleEnvironment == null) {
            return R.ok();
        }
        Long environmentId = moduleEnvironment.getId();
        List<SysModuleEnvironmentParam> list = moduleEnvironmentParamService.listAllByEnvironment(environmentId);
        // 根据style分组
        Map<Byte, List<SysModuleEnvironmentParam>> styleMap = list.stream().collect(Collectors.groupingBy(SysModuleEnvironmentParam::getStyle));
        // 获取全局请求头
        List<SysModuleEnvironmentParam> globalHeaders = styleMap.getOrDefault(ParamStyleEnum.HEADER.getStyle(), Collections.emptyList());
        // 获取全局请求
        List<SysModuleEnvironmentParam> globalRequest = styleMap.getOrDefault(ParamStyleEnum.REQUEST.getStyle(), Collections.emptyList());
        // 获取全局响应
        List<SysModuleEnvironmentParam> globalResponse = styleMap.getOrDefault(ParamStyleEnum.RESPONSE.getStyle(), Collections.emptyList());
        ModuleGlobalParamsVo moduleGlobalParamsVO = new ModuleGlobalParamsVo();
        moduleGlobalParamsVO.setGlobalHeaders(CopyUtil.copyList(globalHeaders, DocParamDTO::new));
        moduleGlobalParamsVO.setGlobalParams(CopyUtil.copyList(globalRequest, DocParamDTO::new));
        moduleGlobalParamsVO.setGlobalReturns(CopyUtil.copyList(globalResponse, DocParamDTO::new));
        return R.ok(moduleGlobalParamsVO);
    }

    @PostMapping("/status/update")
    public R<Void> updateStatus(@RequestBody UpdateStatusParamBo param) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        docInfoService.updateStatus(param.getId(), param.getStatus(), loginUser);
        return R.ok();
    }

    @PostMapping("/modify")
    public R<Void> updateInfo(@Valid @RequestBody ModifyInfoParamBo param) {
        SysDocInfo docInfo = CopyUtil.copyBean(param, SysDocInfo::new);
        docInfoService.updateById(docInfo);
        return R.ok();
    }

    /**
     * 将参数的id设置成null
     *
     * @param docInfoDTO docInfoDTO
     */
    private void nullParamsId(DocInfoDTO docInfoDTO) {
        nullId(docInfoDTO.getHeaderParams());
        nullId(docInfoDTO.getPathParams());
        nullId(docInfoDTO.getQueryParams());
        nullId(docInfoDTO.getRequestParams());
        nullId(docInfoDTO.getResponseParams());
        nullId(docInfoDTO.getErrorCodeParams());
    }

    private void nullId(List<DocParamDTO> docParamDTOList) {
        if (CollectionUtils.isEmpty(docParamDTOList)) {
            return;
        }
        long docId = 0;
        for (DocParamDTO docParamDTO : docParamDTOList) {
            docParamDTO.setId(null);
            docParamDTO.setDocId(docId);
            List<DocParamDTO> children = docParamDTO.getChildren();
            nullId(children);
        }
    }

}
