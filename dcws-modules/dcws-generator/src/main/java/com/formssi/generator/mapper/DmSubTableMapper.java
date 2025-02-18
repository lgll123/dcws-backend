package com.formssi.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.generator.domain.DmSubTable;

/**
 * 业务 数据层
 *
 * @author lizhangyu
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface DmSubTableMapper extends BaseMapperPlus<DmSubTable, DmSubTable> {

    /**
     * 根据表id删除数据
     * @param tableId 表id
     */
    void deleteByTableId(Long tableId);
}
