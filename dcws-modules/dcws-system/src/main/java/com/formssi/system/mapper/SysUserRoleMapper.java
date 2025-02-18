package com.formssi.system.mapper;

import com.formssi.system.domain.SysUserRole;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 用户与角色关联表 数据层
 *
 * @author Lion Li
 */
public interface SysUserRoleMapper extends BaseMapperPlus<SysUserRole, SysUserRole> {

    List<Long> selectUserIdsByRoleId(Long roleId);

}
