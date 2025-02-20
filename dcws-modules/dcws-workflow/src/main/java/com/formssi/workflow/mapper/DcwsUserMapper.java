package com.formssi.workflow.mapper;

import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.workflow.domain.DcwsUser;
import com.formssi.workflow.domain.vo.DcwsUserVo;
import org.apache.ibatis.annotations.Param;

public interface DcwsUserMapper extends BaseMapperPlus<DcwsUser, DcwsUserVo> {

    int deleteByTaskId(@Param("taskId")Long taskId);

}
