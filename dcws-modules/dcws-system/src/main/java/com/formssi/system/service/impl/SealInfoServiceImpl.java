package com.formssi.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.core.constant.CacheNames;
import com.formssi.common.core.constant.UserConstants;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.service.DeptService;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.core.utils.TreeBuildUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.mybatis.helper.DataBaseHelper;
import com.formssi.common.redis.utils.CacheUtils;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.system.domain.*;
import com.formssi.system.domain.bo.SealInfoBo;
import com.formssi.system.domain.bo.SysDeptBo;
import com.formssi.system.domain.bo.SysDictTypeBo;
import com.formssi.system.domain.vo.HrDeptVo;
import com.formssi.system.domain.vo.SealInfoVo;
import com.formssi.system.domain.vo.SysDeptVo;
import com.formssi.system.domain.vo.SysDictTypeVo;
import com.formssi.system.mapper.*;
import com.formssi.system.service.ISealInfoService;
import com.formssi.system.service.ISysDeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     * 根据条件分页查询印章列表
     */
    @Override
    public List<SealInfoVo> selectSealInfoList(SealInfoBo info) {
        LambdaQueryWrapper<SealInfo> lqw = buildQueryWrapper(info);
        return baseMapper.selectVoList(lqw);
    }


    private LambdaQueryWrapper<SealInfo> buildQueryWrapper(SealInfoBo bo) {
        LambdaQueryWrapper<SealInfo> lqw = Wrappers.lambdaQuery();
        lqw.eq(ObjectUtil.isNotNull(bo.getSealName()), SealInfo::getSealName, bo.getSealName());
        lqw.orderByAsc(SealInfo::getSealSort);
        return lqw;
    }

}
