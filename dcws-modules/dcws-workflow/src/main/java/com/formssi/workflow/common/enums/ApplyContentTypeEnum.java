package com.formssi.workflow.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 申请内容类型枚举
 *
 * @author yqh
 */
@Getter
@AllArgsConstructor
public enum ApplyContentTypeEnum {
    /**
     * 服务申请：1-需求(非物料类需求，如开通网络、申请VPN等)
     */
    SERVICE_1("1","需求", "需求(非物料类需求，如开通网络、申请VPN等)"),
    /**
     * 服务申请： 2-事件(故障排查或其它需IT支持事宜)
     */
    SERVICE_2("2","事件", "事件(故障排查或其它需IT支持事宜)"),
    /**
     * 服务申请： 3-设备维修
     */
    SERVICE_3("3", "设备维修 ","设备维修 "),
    /**
     * 资料申请： 4-收入证明
     */
    INFO_4("4", "收入证明","收入证明"),
    /**
     * 资料申请：5-营业执照
     */
    INFO_5("5", "营业执照","营业执照"),
    /**
     * 资料申请：6-其他类型
     */
    INFO_6("6", "其他类型","其他类型");

    private final String code;
    private final String name;
    private final String desc;
    public static ApplyContentTypeEnum of(String type) {
        if (type == null) {
            return null;
        }
        for (ApplyContentTypeEnum value : ApplyContentTypeEnum.values()) {
            if (Objects.equals(type, value.getCode())) {
                return value;
            }
        }
        return null;
    }
}
