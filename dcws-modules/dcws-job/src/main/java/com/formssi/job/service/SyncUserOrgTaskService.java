package com.formssi.job.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.system.domain.vo.*;
import com.formssi.system.mapper.SysRoleMapper;
import com.formssi.system.service.ISysAssetService;
import com.formssi.system.service.ISysDeptService;
import com.formssi.system.service.ISysHrService;
import com.formssi.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SyncUserOrgTaskService {

    private final ISysHrService hrService;

    private final ISysAssetService assetService;

    private final ISysUserService userService;

    private final ISysDeptService deptService;

    private final SysRoleMapper roleMapper;


    /**
     * 同步用户信息：人事系统->OA系统
     * @throws JsonProcessingException
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncUserInfo() throws JsonProcessingException {
        // 1. 调用第三方API获取用户信息
        String data = (String)hrService.selectUserList().getData();
        log.info("人事系统用户信息: {}", data);
        // 2. 解析响应的json数据
        ObjectMapper mapper = new ObjectMapper();
        HrResultVo hrResultVo = mapper.readValue(data, new TypeReference<HrResultVo<HrUserVo>>() {});
        // 3. 处理同步逻辑
        if (hrResultVo.getCode() == 200 && !CollectionUtils.isEmpty(hrResultVo.getData())) {
            //人事系统用户信息
            List<HrUserVo> hrUserList = hrResultVo.getData();
            List<String> hrUserIdList = hrUserList.stream().map(HrUserVo::getHrUserId).collect(Collectors.toList());
            //OA系统用户信息（要排除掉系統本身的用戶）
             List<HrUserVo> oaUserList = userService.selectAllUserList();
            List<String> oaUserIdList = oaUserList.stream().map(HrUserVo::getHrUserId).collect(Collectors.toList());

            int insertNum = 0;
            int deletetNum = 0;
            int updatetNum = 0;
            Long roleId = roleMapper.selectRoleIdMyRoleName("演示");//新增用户时，先初始化一个角色 todo

            if(CollectionUtils.isEmpty(oaUserList)){
                //如果为空则全量同步人事系统数据到OA系统
                userService.insertUserFromHr(hrUserList,roleId);
                insertNum = hrUserList.size();
            }else {
                //如果非空，筛选出OA系统存在，人事系统不存在的用户，标记为删除
                List<String> userIdList = oaUserIdList.stream()
                    .filter(user -> !hrUserIdList.contains(user))
                    .collect(Collectors.toList());
                List<String> userIdList1 = oaUserList.stream()
                    .filter(u -> userIdList.contains(u.getHrUserId()))
                    .filter(u -> !"1".equals(u.getDelFlag()))//排除已经删除的用户，无需重复删除
                    .map(HrUserVo::getUserId)
                    .collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(userIdList1)){
                    deletetNum = userService.deleteUserByIdFromHr(userIdList1);
                }

                //筛选出OA系统不存在，人事系统存在的用户，进行新增
                List<String> userIdList2 = hrUserIdList.stream()
                    .filter(user -> !oaUserIdList.contains(user))
                    .collect(Collectors.toList());
                List<HrUserVo> userList = hrUserList.stream()
                    .filter(u -> userIdList2.contains(u.getHrUserId()))
                    .collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(userList)){
                    userService.insertUserFromHr(userList,roleId);
                    insertNum = userList.size();
                }

                //筛选出两边系统都存在的用户，进行更新
                List<String> userIdList3 = oaUserIdList.stream()
                    .filter(user -> hrUserIdList.contains(user))
                    .collect(Collectors.toList());
                List<HrUserVo> userList2 = hrUserList.stream()
                    .filter(u -> userIdList3.contains(u.getHrUserId()))
                    .collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(userList2)){
                    // 每 500 条数据执行一次
                    int batchSize = 500;
                    for (int i = 0; i < userList2.size(); i += batchSize) {
                        // 获取当前批次的数据
                        List<HrUserVo> batch = userList2.subList(i, Math.min(i + batchSize, userList2.size()));
                        // 调用更新方法
                        int num = userService.updateUserFromHr(batch);
                        updatetNum += num;
                    }
                }
            }
            //同步完成后将leader字段要改成存user_id
            List<HrUserVo> userList = userService.selectAllUserList();
            userList.forEach(e ->{
                String userId = userList.stream()
                        .filter(i -> i.getHrUserId().equals(e.getLeader()))
                        .map(HrUserVo::getUserId)
                        .findFirst().orElse(null);
                e.setLeader(userId);
            });
            userService.updateUserLeader(userList);

            log.info("人事系统用户信息同步完成->新增用户:" + insertNum + "个,删除用户：" + deletetNum + "个,更新用户：" + updatetNum + "个");
        } else {
            log.error("人事系统用户信息返回空数据");
        }
    }

    /**
     * 同步组织信息：人事系统->OA系统
     * @throws JsonProcessingException
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncOrgInfo() throws JsonProcessingException {
        // 1. 调用第三方API获取组织信息
        String data = (String)hrService.selectOrgList().getData();
        log.info("人事系统组织信息: {}", data);
        // 2. 解析响应的json数据
        ObjectMapper mapper = new ObjectMapper();
        HrResultVo hrResultVo = mapper.readValue(data, new TypeReference<HrResultVo<HrDeptVo>>() {});
        // 3. 处理同步逻辑
        if (hrResultVo.getCode() == 200 && !CollectionUtils.isEmpty(hrResultVo.getData())) {
            //人事系统组织信息
            List<HrDeptVo> hrDeptList = hrResultVo.getData();
            List<String> hrDeptIdList = hrDeptList.stream().map(HrDeptVo::getDeptId).collect(Collectors.toList());
            //OA系统组织信息
            List<HrDeptVo> oaDeptList = deptService.selectAllDeptList();
            List<String> oaDeptIdList = oaDeptList.stream().map(HrDeptVo::getDeptId).collect(Collectors.toList());

            int insertNum = 0;
            int deletetNum = 0;
            int updatetNum = 0;
            if(CollectionUtils.isEmpty(oaDeptList)){
                //如果为空则全量同步人事系统数据到OA系统
                insertNum = deptService.insertDeptFromHr(hrDeptList);
            }else {
                //如果非空，筛选出OA系统存在，人事系统不存在的部门，标记为删除
                List<String> deptIdList = oaDeptIdList.stream()
                    .filter(dept -> !hrDeptIdList.contains(dept))
                    .collect(Collectors.toList());
                List<String> deptIdList1 = oaDeptList.stream()
                    .filter(d -> deptIdList.contains(d.getDeptId()))
                    .filter(d -> !"1".equals(d.getDelFlag()))//排除已经删除的机构，无需重复删除
                    .map(HrDeptVo::getDeptId)
                    .collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(deptIdList1)){
                    deletetNum = deptService.deleteDeptByIdFromHr(deptIdList);
                }
                //筛选出OA系统不存在，人事系统存在的部门，进行新增
                List<String> deptIdList2 = hrDeptIdList.stream()
                    .filter(dept -> !oaDeptIdList.contains(dept))
                    .collect(Collectors.toList());
                List<HrDeptVo> deptList = hrDeptList.stream()
                    .filter(d -> deptIdList2.contains(d.getDeptId()))
                    .collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(deptList)){
                    insertNum = deptService.insertDeptFromHr(deptList);
                }
                //筛选出两边系统都存在的部门，进行更新
                List<String> deptIdList3 = oaDeptIdList.stream()
                    .filter(dept -> hrDeptIdList.contains(dept))
                    .collect(Collectors.toList());
                List<HrDeptVo> deptList2 = hrDeptList.stream()
                    .filter(u -> deptIdList3.contains(u.getDeptId()))
                    .collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(deptList2)){
                    updatetNum = deptService.updateDeptFromHr(deptList2);
                }
            }
            //同步完成后将leader字段要改成存user_id
            List<HrUserVo> userList = userService.selectAllUserList();
            List<HrDeptVo> deptInfoList = deptService.selectAllDeptList();
            List<HrDeptVo> collect = deptInfoList.stream()
                    .filter(e -> !StringUtils.isEmpty(e.getLeader()))
                    .collect(Collectors.toList());
            collect.forEach(e ->{
                String userId = userList.stream()
                        .filter(i -> i.getHrUserId().equals(e.getLeader()))
                        .map(HrUserVo::getUserId)
                        .findFirst().orElse(null);
                e.setLeader(userId);
            });
            deptService.updateDeptLeader(deptInfoList);
            log.info("人事系统组织信息同步完成->新增组织:" + insertNum + "个,删除组织：" + deletetNum + "个,更新组织：" + updatetNum + "个");
        } else {
            log.error("人事系统组织信息返回空数据");
        }
    }

    /**
     * 同步组织信息：OA系统->资产系统
     * @throws JsonProcessingException
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncOrgInfoToAsset() throws JsonProcessingException {
        //资产系统部门列表
        List<Map<String, Object>> assetDeptList = assetService.selectDeptList();
        //OA系统部门（一级部门）
        List<HrDeptVo> oaDeptList = deptService.selectFirstDeptList();
        if (CollectionUtils.isEmpty(oaDeptList)) {
            log.error("同步失败：OA系统系统一级部门信息返回空数据");
            return;
        }

        //若资产系统部门为空，则全量同步
        if (CollectionUtils.isEmpty(assetDeptList)) {
            assetService.insertDeptFromOa(oaDeptList);
            log.info("同步组织信息：OA系统->资产系统完成->新增组织:" + oaDeptList.size() + "个");
        } else {
            //若资产系统部门非空，根据部门名称筛选，资产系统没有的就新增
            List<String> assetDeptNameList = assetDeptList.stream().map(e -> e.get("name").toString()).collect(Collectors.toList());
            List<String> oaDeptNameList = oaDeptList.stream().map(HrDeptVo::getDeptName).collect(Collectors.toList());
            List<String> deptNameList = oaDeptNameList.stream()
                    .filter(d -> !assetDeptNameList.contains(d))
                    .collect(Collectors.toList());
            List<HrDeptVo> deptList = oaDeptList.stream()
                    .filter(d -> deptNameList.contains(d.getDeptName()))
                    .collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(deptList)){
                assetService.insertDeptFromOa(deptList);
            }
            log.info("同步组织信息：OA系统->资产系统完成->新增组织:" + deptNameList.size() + "个");
        }
    }

    /**
     * 同步用户信息：OA系统->资产系统
     * @throws JsonProcessingException
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncUserInfoToAsset() throws JsonProcessingException {
        //查询资产系统已经同步过得部门id、部门名称（用于同步用户时，赋值部门id）
        List<Map<String, Object>> assetDeptList = assetService.selectDeptList();
        //查询OA所有用户（不包含OA本身的用户）（其部门层级>=3）,且将部门层级>3的用户的部门id重新赋值为一级部门id
        List<HrUserVo> oaUserList = userService.selectUserDeptList();
        List<String> oaUserEmpList = oaUserList.stream().map(HrUserVo::getEmpNo).collect(Collectors.toList());
        //查询资产系统用户
        List<Map<String, Object>> assetUserList = assetService.selectUserList();
        List<String> assetUserEmpList = assetUserList.stream().map(e -> e.get("username").toString()).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(oaUserList)) {
            log.error("同步失败：OA系统系统用户信息返回空数据");
            return;
        }
        //将用户部门id赋值为资产系统对应的部门id，将assetUserId赋值为资产系统用户id
        oaUserList.forEach(u ->{
            String deptId = assetDeptList.stream()
                    .filter(e -> e.get("name").toString().equals(u.getDeptName()))
                    .map(e -> e.get("id"))
                    .findFirst().get().toString();

            List<Map<String, Object>> empNo = assetUserList.stream()
                    .filter(e -> e.get("username").toString().equals(u.getEmpNo()))
                    .collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(empNo)){
                String userId = empNo.stream().map(e -> e.get("id")).findFirst().get().toString();
                u.setAssetUserId(userId);
            }
            u.setDeptId(deptId);

        });
        //若资产系统用户为空， OA用户非空，则全量同步
        if(CollectionUtils.isEmpty(assetUserList)){
            assetService.insertUserFromOa(oaUserList);
            log.info("同步用户信息：OA系统->资产系统完成->新增用户:" + oaUserList.size() + "个");
        }else {
            //用户只存在资产系统，不存在OA系统，先不处理，先不删除
            List<String> assetUserEmpList2 = assetUserEmpList.stream()
                    .filter(e -> !oaUserEmpList.contains(e))
                    .collect(Collectors.toList());
            List<Map<String, Object>> userList = assetUserList.stream()
                    .filter(e -> assetUserEmpList2.contains(e.get("username").toString()))
                    .collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(userList)){
                //todo 先不处理
            }
            //用户只存在OA系统，不存在资产系统，则新增
            List<String> oaUserEmpList2 = oaUserEmpList.stream()
                    .filter(e -> !assetUserEmpList.contains(e))
                    .collect(Collectors.toList());
            List<HrUserVo> userList2 = oaUserList.stream()
                    .filter(e -> oaUserEmpList2.contains(e.getEmpNo()))
                    .collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(userList2)){
                assetService.insertUserFromOa(userList2);
            }
            //若用户名（工号）存在2系统，则更新资产系统用户的：名字、邮箱、电话、部门
            List<String> userEmpList = oaUserEmpList.stream()
                    .filter(e -> assetUserEmpList.contains(e))
                    .collect(Collectors.toList());
            List<HrUserVo> userList3 = oaUserList.stream()
                    .filter(e -> userEmpList.contains(e.getEmpNo()))
                    .collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(userList3)){
                assetService.updateUserFromOa(userList3);
            }

            //查询同步后的资产系统用户
            List<Map<String, Object>> assetUserList2 = assetService.selectUserList();
            //将同步后的资产系统的用户表id，存储到OA系统用户表的asset_user_id字段
            oaUserList.forEach(u ->{
                List<Map<String, Object>> empNo = assetUserList2.stream()
                        .filter(e -> e.get("username").toString().equals(u.getEmpNo()))
                        .collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(empNo)){
                    String userId = empNo.stream().map(e -> e.get("id")).findFirst().get().toString();
                    u.setAssetUserId(userId);
                }
            });
            userService.updateUserInfo(oaUserList);

            log.info("同步用户信息：OA系统->资产系统完成->新增用户:" + userList2.size() + "个，更新用户：" + userList3.size() +"个");
        }

    }


}
