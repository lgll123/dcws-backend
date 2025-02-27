package com.formssi.workflow.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.workflow.domain.DcwsNormalTask;
import com.formssi.workflow.domain.vo.DcwsNormalTaskVo;
import org.apache.ibatis.annotations.Param;

public interface DcwsNormalTaskMapper extends BaseMapperPlus<DcwsNormalTask, DcwsNormalTaskVo> {

    /**
     * 获取待办信息
     *
     * @param page         分页
     * @param queryWrapper 条件
     * @return 结果
     */
    Page<DcwsNormalTaskVo> getTaskWaitByPage(@Param("page") Page<DcwsNormalTaskVo> page, @Param(Constants.WRAPPER) Wrapper<DcwsNormalTaskVo> queryWrapper);


    /**
     * 获取已办
     *
     * @param page         分页
     * @param queryWrapper 条件
     * @return 结果
     */
    Page<DcwsNormalTaskVo> getTaskFinishByPage(@Param("page") Page<DcwsNormalTaskVo> page, @Param(Constants.WRAPPER) Wrapper<DcwsNormalTaskVo> queryWrapper);

}
