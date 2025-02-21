package com.formssi.workflow.mapper;

import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.workflow.domain.DcwsProject;
import com.formssi.workflow.domain.vo.DcwsProjectVo;
import org.apache.ibatis.annotations.Param;

public interface DcwsProjectMapper extends BaseMapperPlus<DcwsProject, DcwsProjectVo> {

    int updateByProjectId(@Param("projectStatus")String projectStatus, @Param("projectId")Long projectId);

}
