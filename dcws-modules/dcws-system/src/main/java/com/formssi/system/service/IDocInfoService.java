package com.formssi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.common.core.domain.model.LoginUser;
import com.formssi.system.domain.SysDocInfo;
import com.formssi.system.domain.dto.DocInfoDTO;
import com.formssi.system.domain.dto.UpdateDocFolderDTO;
import com.formssi.system.domain.vo.ModuleDocInfoVo;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/22 10:33
 */
public interface IDocInfoService extends IService<SysDocInfo> {

    /**
     * 查询模块下的所有文档
     *
     * @param moduleId 模块id
     * @return 返回文档
     */
    List<SysDocInfo> listModuleTableDoc(long moduleId);

    /**
     * 根据文件id获取文件信息
     * @param dataId 文件id
     * @return 返回文件信息
     */
    SysDocInfo getByDataId(String dataId);

    /**
     * 保存文档
     * @param docInfoDTO 文档信息DTO
     * @param loginUser 登录用户信息
     * @return 返回文档信息
     */
    SysDocInfo saveDocInfo(DocInfoDTO docInfoDTO, LoginUser loginUser);

    /**
     * 更新文档
     * @param docInfoDTO 文档信息DTO
     * @param loginUser 登录用户信息
     * @return 返回文档信息
     */
    SysDocInfo updateDocInfo(DocInfoDTO docInfoDTO, LoginUser loginUser);

    /**
     * 删除文档信息
     * @param id 文档id
     * @param loginUser 登录用户信息
     */
    void deleteDocInfo(long id, LoginUser loginUser);

    /**
     * 根据文档id获取文档详情DTO
     * @param docId 文档id
     * @return 返回文档信息DTO
     */
    DocInfoDTO getDocDetail(long docId);

    /**
     * 查询模块下面所有分类
     *
     * @param moduleId 模块id
     * @return 返回分类
     */
    List<SysDocInfo> listFolders(long moduleId);

    /**
     * 创建文档分类，不检查名称是否存在
     *
     * @param folderName 分类名称
     * @param moduleId   模块id
     * @param loginUser       操作人
     * @param parentId   父节点id
     * @return 返回添加后的文档
     */
    SysDocInfo createDocFolder(String folderName, long moduleId, LoginUser loginUser, Long parentId);

    /**
     * 修改分类名称
     *
     * @param updateDocFolderDTO 更新文件夹DTO
     */
    void updateDocFolderName(UpdateDocFolderDTO updateDocFolderDTO);

    /**
     * 根据文档id列表获取文档列表
     * @param docIdList 文档id列表
     * @return 返回文档DTO列表
     */
    List<DocInfoDTO> listDocDetail(List<Long> docIdList);

    /**
     * 根据文档id更新版本
     * @param docId 文档id
     * @param version 版本
     */
    void updateVersion(Long docId, String version);

    /**
     * 更新文档状态
     *
     * @param docId 文档id
     * @param status 状态
     * @param loginUser 登录用户
     */
    void updateStatus(long docId, byte status, LoginUser loginUser);

    /**
     * 根据项目id获取所有接口文档
     * @param projectId 项目id
     * @return 返回所有接口文档
     */
    List<ModuleDocInfoVo> getListByProjectId(Long projectId);
}
