package com.formssi.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.system.domain.SysEnumInfo;
import com.formssi.system.domain.dto.EnumInfoDTO;

/**
 * @author lizhangyu
 * @version 1.0
 * @date 2024/11/25 11:30
 */
public interface IEnumInfoService extends IService<SysEnumInfo> {

    /**
     * 保存枚举信息
     * @param enumInfoDTO 枚举信息DTO
     * @return 返回枚举信息
     */
    SysEnumInfo saveEnumInfo(EnumInfoDTO enumInfoDTO);

}
