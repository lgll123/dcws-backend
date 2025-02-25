package com.formssi.workflow.externalsystem.assets.strategy;

import com.formssi.workflow.domain.bo.AssetsSystemBo;
import com.formssi.workflow.utils.TypeSafeUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

// Hardware策略实现 附属品-accessories、组件-components、许可证-licenses、消耗品-consumables、资产-hardware
@Component("hardwareProcessor")
public class HardwareProcessor implements AssetProcessor {
    @Override
    public void process(Map<String, Object> data, AssetsSystemBo bo) {
        bo.setCategoryName(TypeSafeUtils.safeGetNestedString(data, "category", "name"));
        bo.setModelNo(TypeSafeUtils.safeGetNestedString(data, "model", "name"));
        bo.setSerial((String)data.get("serial"));
        bo.setAssetTag((String)data.get("asset_tag"));
        bo.setAssetStatus(TypeSafeUtils.safeGetNestedString(data, "status_label", "status_type"));
        bo.setAssetStatusId(((Map<String,Object>)data.get("status_label")).get("id").toString());
    }
}
