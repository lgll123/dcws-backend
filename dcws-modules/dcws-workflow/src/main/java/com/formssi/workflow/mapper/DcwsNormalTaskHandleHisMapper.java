package com.formssi.workflow.mapper;

import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.workflow.domain.DcwsNormalTaskHandleHis;
import com.formssi.workflow.domain.vo.DcwsNormalTaskHandleHisVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DcwsNormalTaskHandleHisMapper extends BaseMapperPlus<DcwsNormalTaskHandleHis, DcwsNormalTaskHandleHisVo> {

    int updateByTaskId(@Param("comment")String comment, @Param("userId")String userId, @Param("taskId")Long taskId);

    int deleteByTaskId(@Param("taskId")Long taskId);

    List<DcwsNormalTaskHandleHisVo> getHistoryRecord(@Param("taskId")Long taskId);
}
