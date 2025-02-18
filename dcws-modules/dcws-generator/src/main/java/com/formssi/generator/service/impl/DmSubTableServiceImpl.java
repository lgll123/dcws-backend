package com.formssi.generator.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.generator.domain.DmSubTable;
import com.formssi.generator.mapper.DmSubTableMapper;
import com.formssi.generator.service.IDmSubTableService;
import com.formssi.system.util.IdGenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2025/1/3 14:53
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DmSubTableServiceImpl extends ServiceImpl<DmSubTableMapper, DmSubTable> implements IDmSubTableService {

    private final DmSubTableMapper dmSubTableMapper;

    @Override
    public List<DmSubTable> getDmSubTableListByTableId(Long tableId) {
        return lambdaQuery()
                .eq(DmSubTable::getTableId, tableId)
                .eq(DmSubTable::getDeleted,0)
                .orderByAsc(DmSubTable::getSort)
                .list();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSubTableListByTableId(Long tableId, List<DmSubTable> dmSubTableList) {
        // 先删除
        dmSubTableMapper.deleteByTableId(tableId);

        if (CollectionUtils.isNotEmpty(dmSubTableList)) {
            // 添加子表数据
            baseMapper.insertBatch(dmSubTableList);
        }
    }

}
