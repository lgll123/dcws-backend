package com.formssi.system.service.impl;


import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.system.domain.*;
import com.formssi.system.domain.bo.SealInfoBo;
import com.formssi.system.domain.vo.*;
import com.formssi.system.mapper.*;
import com.formssi.system.service.ISealInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 印章管理 服务实现
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SealInfoServiceImpl implements ISealInfoService {

    private final SealInfoMapper baseMapper;

    /**
     * 查询印章列表
     */
    @Override
    public List<SealInfoVo> selectSealInfoList(SealInfoBo info) {
        LambdaQueryWrapper<SealInfo> lqw = buildQueryWrapper(info);
        return baseMapper.selectVoList(lqw);
    }
    /**
     * 获取印章列表-分页
     */
    @Override
    public TableDataInfo<SealInfoVo> selectPageSealList(SealInfoBo info, PageQuery pageQuery) {
        Page<SealInfoVo> result = baseMapper.selectVoPage(pageQuery.build(), this.buildQueryWrapper(info));
        return TableDataInfo.build(result);
    }

    /**
     * 校验印章名称是否唯一
     *
     * @param info 印章
     * @return 结果
     */
    @Override
    public boolean checkSealNameUnique(SealInfoBo info) {
        boolean exist = baseMapper.exists(new LambdaQueryWrapper<SealInfo>()
                .eq(SealInfo::getSealName, info.getSealName())
                .ne(ObjectUtil.isNotNull(info.getId()), SealInfo::getId, info.getId()));
        return !exist;
    }

    /**
     * 新增保存印章信息
     *
     * @param info 印章信息
     * @return 结果
     */
    @Override
    public List<SealInfoVo> insertSeal(SealInfoBo info) {
        if(!ObjectUtil.isEmpty(info.getSealUser())){
            info.setSealUserHis(String.valueOf(info.getSealUser()));
        }
        SealInfo seal = MapstructUtils.convert(info, SealInfo.class);
        int row = baseMapper.insert(seal);
        if (row > 0) {
            return new ArrayList<>();
        }
        throw new ServiceException("操作失败");
    }

    /**
     * 修改保存印章信息
     *
     * @param info 印章信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SealInfoVo> updateSeal(SealInfoBo info) {
        info.setUpdateTime(new Date());
        if(!ObjectUtil.isEmpty(info.getSealUser())){
            if(StringUtils.isEmpty(info.getSealUserHis())){
                info.setSealUserHis(String.valueOf(info.getSealUser()));
            }else {
                info.setSealUserHis(info.getSealUserHis() + "," + info.getSealUser());
            }
        }
        SealInfo seal = MapstructUtils.convert(info, SealInfo.class);
        int row = baseMapper.updateById(seal);
        if (row > 0) {
            return new ArrayList<>();
        }
        throw new ServiceException("操作失败");
    }

    /**
     * 删除印章信息
     *
     * @param sealId 需要删除的印章ID
     */
    @Override
    public void deleteSealById(Long sealId) {
        baseMapper.deleteById(sealId);
    }

    private LambdaQueryWrapper<SealInfo> buildQueryWrapper(SealInfoBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<SealInfo> lqw = Wrappers.lambdaQuery();
        lqw.like(ObjectUtil.isNotNull(bo.getSealName()), SealInfo::getSealName, bo.getSealName());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
                SealInfo::getCreateTime, params.get("beginTime"), params.get("endTime"));
        lqw.orderByAsc(SealInfo::getSealSort);
        return lqw;
    }

}
