package com.formssi.generator.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.generator.domain.*;
import com.formssi.generator.mapper.DmTableColumnMapper;
import com.formssi.generator.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 业务 服务层实现
 *
 * @author lizhangyu
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DmTableColumnServiceImpl extends ServiceImpl<DmTableColumnMapper, DmTableColumn> implements IDmTableColumnService {

    private final DmTableColumnMapper dmTableColumnMapper;

    @Override
    public List<DmTableColumn> selectGenTableColumnListByTableId(Long tableId) {
        return dmTableColumnMapper.selectList(new LambdaQueryWrapper<DmTableColumn>()
                .eq(DmTableColumn::getTableId, tableId)
                .orderByAsc(DmTableColumn::getSort));
    }
}

