package com.formssi.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.formssi.system.domain.SysDept;
import com.formssi.common.mybatis.annotation.DataColumn;
import com.formssi.common.mybatis.annotation.DataPermission;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.system.domain.vo.HrDeptVo;
import com.formssi.system.domain.vo.SysDeptVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 部门管理 数据层
 *
 * @author Lion Li
 */
public interface SysDeptMapper extends BaseMapperPlus<SysDept, SysDeptVo> {

    /**
     * 查询部门管理数据
     *
     * @param queryWrapper 查询条件
     * @return 部门信息集合
     */
    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id")
    })
    List<SysDeptVo> selectDeptList(@Param(Constants.WRAPPER) Wrapper<SysDept> queryWrapper);

    @DataPermission({
        @DataColumn(key = "deptName", value = "dept_id")
    })
    long countDeptById(Long deptId);


    //查询所有部门数据
    List<HrDeptVo> selectAllDeptList();
    //新增部门--人事系统数据
    int insertDeptFromHr(@Param("list")List<HrDeptVo> hrDeptList);
    //逻辑删除人事系统不存在的部门
    int deleteByIdFromHr(@Param("list") List<String> deptIdList);
    //更新部门
    int updateDeptFromHr(@Param("list")List<HrDeptVo> hrDeptList);

    /**
     * 根据角色ID查询部门树信息
     *
     * @param roleId            角色ID
     * @param deptCheckStrictly 部门树选择项是否关联显示
     * @return 选中部门列表
     */
    List<Long> selectDeptListByRoleId(@Param("roleId") Long roleId, @Param("deptCheckStrictly") boolean deptCheckStrictly);

}
