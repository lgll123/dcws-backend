package com.formssi.workflow.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.mybatis.core.mapper.BaseMapperPlus;
import com.formssi.workflow.domain.vo.DcwsTaskVo;
import com.formssi.workflow.domain.vo.TaskVo;
import org.apache.ibatis.annotations.Param;


/**
 * 任务信息Mapper接口
 *
 * @author may
 * @date 2024-03-02
 */
@InterceptorIgnore(tenantLine = "true")
public interface DcwsActTaskMapper extends BaseMapperPlus<DcwsTaskVo, DcwsTaskVo> {
    /**
     * 获取待办信息
     *
     * @param page         分页
     * @param queryWrapper 条件
     * @return 结果
     */
    Page<DcwsTaskVo> getTaskWaitByPage(@Param("page") Page<DcwsTaskVo> page, @Param(Constants.WRAPPER) Wrapper<DcwsTaskVo> queryWrapper);

    /**
     * 获取已办
     *
     * @param page         分页
     * @param queryWrapper 条件
     * @return 结果
     */
    Page<DcwsTaskVo> getTaskFinishByPage(@Param("page") Page<DcwsTaskVo> page, @Param(Constants.WRAPPER) Wrapper<DcwsTaskVo> queryWrapper);

    /**
     * 查询当前用户的抄送
     *
     * @param page         分页
     * @param queryWrapper 条件
     * @return 结果
     */
    Page<DcwsTaskVo> getTaskCopyByPage(@Param("page") Page<TaskVo> page, @Param(Constants.WRAPPER) QueryWrapper<DcwsTaskVo> queryWrapper);
}
