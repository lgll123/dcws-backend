package com.formssi.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.generator.domain.DmLayout;

/**
 * ER图设计 数据层
 *
 * @author Shen Tao
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface DmLayoutMapper extends BaseMapperPlus<DmLayout, Long> {

}
