package com.formssi.workflow.externalsystem.assets.constant;


/**
 * 资产系统常量
 *
 * @author yqh
 */
public interface AssetsConstant {
    /**
     * 搜索条件
     */
    String SEARCH = "search";
    /**
     * 每页条数
     */
    String LIMIT = "limit";
    /**
     * 分页偏移量
     */
    String OFFSET = "offset";
    /**
     * 排序number
     */
    String ORDER_NUMBER = "order_number";
    /**
     * 排序字段值
     */
    String SORT ="sort";
    /**
     * 排序
     */
    String ORDER ="order";
    /**
     * 扩展
     */
    String EXPAND ="expand";

    /**
     * id
     */
    String ID = "id";

    /**
     * NAME
     */
    String NAME = "name";
    /**
     * 总计数量
     */
    String QTY = "qty";
    /**
     * 剩余数量
     */
    String REMAINING_QTY = "remaining_qty";
    /**
     * 已借出数量
     */
    String CHECKOUTS_COUNT = "checkouts_count";
    /**
     * 资产目录类型
     */
    String CATEGORY_TYPE = "category_type";
    /**
     * 资产目录类型ID
     */
    String CATEGORY_ID = "category_id";
    /**
     * 返回数据行数
     */
    String ROWS = "rows";
    /**
     * 返回数据总数
     */
    String TOTAL = "total";
    /**
     * 状态
     */
    String STATUS = "status";
    /**
     * 请求类型get
     */
    String REQUEST_TYPE_GET = "get";
    /**
     * 请求类型post
     */
    String REQUEST_TYPE_POST = "post";
    /**
     * 请求类型put
     */
    String REQUEST_TYPE_PUT = "put";
    /**
     * 资产状态 可申领
     */
    String STATUS_REQUESTABLE = "Requestable";
    /**
     * 资产目录类型 附属品-accessories
     */
    String CATEGORIES_ACCESSORIES = "accessories";
    /**
     * 资产目录类型 组件-components
     */
    String CATEGORIES_COMPONENTS = "components";
    /**
     * 资产目录类型 许可证-licenses
     */
    String CATEGORIES_LICENSES = "licenses";
    /**
     * 资产目录类型 消耗品-consumables
     */
    String CATEGORIES_CONSUMABLES = "consumables";
    /**
     * 资产目录类型 资产-hardware
     */
    String CATEGORIES_HARDWARE = "hardware";

}
