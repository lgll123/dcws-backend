package com.formssi.demo.mapper;

import com.formssi.common.mybatis.annotation.DataColumn;
import com.formssi.common.mybatis.annotation.DataPermission;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.demo.domain.TestTree;
import com.formssi.demo.domain.vo.TestTreeVo;

/**
 * 测试树表Mapper接口
 *
 * @author Lion Li
 * @date 2021-07-26
 */
@DataPermission({
    @DataColumn(key = "deptName", value = "dept_id"),
    @DataColumn(key = "userName", value = "user_id")
})
public interface TestTreeMapper extends BaseMapperPlus<TestTree, TestTreeVo> {

}
