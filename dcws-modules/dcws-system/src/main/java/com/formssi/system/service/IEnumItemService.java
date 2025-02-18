package com.formssi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.system.domain.SysEnumInfo;
import com.formssi.system.domain.SysEnumItem;
import com.formssi.system.domain.dto.EnumItemDTO;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/25 15:02
 */
public interface IEnumItemService extends IService<SysEnumItem> {

    /**
     * 根据枚举id获取枚举详情DTO
     * @param enumId 枚举id
     * @return 返回枚举详情列表
     */
    List<EnumItemDTO> getListByEnumId(Long enumId);

    /**
     * 更新枚举详情
     * @param sysEnumInfo 枚举信息
     * @param items 枚举详情列表
     */
    void updateItems(SysEnumInfo sysEnumInfo, List<EnumItemDTO> items);

}
