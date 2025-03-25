package com.formssi.system.service;

import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.system.domain.bo.DocumentInfoBo;
import com.formssi.system.domain.vo.DocumentInfoVo;

import java.util.List;

/**
 * 资料-部门维护 服务层
 *
 * @author Lion Li
 */
public interface IDocumentInfoService {

    /**
     * 查询资料-部门数据
     *
     * @param info 资料-部门信息
     * @return 资料-部门信息集合
     */
    List<DocumentInfoVo> selectDocumentInfoList(DocumentInfoBo info);

    /**
     * 查询资料-部门数据-分页
     * @param info
     * @param pageQuery
     * @return
     */
    TableDataInfo<DocumentInfoVo> selectPageDocumentList(DocumentInfoBo info, PageQuery pageQuery);

    /**
     * 校验部门名称是否唯一
     *
     * @param info 资料-部门
     * @return 结果
     */
    boolean checkSealNameUnique(DocumentInfoBo info);

    /**
     * 校验是否默认部门
     *
     * @param info 资料-部门
     * @return 结果
     */
    boolean checkIsDeaultDept(DocumentInfoBo info);

    /**
     * 新增保存资料-部门信息
     *
     * @param info 资料-部门信息
     * @return 结果
     */
    List<DocumentInfoVo> insertDocument(DocumentInfoBo info);

    /**
     * 修改保存资料-部门信息
     *
     * @param info 资料-部门信息
     * @return 结果
     */
    List<DocumentInfoVo> updateDocument(DocumentInfoBo info);

    /**
     * 删除资料-部门信息
     *
     * @param documentId 需要删除的资料-部门ID
     */
    void deleteDocumentById(Long documentId);
}
