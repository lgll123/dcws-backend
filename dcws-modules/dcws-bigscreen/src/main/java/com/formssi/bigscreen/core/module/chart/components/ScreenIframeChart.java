package com.formssi.bigscreen.core.module.chart.components;

import com.formssi.bigscreen.core.constant.PageDesignConstant;
import com.formssi.bigscreen.core.module.chart.bean.Chart;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author hongyang
 * @version 1.0
 * @date 2023/3/13 16:44
 */
@Data
public class ScreenIframeChart extends Chart {

    @ApiModelProperty(notes = "外链地址")
    private String url;

    @ApiModelProperty(notes = "类型")
    private String type = PageDesignConstant.BigScreen.Type.IFRAME;

}
