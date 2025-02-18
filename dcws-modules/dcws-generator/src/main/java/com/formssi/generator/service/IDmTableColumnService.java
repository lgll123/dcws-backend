package com.formssi.generator.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.generator.domain.DmTable;
import com.formssi.generator.domain.DmTableColumn;
import com.formssi.generator.domain.dto.GeneratorCompareDTO;
import com.formssi.generator.sql.CodeFile;
import com.formssi.generator.sql.TableDefinition;

import java.util.List;
import java.util.Map;

/**
 * 业务 服务层
 *
 * @author Lion Li
 */
public interface IDmTableColumnService extends IService<DmTableColumn> {

    /**
     * 查询业务字段列表
     *
     * @param tableId 业务字段编号
     * @return 业务字段集合
     */
    List<DmTableColumn> selectGenTableColumnListByTableId(Long tableId);


}
