package com.formssi.workflow.service;


import com.formssi.workflow.domain.bo.DcwsAssetsCheckOutBo;
import com.formssi.workflow.domain.vo.DcwsAssetsCheckOutVo;

/**
 * 物料申请借出记录Service接口
 *
 * @author yqh
 * @date 2025-02-13
 */
public interface IAssetsCheckOutRecordService {

    /**
     * 查询申请列表
     */
//    List<DcwsAssetsCheckOutVo> queryList(TaskNodeDataBo bo);

    /**
     * 新增申请
     */
    DcwsAssetsCheckOutVo insertByBo(DcwsAssetsCheckOutBo bo);
}
