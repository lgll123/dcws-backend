package com.formssi.workflow.service;

import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.workflow.domain.bo.DcwsProcessInstanceBo;
import com.formssi.workflow.domain.vo.DcwsActHistoryInfoVo;
import com.formssi.workflow.domain.vo.DcwsProcessInstanceVo;

import java.util.List;

/**
 * 流程实例 服务层
 *
 * @author may
 */
public interface DcwsIActProcessInstanceService {

    /**
     * 获取审批记录
     *
     * @param businessKey 业务id
     * @return 结果
     */
    List<DcwsActHistoryInfoVo> getHistoryRecord(String businessKey);

    /**
     * 分页查询当前登录人单据
     *
     * @param processInstanceBo 参数
     * @param pageQuery         分页
     * @return 结果
     */
    TableDataInfo<DcwsProcessInstanceVo> getPageByCurrent(DcwsProcessInstanceBo processInstanceBo, PageQuery pageQuery);

}
