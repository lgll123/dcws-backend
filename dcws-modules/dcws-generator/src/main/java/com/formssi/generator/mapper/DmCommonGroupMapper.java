package com.formssi.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.generator.domain.DmCommonGroup;

import java.util.List;

/**
 * 常用字段分组 数据层
 *
 * @author Shen Tao
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface DmCommonGroupMapper extends BaseMapperPlus<DmCommonGroup, Long> {

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    List<DmCommonGroup> listAll();

}
