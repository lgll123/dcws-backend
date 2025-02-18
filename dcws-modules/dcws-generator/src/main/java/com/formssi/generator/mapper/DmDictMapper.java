package com.formssi.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.generator.domain.DmCommonColumn;
import com.formssi.generator.domain.DmDict;

/**
 * 数据字典 数据层
 *
 * @author Shen Tao
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface DmDictMapper extends BaseMapperPlus<DmDict, Long> {

}
