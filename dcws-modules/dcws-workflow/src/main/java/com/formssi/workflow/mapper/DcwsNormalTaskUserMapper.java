package com.formssi.workflow.mapper;

import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.workflow.domain.DcwsNormalTaskUser;
import com.formssi.workflow.domain.vo.DcwsNormalTaskUserVo;
import org.apache.ibatis.annotations.Param;

public interface DcwsNormalTaskUserMapper extends BaseMapperPlus<DcwsNormalTaskUser, DcwsNormalTaskUserVo> {

    int deleteByTaskId(@Param("taskId")Long taskId);

}
