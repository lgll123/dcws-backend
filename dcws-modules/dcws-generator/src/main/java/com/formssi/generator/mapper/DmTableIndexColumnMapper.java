package com.formssi.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.generator.domain.DmTableIndexColumn;

/**
 * 索引字段关系 数据层
 *
 * @author Shen Tao
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface DmTableIndexColumnMapper extends BaseMapperPlus<DmTableIndexColumn, Long> {

}
