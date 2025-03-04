package com.formssi.workflow.service;

import com.formssi.workflow.domain.bo.DcwsTaskSerialNumberBo;
import com.formssi.workflow.domain.vo.DcwsTaskSerialNumberVo;

public interface TaskSerialService {

    /**
     * 获取序号
     */
    String getTaskSerial(String taskType,String taskDate);


}
