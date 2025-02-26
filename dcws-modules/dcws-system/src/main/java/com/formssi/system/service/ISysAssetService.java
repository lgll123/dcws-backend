package com.formssi.system.service;


import com.formssi.system.domain.vo.HrDeptVo;
import com.formssi.system.domain.vo.HrUserVo;

import java.util.List;
import java.util.Map;

public interface ISysAssetService {

    //查询部门列表
    List<Map<String, Object>> selectDeptList();
    //新增部门信息
    void insertDeptFromOa(List<HrDeptVo> oaDeptList );
    //查询用户列表
    List<Map<String, Object>> selectUserList();
    //新增用户信息
    void insertUserFromOa(List<HrUserVo> oaUserList );
    //更新用户信息
    void updateUserFromOa(List<HrUserVo> oaUserList );

}
