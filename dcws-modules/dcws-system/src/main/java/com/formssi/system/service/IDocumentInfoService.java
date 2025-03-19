package com.formssi.system.service;

import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.system.domain.bo.DocumentInfoBo;
import com.formssi.system.domain.bo.SealInfoBo;
import com.formssi.system.domain.vo.DocumentInfoVo;
import com.formssi.system.domain.vo.SealInfoVo;

import java.util.List;

/**
 * 资料-部门维护 服务层
 *
 * @author Lion Li
 */
public interface IDocumentInfoService {

    /**
     * 查询印章数据
     *
     * @param info 印章信息
     * @return 印章信息集合
     */
    List<DocumentInfoVo> selectDocumentInfoList(DocumentInfoBo info);

//    /**
//     * 查询印章数据-分页
//     * @param info
//     * @param pageQuery
//     * @return
//     */
//    TableDataInfo<SealInfoVo> selectPageUserList(SealInfoBo info, PageQuery pageQuery);
//
//    /**
//     * 校验印章名称是否唯一
//     *
//     * @param info 印章
//     * @return 结果
//     */
//    boolean checkSealNameUnique(SealInfoBo info);
//
//    /**
//     * 新增保存印章信息
//     *
//     * @param info 印章信息
//     * @return 结果
//     */
//    List<SealInfoVo> insertSeal(SealInfoBo info);
//
//    /**
//     * 修改保存印章信息
//     *
//     * @param info 印章信息
//     * @return 结果
//     */
//    List<SealInfoVo> updateSeal(SealInfoBo info);
//
//    /**
//     * 删除印章信息
//     *
//     * @param sealId 需要删除的印章ID
//     */
//    void deleteSealById(Long sealId);
}
