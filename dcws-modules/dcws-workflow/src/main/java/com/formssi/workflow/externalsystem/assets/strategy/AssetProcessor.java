package com.formssi.workflow.externalsystem.assets.strategy;

import com.formssi.workflow.domain.bo.AssetsSystemBo;

import java.util.Map;

public interface AssetProcessor {
    void process(Map<String, Object> data, AssetsSystemBo bo);
}
