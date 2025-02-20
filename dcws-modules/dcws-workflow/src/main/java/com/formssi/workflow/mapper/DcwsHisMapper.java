package com.formssi.workflow.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.system.domain.SysUser;
import com.formssi.system.domain.vo.SysUserVo;
import com.formssi.workflow.domain.DcwsApprove;
import com.formssi.workflow.domain.DcwsHis;
import com.formssi.workflow.domain.vo.DcwsHisVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DcwsHisMapper extends BaseMapperPlus<DcwsHis, DcwsHisVo> {

    int updateByTaskId(@Param("comment")String comment,@Param("userId")String userId,@Param("taskId")Long taskId);

    int deleteByTaskId(@Param("taskId")Long taskId);

    List<DcwsHisVo> getHistoryRecord(@Param("taskId")Long taskId);
}
