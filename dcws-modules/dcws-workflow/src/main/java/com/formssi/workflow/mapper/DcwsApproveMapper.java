package com.formssi.workflow.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.workflow.domain.DcwsApprove;
import com.formssi.workflow.domain.vo.DcwsApproveVo;
import com.formssi.workflow.domain.vo.TaskVo;
import org.apache.ibatis.annotations.Param;

public interface DcwsApproveMapper extends BaseMapperPlus<DcwsApprove, DcwsApproveVo> {

    int updateByTaskId(@Param("status")String status,@Param("userId")String userId,@Param("taskId")Long taskId);

    int deleteByTaskId(@Param("taskId")Long taskId);

    /**
     * 获取待办信息
     *
     * @param page         分页
     * @param queryWrapper 条件
     * @return 结果
     */
    Page<DcwsApproveVo> getTaskWaitByPage(@Param("page") Page<DcwsApproveVo> page, @Param(Constants.WRAPPER) Wrapper<DcwsApproveVo> queryWrapper);


    /**
     * 获取已办
     *
     * @param page         分页
     * @param queryWrapper 条件
     * @return 结果
     */
    Page<DcwsApproveVo> getTaskFinishByPage(@Param("page") Page<DcwsApproveVo> page, @Param(Constants.WRAPPER) Wrapper<DcwsApproveVo> queryWrapper);


}
