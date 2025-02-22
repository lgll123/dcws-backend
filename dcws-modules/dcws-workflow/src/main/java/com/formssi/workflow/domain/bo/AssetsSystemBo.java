package com.formssi.workflow.domain.bo;

import lombok.Data;

/**
 * 物料申请业务对象 assets
 */
@Data
public class AssetsSystemBo{


    /**
     * 物料id
     */
//    @NotNull(message = "物料id为空")
    private Integer id;
    /**
     * 物料名称/资产名称
     */
    private String name;
    /**
     * 物料规格型号/资产型号
     */
    private String modelNo;

    /**
     * 物料剩余库存
     */
    private Integer remainQty;

    /**
     * 物料已占用数量
     * 已借出
     */
    private Integer checkoutsCount;

    /**
     * 物料总库存
     */
    private Integer qty;
    /**
     * 资产标签
     */
    private String assetTag;

    /**
     * 资产状态
     */
    private String assetStatus;



    /**
     * 分页偏移量
     */
    private Integer offset;

    /**
     * 每页条数
     */
    private Integer limit;
}
