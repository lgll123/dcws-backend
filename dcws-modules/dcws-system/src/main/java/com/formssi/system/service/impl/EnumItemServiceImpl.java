package com.formssi.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.common.core.utils.CopyUtil;
import com.formssi.system.domain.SysEnumInfo;
import com.formssi.system.domain.SysEnumItem;
import com.formssi.system.domain.dto.EnumItemDTO;
import com.formssi.system.enums.BooleanEnum;
import com.formssi.system.mapper.SysEnumItemMapper;
import com.formssi.system.service.IEnumItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/25 15:03
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EnumItemServiceImpl extends ServiceImpl<SysEnumItemMapper, SysEnumItem> implements IEnumItemService {

    @Override
    public List<EnumItemDTO> getListByEnumId(Long enumId) {
        List<SysEnumItem> itemList = getListById(enumId);
        return CopyUtil.copyList(itemList, EnumItemDTO::new);
    }

    @Override
    public void updateItems(SysEnumInfo sysEnumInfo, List<EnumItemDTO> items) {
        if (items != null) {
            for (EnumItemDTO item : items) {
                item.setEnumId(sysEnumInfo.getId());
                saveEnumItem(item);
            }
        }
    }

    /**
     * 保存枚举详情
     * @param itemDTO 枚举详情DTO
     */
    private void saveEnumItem(EnumItemDTO itemDTO) {
        SysEnumItem sysEnumItem = getByEnumIdAndName(itemDTO.getEnumId(), itemDTO.getName());
        if (sysEnumItem == null) {
            sysEnumItem = CopyUtil.copyBean(itemDTO, SysEnumItem::new);
            sysEnumItem.setIsDeleted(BooleanEnum.FALSE.getType());
            baseMapper.insert(sysEnumItem);
        } else {
            CopyUtil.copyPropertiesIgnoreNull(itemDTO, sysEnumItem);
            sysEnumItem.setIsDeleted(BooleanEnum.FALSE.getType());
            baseMapper.updateById(sysEnumItem);
        }
    }

    /**
     * 根据枚举id和名称获取枚举详情
     * @param enumId 枚举id
     * @param name 名称
     * @return 返回枚举详情
     */
    private SysEnumItem getByEnumIdAndName(Long enumId, String name) {
        return lambdaQuery().eq(SysEnumItem::getEnumId, enumId)
                .eq(SysEnumItem::getName, name)
                .eq(SysEnumItem::getIsDeleted, BooleanEnum.FALSE.getType())
                .one();
    }

    /**
     * 根据主键id获取枚举详情列表
     * @param enumId 枚举id
     * @return 返回枚举详情列表
     */
    private List<SysEnumItem> getListById(Long enumId) {
        return lambdaQuery().eq(SysEnumItem::getEnumId, enumId)
                .orderByAsc(SysEnumItem::getId)
                .list();
    }

}
