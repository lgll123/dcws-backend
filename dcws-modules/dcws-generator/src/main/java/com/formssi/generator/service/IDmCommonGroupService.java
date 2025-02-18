package com.formssi.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.generator.domain.DmCommonGroup;
import com.formssi.generator.model.vo.DmCommonGroupVO;

import java.util.List;

/**
 * 常用字段分组 服务层
 *
 * @author Shen Tao
 */
public interface IDmCommonGroupService extends IService<DmCommonGroup> {

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    List<DmCommonGroupVO> listAll();

}
