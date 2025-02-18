package com.formssi.generator.service.impl;

import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.generator.constant.DictConstants;
import com.formssi.generator.domain.DmDict;
import com.formssi.generator.mapper.DmDictMapper;
import com.formssi.generator.model.request.DmDictQueryRequest;
import com.formssi.generator.model.request.DmDictRequest;
import com.formssi.generator.model.vo.DmDictVO;
import com.formssi.generator.service.IDmDictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典 服务层实现
 *
 * @author Shen Tao
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DmDictServiceImpl extends ServiceImpl<DmDictMapper, DmDict> implements IDmDictService {

    private final DmDictMapper dmDictMapper;

    @Override
    public TableDataInfo<DmDictVO> selectPage(DmDictQueryRequest request, PageQuery pageQuery) {
        LambdaQueryWrapper<DmDict> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(DmDict::getStatus, DictConstants.OK.getCode());
        queryWrapper.and(StringUtils.isNotEmpty(request.getKeyword()), w -> w.like(DmDict::getColumnName, request.getKeyword()).or().like(DmDict::getColumnComment, request.getKeyword()));
        queryWrapper.like(StringUtils.isNotEmpty(request.getColumnName()), DmDict::getColumnName, request.getColumnName());
        queryWrapper.like(StringUtils.isNotEmpty(request.getColumnComment()), DmDict::getColumnComment, request.getColumnComment());
        Page<DmDict> dictPage = baseMapper.selectPage(pageQuery.build(), queryWrapper);
        List<DmDictVO> result = new ArrayList<>();
        for (DmDict dict : dictPage.getRecords()) {
            DmDictVO dictVO = new DmDictVO();
            dictVO.setDictId(dict.getDictId());
            dictVO.setColumnName(dict.getColumnName());
            dictVO.setColumnComment(dict.getColumnComment());
            dictVO.setColumnType(dict.getColumnType());
            dictVO.setLength(dict.getLength());
            dictVO.setDefaultValue(dict.getDefaultValue());
            dictVO.setDescription(dict.getDescription());
            dictVO.setJavaType(dict.getJavaType());
            dictVO.setVersion(dict.getVersion());
            result.add(dictVO);
        }
        TableDataInfo<DmDictVO> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        rspData.setRows(result);
        rspData.setTotal(dictPage.getTotal());
        return rspData;
    }

    @Override
    public Long addDict(DmDictRequest request) {
        DmDict dict = new DmDict();
        dict.setColumnName(request.getColumnName());
        dict.setColumnComment(request.getColumnComment());
        dict.setColumnType(request.getColumnType());
        dict.setLength(request.getLength());
        dict.setDefaultValue(request.getDefaultValue());
        dict.setDescription(request.getDescription());
        dict.setJavaType(request.getJavaType());
        dict.setStatus(DictConstants.OK.getCode());
        dict.setVersion(request.getVersion());
        dmDictMapper.insert(dict);
        return dict.getDictId();
    }

    @Override
    public void updateDict(Long dictId, DmDictRequest request) {
        DmDict dict = new DmDict();
        dict.setDictId(dictId);
        dict.setColumnName(request.getColumnName());
        dict.setColumnComment(request.getColumnComment());
        dict.setColumnType(request.getColumnType());
        dict.setLength(request.getLength());
        dict.setDefaultValue(request.getDefaultValue());
        dict.setDescription(request.getDescription());
        dict.setJavaType(request.getJavaType());
        dict.setVersion(request.getVersion());
        dmDictMapper.updateById(dict);
    }

    @Override
    public void deleteDict(Long dictId) {
        DmDict dict = new DmDict();
        dict.setDictId(dictId);
        dict.setStatus(DictConstants.DELETED.getCode());
        dmDictMapper.updateById(dict);
    }

}
