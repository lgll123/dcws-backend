package com.formssi.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.generator.domain.DmSubTable;

import java.util.List;

/**
 * 业务 服务层
 *
 * @author lizhangyu
 */
public interface IDmSubTableService extends IService<DmSubTable> {

    /**
     * 根据主表id获取子表列表
     * @param tableId 主表id
     * @return 返回子表列表
     */
    List<DmSubTable> getDmSubTableListByTableId(Long tableId);

    /**
     * 根据主表id更新对应的子表信息
     * @param tableId 表id
     * @param dmSubTableList 子表列表
     */
    void updateSubTableListByTableId(Long tableId, List<DmSubTable> dmSubTableList);
}
