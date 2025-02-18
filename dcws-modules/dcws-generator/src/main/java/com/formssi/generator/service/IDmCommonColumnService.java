package com.formssi.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.generator.domain.DmCommonColumn;

import java.util.List;

/**
 * 常用字段分组 服务层
 *
 * @author Shen Tao
 */
public interface IDmCommonColumnService extends IService<DmCommonColumn> {

    List<DmCommonColumn> listByCommonGroupId(Long commonGroupId);

}
