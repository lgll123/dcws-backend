package com.formssi.generator.service.impl;

import com.formssi.generator.config.ConnectConfig;
import com.formssi.generator.config.DbTypeConfig;
import com.formssi.generator.domain.DatasourceConfig;
import com.formssi.generator.mapper.DatasourceConfigMapper;
import com.formssi.generator.service.IDatasourceConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author tanghc
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DatasourceConfigServiceImpl implements IDatasourceConfigService {

    private final DatasourceConfigMapper datasourceConfigMapper;

    @Override
    public DatasourceConfig getById(int id) {
        return datasourceConfigMapper.getById(id);
    }

    @Override
    public List<DatasourceConfig> getByIdList(List<Integer> idList) {
        return datasourceConfigMapper.getByIdList(idList);
    }

    @Override
    public List<DatasourceConfig> listAll() {
        return datasourceConfigMapper.listAll();
    }

    @Override
    public void insert(DatasourceConfig datasourceConfig) {
        datasourceConfig.setIsDeleted(0);
        ConnectConfig connectConfig = DbTypeConfig.getInstance().getConnectConfig(datasourceConfig.getDbType());
        if (connectConfig != null) {
            datasourceConfig.setDriverClass(connectConfig.getDriver());
        }
        datasourceConfigMapper.insert(datasourceConfig);
    }

    @Override
    public void update(DatasourceConfig datasourceConfig) {
        datasourceConfigMapper.update(datasourceConfig);
    }

    @Override
    public void delete(DatasourceConfig datasourceConfig) {
        datasourceConfigMapper.delete(datasourceConfig);
    }

}
