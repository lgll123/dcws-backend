package com.formssi.workflow.service.strategy;

import java.util.Map;

/**
 * dcws申请单生成PDF数据处理接口策略
 *
 * @author yqh
 */
public interface DcwsApplyFilePDFCreateStrategy<T> {
    Map<String, Object> process(String templateName,T taskNodeDataVo);

}
