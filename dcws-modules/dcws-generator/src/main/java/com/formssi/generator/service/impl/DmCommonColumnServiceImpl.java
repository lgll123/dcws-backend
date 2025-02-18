package com.formssi.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.generator.domain.DmCommonColumn;
import com.formssi.generator.mapper.DmCommonColumnMapper;
import com.formssi.generator.service.IDmCommonColumnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 常用字段 服务层实现
 *
 * @author Shen Tao
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DmCommonColumnServiceImpl extends ServiceImpl<DmCommonColumnMapper, DmCommonColumn> implements IDmCommonColumnService {

    private final DmCommonColumnMapper dmCommonColumnMapper;

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    @Override
    public List<DmCommonColumn> listByCommonGroupId(Long commonGroupId) {
        LambdaQueryWrapper<DmCommonColumn> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DmCommonColumn::getCommonGroupId, commonGroupId);
        wrapper.orderByAsc(DmCommonColumn::getSort);
        return dmCommonColumnMapper.selectList(wrapper);
    }

}
