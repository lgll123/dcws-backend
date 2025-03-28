package com.formssi.system.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.system.domain.DocumentInfo;
import com.formssi.system.domain.bo.DocumentInfoBo;
import com.formssi.system.domain.vo.DocumentInfoVo;
import com.formssi.system.domain.vo.SysUserVo;
import com.formssi.system.mapper.DocumentInfoMapper;
import com.formssi.system.service.IDocumentInfoService;
import com.formssi.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 资料-部门维护 服务实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class DocumentInfoServiceImpl implements IDocumentInfoService {

    private final DocumentInfoMapper baseMapper;
    private final ISysUserService iSysUserService;

    /**
     * 查询资料-部门列表
     */
    @Override
    public List<DocumentInfoVo> selectDocumentInfoList(DocumentInfoBo info) {
        LambdaQueryWrapper<DocumentInfo> lqw = buildQueryWrapper(info);
        List<DocumentInfoVo> documentInfoVos = baseMapper.selectVoList(lqw);
        documentInfoVos.forEach(e ->{
            SysUserVo u1 = iSysUserService.selectUserById(Long.valueOf(e.getLeaderUser()));
            SysUserVo u2 = iSysUserService.selectUserById(Long.valueOf(e.getProviderUser()));
            if(!ObjectUtil.isEmpty(u1)){
                e.setLeaderName(u1.getUserName());
            }
            if(!ObjectUtil.isEmpty(u2)){
                e.setProviderName(u2.getUserName());
            }
        });
        return documentInfoVos;
    }
    /**
     * 获取资料-部门列表-分页
     */
    @Override
    public TableDataInfo<DocumentInfoVo> selectPageDocumentList(DocumentInfoBo info, PageQuery pageQuery) {
        Page<DocumentInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), this.buildQueryWrapper(info));
        List<DocumentInfoVo> records = result.getRecords();
        records.forEach(e ->{
            SysUserVo u1 = iSysUserService.selectUserById(Long.valueOf(e.getLeaderUser()));
            SysUserVo u2 = iSysUserService.selectUserById(Long.valueOf(e.getProviderUser()));
            if(!ObjectUtil.isEmpty(u1)){
                e.setLeaderName(u1.getUserName());
            }
            if(!ObjectUtil.isEmpty(u2)){
                e.setProviderName(u2.getUserName());
            }
        });
        return TableDataInfo.build(result);
    }

    /**
     * 校验部门名称是否唯一
     *
     * @param info 资料-部门
     * @return 结果
     */
    @Override
    public boolean checkSealNameUnique(DocumentInfoBo info) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<DocumentInfo>()
                .eq(DocumentInfo::getDeptName, info.getDeptName())
                .ne(ObjectUtil.isNotNull(info.getId()), DocumentInfo::getId, info.getId()));
        return !exist;
    }

    /**
     * 校验是否默认部门
     *
     * @param info 资料-部门
     * @return 结果
     */
    @Override
    public boolean checkIsDeaultDept(DocumentInfoBo info) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<DocumentInfo>()
                .eq(DocumentInfo::getIsDeaultDept, info.getIsDeaultDept())
                .ne(ObjectUtil.isNotNull(info.getId()), DocumentInfo::getId, info.getId()));
        return !exist;
    }



    /**
     * 新增保存资料-部门信息
     *
     * @param info 资料-部门信息
     * @return 结果
     */
    @Override
    public List<DocumentInfoVo> insertDocument(DocumentInfoBo info) {
        DocumentInfo documentInfo = MapstructUtils.convert(info, DocumentInfo.class);
        int row = baseMapper.insert(documentInfo);
        if (row > 0) {
            return new ArrayList<>();
        }
        throw new ServiceException("操作失败");
    }

    /**
     * 修改保存资料-部门信息
     *
     * @param info 资料-部门信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DocumentInfoVo> updateDocument(DocumentInfoBo info) {
        info.setUpdateTime(new Date());
        DocumentInfo documentInfo = MapstructUtils.convert(info, DocumentInfo.class);
        int row = baseMapper.updateById(documentInfo);
        if (row > 0) {
            return new ArrayList<>();
        }
        throw new ServiceException("操作失败");
    }

    /**
     * 删除资料-部门信息
     *
     * @param documentId 需要删除的资料-部门ID
     */
    @Override
    public void deleteDocumentById(Long documentId) {
        baseMapper.deleteById(documentId);
    }

    private LambdaQueryWrapper<DocumentInfo> buildQueryWrapper(DocumentInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<DocumentInfo> lqw = Wrappers.lambdaQuery();
        lqw.like(ObjectUtil.isNotNull(bo.getDeptName()), DocumentInfo::getDeptName, bo.getDeptName());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
                DocumentInfo::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByAsc(DocumentInfo::getCreateTime);
        return lqw;
    }

}
