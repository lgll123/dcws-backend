package com.formssi.workflow.mapper;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.workflow.domain.DcwsApprove;
import com.formssi.workflow.domain.DcwsHis;
import com.formssi.workflow.domain.vo.DcwsHisVo;
import org.apache.ibatis.annotations.Param;

public interface DcwsHisMapper extends BaseMapperPlus<DcwsHis, DcwsHisVo> {

    int updateByTaskId(@Param(Constants.ENTITY) DcwsHis dcwsHis);
}
