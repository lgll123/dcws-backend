package com.formssi.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.CopyUtil;
import com.formssi.system.domain.SysEnumInfo;
import com.formssi.system.domain.dto.EnumInfoDTO;
import com.formssi.system.domain.dto.EnumItemDTO;
import com.formssi.system.enums.BooleanEnum;
import com.formssi.system.mapper.SysEnumInfoMapper;
import com.formssi.system.service.IEnumItemService;
import com.formssi.system.service.IEnumInfoService;
import com.formssi.system.util.DataIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author lizhangyu
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class EnumInfoServiceImpl extends ServiceImpl<SysEnumInfoMapper, SysEnumInfo> implements IEnumInfoService {

    private final IEnumItemService enumItemService;

    /**
     * 根据模块id获取枚举信息列表
     * @param moduleId 模块id
     * @return 返回枚举信息DTO列表
     */
    public List<EnumInfoDTO> listAll(long moduleId) {
        List<SysEnumInfo> sysEnumInfoList = getListByModuleId(moduleId);
        List<EnumInfoDTO> enumInfoDTOS = CopyUtil.copyList(sysEnumInfoList, EnumInfoDTO::new);
        for (EnumInfoDTO enumInfoDTO : enumInfoDTOS) {
            Long enumId = enumInfoDTO.getId();
            List<EnumItemDTO> items = enumItemService.getListByEnumId(enumId);
            enumInfoDTO.setItems(items);
        }
        return enumInfoDTOS;
    }

    /**
     * 根据模块id获取枚举信息
     * @param moduleId 模块id
     * @return 返回枚举信息DTO列表
     */
    public List<EnumInfoDTO> listBase(long moduleId) {
        List<SysEnumInfo> sysEnumInfoList = getListByModuleId(moduleId);
        return CopyUtil.copyList(sysEnumInfoList, EnumInfoDTO::new);
    }

    /**
     * 根据模块id获取枚举信息列表
     * @param moduleId 模块id
     * @return 返回枚举信息列表
     */
    private List<SysEnumInfo> getListByModuleId(Long moduleId) {
        return lambdaQuery()
                .eq(SysEnumInfo::getModuleId, moduleId)
                .eq(SysEnumInfo::getIsDeleted, BooleanEnum.FALSE.getType())
                .list();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public SysEnumInfo saveEnumInfo(EnumInfoDTO enumInfoDTO) {
        String dataId = buildDataId(enumInfoDTO.getModuleId(), enumInfoDTO.getName());
        SysEnumInfo sysEnumInfo = getByDataId(dataId);
        if (sysEnumInfo == null) {
            sysEnumInfo = CopyUtil.copyBean(enumInfoDTO, SysEnumInfo::new);
            sysEnumInfo.setDataId(dataId);
            baseMapper.insert(sysEnumInfo);
        } else {
            sysEnumInfo.setDescription(enumInfoDTO.getDescription());
            baseMapper.updateById(sysEnumInfo);
        }
        List<EnumItemDTO> items = enumInfoDTO.getItems();
        enumItemService.updateItems(sysEnumInfo, items);
        return sysEnumInfo;
    }

    /**
     * 根据文档id获取枚举信息
     * @param dataId 文档id
     * @return 返回枚举信息
     */
    private SysEnumInfo getByDataId(String dataId) {
        return lambdaQuery().eq(SysEnumInfo::getDataId, dataId)
                .eq(SysEnumInfo::getIsDeleted, BooleanEnum.FALSE.getType())
                .one();
    }

//    private void updateItems(EnumInfo enumInfo, List<EnumItemDTO> items) {
//        if (items != null) {
//            for (EnumItemDTO item : items) {
//                item.setEnumId(enumInfo.getId());
//                saveEnumItem(item);
//            }
//        }
//    }

    /**
     * 添加枚举信息
     * @param enumInfoDTO 枚举信息DTO
     * @return 返回枚举信息
     */
    public SysEnumInfo addEnumInfo(EnumInfoDTO enumInfoDTO) {
        this.checkInfoExist(enumInfoDTO);
        String dataId = DataIdUtil.getEnumInfoDataId(enumInfoDTO.getModuleId(), enumInfoDTO.getName());
        SysEnumInfo sysEnumInfo = CopyUtil.copyBean(enumInfoDTO, SysEnumInfo::new);
        sysEnumInfo.setDataId(dataId);
        baseMapper.insert(sysEnumInfo);
        return sysEnumInfo;
    }

    /**
     * 更新枚举信息
     * @param enumInfoDTO 枚举信息DTO
     * @return 返回枚举信息
     */
    public SysEnumInfo updateEnumInfo(EnumInfoDTO enumInfoDTO) {
        this.checkInfoExist(enumInfoDTO);
        SysEnumInfo sysEnumInfo = CopyUtil.copyBean(enumInfoDTO, SysEnumInfo::new);
        sysEnumInfo.setDataId(buildDataId(enumInfoDTO.getModuleId(), enumInfoDTO.getName()));
        baseMapper.updateById(sysEnumInfo);
        return sysEnumInfo;
    }

//    public List<EnumItemDTO> listItems(long enumId) {
//        Query query = enumItemService.query()
//                .eq(EnumItem::getEnumId, enumId)
//                .orderBy(EnumItem::getId, Sort.ASC);
//        List<EnumItem> itemList = enumItemService.list(query);
//        return CopyUtil.copyList(itemList, EnumItemDTO::new);
//    }

    /**
     * 构建文档id
     * @param moduleId 模块id
     * @param name 名称
     * @return 返回文档id
     */
    private String buildDataId(long moduleId, String name) {
        return DataIdUtil.getEnumInfoDataId(moduleId, name);
    }

//    private void saveEnumItem(EnumItemDTO itemDTO) {
//        EnumItem enumItem = enumItemService.getByEnumIdAndName(itemDTO.getEnumId(), itemDTO.getName());
//        if (enumItem == null) {
//            enumItem = CopyUtil.copyBean(itemDTO, EnumItem::new);
//            enumItem.setIsDeleted(Booleans.FALSE);
//            enumItemService.save(enumItem);
//        } else {
//            CopyUtil.copyPropertiesIgnoreNull(itemDTO, enumItem);
//            enumItem.setIsDeleted(Booleans.FALSE);
//            enumItemService.update(enumItem);
//        }
//    }

//    public EnumItem addEnumItem(EnumItemDTO itemDTO) {
//        this.checkItemExist(itemDTO);
//        EnumItem enumItem = CopyUtil.copyBean(itemDTO, EnumItem::new);
//        enumItemService.save(enumItem);
//        return enumItem;
//    }

//    public void addEnumItems(List<EnumItem> items) {
//        if (CollectionUtils.isEmpty(items)) {
//            return;
//        }
//        enumItemService.saveBatch(items);
//    }

    /**
     * 坚持枚举信息是否存在
     * @param enumInfoDTO 枚举信息DTO
     */
    private void checkInfoExist(EnumInfoDTO enumInfoDTO) {
        SysEnumInfo sysEnumInfo = getByModuleIdAndName(enumInfoDTO.getModuleId(), enumInfoDTO.getName());
        if (sysEnumInfo != null) {
            if (sysEnumInfo.getId() == null || !sysEnumInfo.getId().equals(enumInfoDTO.getId())) {
                throw new ServiceException(enumInfoDTO.getName() + "已存在");
            }
        }
    }

    /**
     * 根据模块id和枚举名称获取枚举信息
     * @param moduleId 模块id
     * @param name 名称
     * @return 返回枚举信息
     */
    private SysEnumInfo getByModuleIdAndName(Long moduleId, String name) {
        return lambdaQuery() .eq(SysEnumInfo::getModuleId, moduleId)
                .eq(SysEnumInfo::getName, name)
                .one();
    }

//    private void checkItemExist(EnumItemDTO itemDTO) {
//        EnumItem enumItem = enumItemService.getByEnumIdAndName(itemDTO.getEnumId(), itemDTO.getName());
//        if (enumItem != null) {
//            if (enumItem.getId() == null || !enumItem.getId().equals(itemDTO.getId())) {
//                throw new BizException(itemDTO.getName() + "已存在");
//            }
//        }
//    }

//    public EnumItem updateItem(EnumItemDTO itemDTO) {
//        this.checkItemExist(itemDTO);
//        EnumItem enumItem = enumItemService.getById(itemDTO.getId());
//        CopyUtil.copyPropertiesIgnoreNull(itemDTO, enumItem);
//        enumItemService.update(enumItem);
//        return enumItem;
//    }

    /**
     * 根据主键删除
     * @param id 主键id
     */
    public void deleteEnumInfo(long id) {
        lambdaUpdate().eq(SysEnumInfo::getId, id)
                .set(SysEnumInfo::getIsDeleted, BooleanEnum.TRUE.getType())
                .update();
    }

//    public void deleteEnumItem(long id) {
//        EnumItem item = enumItemService.getById(id);
//        enumItemService.getMapper().forceDelete(item);
//    }

}
