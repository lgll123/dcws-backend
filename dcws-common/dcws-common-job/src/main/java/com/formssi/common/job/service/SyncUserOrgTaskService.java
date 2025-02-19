package com.formssi.common.job.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import com.formssi.common.job.config.SyncUserOrgTask;
import com.formssi.system.domain.vo.HrDeptVo;
import com.formssi.system.domain.vo.HrResultVo;
import com.formssi.system.domain.vo.HrUserVo;
import com.formssi.system.service.ISysDeptService;
import com.formssi.system.service.ISysHrService;
import com.formssi.system.service.ISysUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户 业务层处理
 *
 * @author Lion Li
 */
@RequiredArgsConstructor
@Service
public class SyncUserOrgTaskService {

    private final ISysHrService hrService;

    private final ISysUserService userService;

    private final ISysDeptService deptService;

    private static final Logger log = LoggerFactory.getLogger(SyncUserOrgTask.class);


    /**
     * 同步用户信息
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
            List<String> hrUserIdList = hrUserList.stream().map(HrUserVo::getUserId).collect(Collectors.toList());
            //OA系统用户信息
            List<HrUserVo> oaUserList = userService.selectAllUserList();
            List<String> oaUserIdList = oaUserList.stream().map(HrUserVo::getUserId).collect(Collectors.toList());

            int insertNum = 0;
            int deletetNum = 0;
            int updatetNum = 0;
            if(CollectionUtils.isEmpty(oaUserList)){
                //如果为空则全量同步人事系统数据到OA系统
                // 每 500 条数据执行一次
                int batchSize = 500;
                for (int i = 0; i < hrUserList.size(); i += batchSize) {
                    // 获取当前批次的数据
                    List<HrUserVo> batch = hrUserList.subList(i, Math.min(i + batchSize, hrUserList.size()));
                    // 调用更新方法
                    int num = userService.insertUserFromHr(batch);
                    insertNum += num;
                }
            }else {
                //如果非空，筛选出OA系统存在，人事系统不存在的用户，标记为删除
                List<String> userIdList = oaUserIdList.stream()
                    .filter(user -> !hrUserIdList.contains(user))
                    .collect(Collectors.toList());
                List<String> userIdList1 = oaUserList.stream()
                    .filter(u -> userIdList.contains(u.getUserId()))
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
                    .filter(u -> userIdList2.contains(u.getUserId()))
                    .collect(Collectors.toList());
                if(!CollectionUtils.isEmpty(userList)){
                    // 每 500 条数据执行一次
                    int batchSize = 500;
                    for (int i = 0; i < userList.size(); i += batchSize) {
                        // 获取当前批次的数据
                        List<HrUserVo> batch = userList.subList(i, Math.min(i + batchSize, userList.size()));
                        // 调用更新方法
                        int num = userService.insertUserFromHr(batch);
                        insertNum += num;
                    }
                }
                //筛选出两边系统都存在的用户，进行更新
                List<String> userIdList3 = oaUserIdList.stream()
                    .filter(user -> hrUserIdList.contains(user))
                    .collect(Collectors.toList());
                List<HrUserVo> userList2 = hrUserList.stream()
                    .filter(u -> userIdList3.contains(u.getUserId()))
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
            log.info("人事系统用户信息同步完成->新增用户:" + insertNum + "个,删除用户：" + deletetNum + "个,更新用户：" + updatetNum + "个");
        } else {
            log.error("人事系统用户信息返回空数据");
        }
    }



    /**
     * 同步组织信息
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
                //筛选出OA系统不存在，人事系统存在的用户，进行新增
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
            log.info("人事系统组织信息同步完成->新增组织:" + insertNum + "个,删除组织：" + deletetNum + "个,更新组织：" + updatetNum + "个");
        } else {
            log.error("人事系统组织信息返回空数据");
        }
    }
}
