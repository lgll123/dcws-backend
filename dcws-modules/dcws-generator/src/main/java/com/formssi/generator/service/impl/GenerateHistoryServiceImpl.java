package com.formssi.generator.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.formssi.generator.domain.GenerateHistory;
import com.formssi.generator.param.GeneratorHistoryParam;
import com.formssi.generator.param.GeneratorParam;
import com.formssi.generator.service.IGenerateHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.alibaba.fastjson.JSON;
import com.formssi.generator.mapper.GenerateHistoryMapper;

import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GenerateHistoryServiceImpl implements IGenerateHistoryService {

    private final GenerateHistoryMapper generateHistoryMapper;

    @Override
    public void saveHistory(GeneratorParam param) {
        String content = JSON.toJSONString(param);
        String md5 = SecureUtil.md5(content);
        GenerateHistory history = generateHistoryMapper.getByMd5(md5);
        if (history != null) {
            history.setGenerateTime(new Date());
            generateHistoryMapper.updateIgnoreNull(history);
            return;
        }
        GenerateHistory generateHistory = new GenerateHistory();
        generateHistory.setConfigContent(content);
        generateHistory.setMd5Value(md5);
        generateHistory.setGenerateTime(new Date());
        this.insertIgnoreNull(generateHistory);
    }

    @Override
    public void saveGenerateHistory(GeneratorHistoryParam historyParam) {
        String content = JSON.toJSONString(historyParam);
        String md5 = SecureUtil.md5(content);
        int version = generateHistoryMapper.queryMaxVersion();
        GenerateHistory generateHistory = new GenerateHistory();
        generateHistory.setConfigContent(content);
        generateHistory.setMd5Value(md5);
        generateHistory.setGenerateTime(new Date());
        generateHistory.setVersion(version + 1);
        this.insertIgnoreNull(generateHistory);
    }

    /**
     * 查询所有记录
     *
     * @return 返回集合，没有返回空List
     */
    @Override
    public List<GenerateHistory> listAll() {
    	return generateHistoryMapper.listAll();
    }


    /**
     * 根据主键查询
     *
     * @param id 主键
     * @return 返回记录，没有返回null
     */
    @Override
    public GenerateHistory getById(Integer id) {
    	return generateHistoryMapper.getById(id);
    }

    /**
     * 新增，插入所有字段
     *
     * @param generateHistory 新增的记录
     * @return 返回影响行数
     */
    @Override
    public int insert(GenerateHistory generateHistory) {
    	return generateHistoryMapper.insert(generateHistory);
    }

    /**
     * 新增，忽略null字段
     *
     * @param generateHistory 新增的记录
     * @return 返回影响行数
     */
    @Override
    public int insertIgnoreNull(GenerateHistory generateHistory) {
    	return generateHistoryMapper.insertIgnoreNull(generateHistory);
    }

    /**
     * 修改，修改所有字段
     *
     * @param generateHistory 修改的记录
     * @return 返回影响行数
     */
    @Override
    public int update(GenerateHistory generateHistory) {
    	return generateHistoryMapper.update(generateHistory);
    }

    /**
     * 修改，忽略null字段
     *
     * @param generateHistory 修改的记录
     * @return 返回影响行数
     */
    @Override
    public int updateIgnoreNull(GenerateHistory generateHistory) {
    	return generateHistoryMapper.updateIgnoreNull(generateHistory);
    }

    /**
     * 删除记录
     *
     * @param generateHistory 待删除的记录
     * @return 返回影响行数
     */
    @Override
    public int delete(GenerateHistory generateHistory) {
    	return generateHistoryMapper.delete(generateHistory);
    }

    public List<GenerateHistory> getListByVersionList(List<Integer> versionList) {
        return generateHistoryMapper.getListByVersionList(versionList);
    }

}
