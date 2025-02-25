package com.formssi.workflow.service.impl;

import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.workflow.domain.DcwsAssetsCheckOut;
import com.formssi.workflow.domain.bo.DcwsAssetsCheckOutBo;
import com.formssi.workflow.domain.vo.DcwsAssetsCheckOutVo;
import com.formssi.workflow.mapper.DcwsAssetsCheckOutMapper;
import com.formssi.workflow.service.IAssetsCheckOutRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class AssetsCheckOutRecordServiceImpl implements IAssetsCheckOutRecordService {
    private final DcwsAssetsCheckOutMapper dcwsAssetsCheckOutMapper;
    @Override
    public DcwsAssetsCheckOutVo insertByBo(DcwsAssetsCheckOutBo bo) {
        DcwsAssetsCheckOut add = MapstructUtils.convert(bo, DcwsAssetsCheckOut.class);
        boolean flag = dcwsAssetsCheckOutMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return MapstructUtils.convert(add, DcwsAssetsCheckOutVo.class);
    }
}
