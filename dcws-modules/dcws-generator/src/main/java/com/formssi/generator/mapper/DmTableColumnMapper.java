package com.formssi.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.generator.domain.DmTableColumn;

/**
 * 业务字段 数据层
 *
 * @author lizhangyu
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface DmTableColumnMapper extends BaseMapperPlus<DmTableColumn, DmTableColumn> {

}
