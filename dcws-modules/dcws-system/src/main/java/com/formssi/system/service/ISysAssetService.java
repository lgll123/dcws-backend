package com.formssi.system.service;


import com.formssi.system.domain.AjaxResult;
import com.formssi.system.domain.vo.HrDeptVo;

import java.util.List;
import java.util.Map;

public interface ISysAssetService {

    List<Map<String, Object>> selectDeptList();
    void insertDeptFromOa(List<HrDeptVo> oaDeptList );

}
