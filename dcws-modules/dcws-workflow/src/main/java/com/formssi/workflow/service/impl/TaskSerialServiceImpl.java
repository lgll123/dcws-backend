package com.formssi.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.workflow.domain.DcwsNormalTask;
import com.formssi.workflow.domain.DcwsNormalTaskHandleHis;
import com.formssi.workflow.domain.DcwsTaskSerialNumber;
import com.formssi.workflow.domain.bo.DcwsTaskSerialNumberBo;
import com.formssi.workflow.domain.vo.DcwsTaskSerialNumberVo;
import com.formssi.workflow.mapper.DcwsTaskSerialNumberMapper;
import com.formssi.workflow.service.TaskSerialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Date;
import java.util.List;


@RequiredArgsConstructor
@Service
@Slf4j
public class TaskSerialServiceImpl implements TaskSerialService {

    private final DcwsTaskSerialNumberMapper taskSerialNumberMapper;

    @Override
    public String getTaskSerial(String taskType,String taskDate) {
        LambdaQueryWrapper<DcwsTaskSerialNumber> lqw = Wrappers.lambdaQuery();
        lqw.eq(DcwsTaskSerialNumber::getTaskType, taskType);
        lqw.eq(DcwsTaskSerialNumber::getTaskDate, taskDate);
        List<DcwsTaskSerialNumberVo> list = taskSerialNumberMapper.selectVoList(lqw);
        if (ObjectUtils.isEmpty(list)){
            DcwsTaskSerialNumberBo bo = new DcwsTaskSerialNumberBo();
            bo.setTaskType(taskType);
            bo.setTaskDate(taskDate);
            DcwsTaskSerialNumber add = MapstructUtils.convert(bo, DcwsTaskSerialNumber.class);
            add.setSystemName("DCWS");
            add.setTaskNum(1L);
            taskSerialNumberMapper.insert(add);
            return "DCWS" + StringUtils.padl(Integer.parseInt(taskType),3)+ taskDate + StringUtils.padl(1,6);
        }else {
            DcwsTaskSerialNumberVo taskSerialNumberVo = list.get(0);
            taskSerialNumberMapper.update(null, new LambdaUpdateWrapper<DcwsTaskSerialNumber>()
                    .set(DcwsTaskSerialNumber::getTaskNum, taskSerialNumberVo.getTaskNum() + 1)
                    .eq(DcwsTaskSerialNumber::getTaskType, taskType)
                    .eq(DcwsTaskSerialNumber::getTaskDate, taskDate));
            return "DCWS" + StringUtils.padl(Integer.parseInt(taskType),3)+ taskDate + StringUtils.padl(taskSerialNumberVo.getTaskNum()+1,6);
        }
    }
}
