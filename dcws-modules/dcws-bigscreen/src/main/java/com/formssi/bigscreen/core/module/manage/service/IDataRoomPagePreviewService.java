package com.formssi.bigscreen.core.module.manage.service;

import com.formssi.common.service.ISuperService;
import com.formssi.bigscreen.core.module.basic.entity.PagePreviewEntity;
import com.formssi.bigscreen.core.module.manage.dto.DataRoomPageDTO;

/**
 * @author hongyang
 * @version 1.0
 * @date 2023/9/13 10:10
 */
public interface IDataRoomPagePreviewService extends ISuperService<PagePreviewEntity> {

    String PREVIEW_KEY = "preview";

    /**
     * 保存大屏预览数据
     * @param bigScreenPageDTO
     * @return
     */
    String add(DataRoomPageDTO bigScreenPageDTO);


    /**
     * 根据code获取大屏预览数据
     * @param code
     * @return
     */
    PagePreviewEntity getByCode(String code);

    /**
     * 清理预览数据
     */
    void clear();

}
