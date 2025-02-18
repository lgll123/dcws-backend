package com.formssi.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.formssi.system.domain.SysProp;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author tanghc
 */
public interface SysPropMapper extends BaseMapper<SysProp> {

    int saveProps(@Param("items") List<SysProp> items);

}
