package com.formssi.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.generator.domain.DmCommonGroup;
import com.formssi.generator.mapper.DmCommonGroupMapper;
import com.formssi.generator.model.vo.DmCommonGroupVO;
import com.formssi.generator.service.IDmCommonGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 常用字段分组 服务层实现
 *
 * @author Shen Tao
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DmCommonGroupServiceImpl extends ServiceImpl<DmCommonGroupMapper, DmCommonGroup> implements IDmCommonGroupService {

    private final DmCommonGroupMapper dmCommonGroupMapper;

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    @Override
    public List<DmCommonGroupVO> listAll() {
        LambdaQueryWrapper<DmCommonGroup> wrapper = Wrappers.lambdaQuery();
        wrapper.select(DmCommonGroup::getCommonGroupId, DmCommonGroup::getGroupName);
        wrapper.orderByAsc(DmCommonGroup::getSort);
        List<DmCommonGroup> commonGroupList = dmCommonGroupMapper.selectList(wrapper);
        List<DmCommonGroupVO> list = new ArrayList<>();
        for (DmCommonGroup item : commonGroupList) {
            DmCommonGroupVO commonGroupVO = new DmCommonGroupVO();
            commonGroupVO.setCommonGroupId(item.getCommonGroupId());
            commonGroupVO.setGroupName(item.getGroupName());
            list.add(commonGroupVO);
        }
        return list;
    }

}
