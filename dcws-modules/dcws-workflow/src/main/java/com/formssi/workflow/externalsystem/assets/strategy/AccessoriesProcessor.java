package com.formssi.workflow.externalsystem.assets.strategy;

import com.formssi.workflow.domain.bo.AssetsSystemBo;
import com.formssi.workflow.utils.TypeSafeUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

// accessories策略实现 附属品-accessories、组件-components、许可证-licenses、消耗品-consumables、资产-hardware
@Component("accessoriesProcessor")
public class AccessoriesProcessor implements AssetProcessor {
    @Override
    public void process(Map<String, Object> data, AssetsSystemBo bo) {
        bo.setManufacturerName(TypeSafeUtils.safeGetNestedString(data, "manufacturer", "name"));
        bo.setExpirationDate(TypeSafeUtils.safeGetNestedString(data, "expiration_date", "date"));
        bo.setProductKey((String)data.get("product_key"));
        bo.setCategoryName(TypeSafeUtils.safeGetNestedString(data, "category", "name"));
        bo.setPurchaseDate(TypeSafeUtils.safeGetNestedString(data, "purchase_date", "date"));
        bo.setLicenseEmail((String)data.get("license_email"));
        bo.setLicenseName((String)data.get("license_name"));
        bo.setPurchaseCost((String)data.get("purchase_cost"));
        bo.setLocationName(TypeSafeUtils.safeGetNestedString(data, "location", "name"));
        bo.setModelNo((String)data.get("model_number"));
        bo.setQty((Integer)data.get("qty"));
        bo.setRemainQty((Integer)data.get("remaining_qty"));
        bo.setCheckoutsCount((Integer)data.get("checkouts_count"));
    }
}