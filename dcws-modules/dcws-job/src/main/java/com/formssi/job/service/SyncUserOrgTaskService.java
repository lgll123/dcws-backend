package com.formssi.job.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.job.snailjob.SyncUserOrgTask;
import com.formssi.system.domain.vo.HrDeptVo;
import com.formssi.system.domain.vo.HrResultVo;
import com.formssi.system.domain.vo.HrUserVo;
import com.formssi.system.mapper.SysRoleMapper;
import com.formssi.system.service.ISysDeptService;
import com.formssi.system.service.ISysHrService;
import com.formssi.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private final SysRoleMapper roleMapper;

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
            //OA系统用户信息（要排除掉系統本身的用戶）
            List<HrUserVo> oaUserList = userService.selectAllUserList();
            List<String> oaUserIdList = oaUserList.stream().map(HrUserVo::getUserId).collect(Collectors.toList());

            int insertNum = 0;
            int deletetNum = 0;
            int updatetNum = 0;
            Long roleId = roleMapper.selectRoleIdMyRoleName("演示");//新增用户时，先初始化一个角色 todo

            if(CollectionUtils.isEmpty(oaUserList)){
                //如果为空则全量同步人事系统数据到OA系统
                // 每 500 条数据执行一次
                int batchSize = 500;
                for (int i = 0; i < hrUserList.size(); i += batchSize) {
                    // 获取当前批次的数据
                    List<HrUserVo> batch = hrUserList.subList(i, Math.min(i + batchSize, hrUserList.size()));
                    // 调用更新方法
                    int num = userService.insertUserFromHr(batch,roleId);
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
                        int num = userService.insertUserFromHr(batch,roleId);
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




    private static final String API_URL = "http://10.101.68.29:8000/api/v1/";
    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiIxIiwianRpIjoiNDRlMWQ2OTA0ZjM3NTQyNDJmZmU5ZTMxOTk0MjZiNzI1MDRkYzNhMmM4ODhhNTIxZWFjNDdlNDA4MjliZWUxZjBmNWRhOGFhYTA2MDZmMjIiLCJpYXQiOjE3NDAwNDEwNzUuNzY0Nzg5LCJuYmYiOjE3NDAwNDEwNzUuNzY0NzkxLCJleHAiOjMwMDIzNDUwNzUuNzYyMjEzLCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.TowtjDbbsNkWvdLxz16s69Cj3zA1KF8iioV3jOF26W6SwjP8JWA1r4Fvk71rcOXNTlETeZMS-xmFgjeXyZHYOcqeizIOUXwpdppsBXKuzTH8z2XaZKrpYLNVJrrip1hZEYC_xdln6vapWHRtxwVYLM-L-yzz3Ytc97_afab7f5El6-WxsIvPy7lBIhfexwICweOTU-7EgRdyA5DO21Y5YVC6yBGQBb4U4GRqxhNM_WjsKg4msH_MT19FzEBY0JLgIPzDF1sS66IywsGRJr-yBy6cSdk9h4aOxjprSuWZYaI9Lk_B1jHkcE58EB7Tjvrw_jiOofPoUVSPTxx_wGIhpk8Lz_3Yyb-Jzimp59JxbWaek5TPbxcLEaElrsL2MFY6mgbWpAlzdxQTgYzG5VkY9wjqw08NOklGhMIAtukX4-fwK2dHLu_AiyFNojYKcOinCNKb-ybeSIyG-hE9Tk9Iw4H0h2B5RH3TA64GRCwjl1y1RIgurxUR7peKFABxGpNBJ-63R90byn9Q5GXzndtOp9UZplYLg4XME7jt7yarZJUMjIFnW8ef938UTzg2Gig7HetrKeHEE_-Vl8RDFcpYpRn29QxTOrkriSX6SNu8SdH_zE-sL9NStHz0BfwI6jfb96gFretBzArxs2tVcUHZysxwTn-LkenpMcHX4owrNCU";
    private final OkHttpClient client = new OkHttpClient();

    @Transactional(rollbackFor = Exception.class)
    public void testaaa()  {
        try {
            String jsonData = "{\"hard\": [], \"asset\": [{\"id\": 5, \"qty\": null, \"name\": \"资产-苹果电脑\", \"serial\": \"2\", \"modelNo\": \"test\", \"assetTag\": \"C02G64PV3D6T\", \"assetType\": null, \"remainQty\": null, \"productKey\": null, \"assetStatus\": \"deployable\", \"licenseName\": null, \"categoryName\": \"笔记本\", \"licenseEmail\": null, \"locationName\": null, \"purchaseCost\": null, \"purchaseDate\": null, \"checkoutsCount\": null, \"expirationDate\": null, \"manufacturerName\": null}], \"license\": []}";
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<Map<String, String>>> mapData = mapper.readValue(jsonData, new TypeReference<Map<String, List<Map<String, String>>>>() {});
            List<Map<String, String>> assetList = mapData.get("asset");//IT资产
            List<Map<String, String>> licenseList = mapData.get("license");//软件清单
            List<Map<String, String>> hardList = mapData.get("hard");//硬件清单

            //附属品
            if(true){
                //1、根据附属品id 查询 附属品用户关联id
                String assetId = "6"; //附属品id 先写死 todo
                String userId = "2"; //先写死被领用用户（张三） todo
                int num = 1; //借出的附属品数量 先写死 todo
                String apiUrl = API_URL + "accessories/" + assetId + "/checkedout";
                List<Map<String, Object>> mapList = callGetApi(apiUrl);//调用接口
                List<String> idList = mapList.stream().filter(u -> { //附属品用户关联id
                            Map<String, String> o = (HashMap) u.get("assigned_to");
                            if (!ObjectUtils.isEmpty(o)) {
                                return userId.equals(String.valueOf(o.get("id")));//筛选出被领用用户的 附属品用户关联id
                            } else {
                                return false;
                            }
                        }).map(e -> String.valueOf(e.get("id")))
                        .limit(num).collect(Collectors.toList());
                //2、根据附属品用户关联id 归还附属品
                idList.forEach(id ->{
                    String apiUrl2 = API_URL + "accessories/" + id + "/checkin";
                    callPostApi(apiUrl2, "");//调用接口
                });
                //3、根据附属品id 借出附属品
                String apiUrlOut = API_URL + "accessories/" + assetId + "/checkout";
                ObjectMapper objectMapperOut = new ObjectMapper();
                Map<String, String> requestBodyMapOut = new HashMap<>();
                requestBodyMapOut.put("checkout_qty", String.valueOf(num));
                requestBodyMapOut.put("assigned_user", "4"); //借出的用户（协同用户）先写死 todo
                String requestBodyOut = null;
                try {
                    requestBodyOut = objectMapperOut.writeValueAsString(requestBodyMapOut);
                } catch (JsonProcessingException ex) {
                    throw new RuntimeException(ex);
                }
                callPostApi(apiUrlOut, requestBodyOut);
            }


        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
            // 抛出BPMN错误，触发错误边界事件
        }
    }




















    public void callPostApi(String apiUrl, String requestBody) {
        log.info("Calling the external system for Assets with URL: {}", apiUrl);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(requestBody, mediaType);
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", BEARER_TOKEN)
                .addHeader("content-type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                log.info("External system response: {}", responseBody);
            } else {
                log.error("External system call failed with status code: {}", response.code());
                // 抛出BPMN错误，触发错误边界事件
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Map<String,Object>> callGetApi(String apiUrl) {
        log.info("Calling the external system for Assets with URL: {}", apiUrl);
        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", BEARER_TOKEN)
                .addHeader("content-type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                log.info("External system response: {}", responseBody);
                ObjectMapper objectMapper = new ObjectMapper();
                Map map = objectMapper.readValue(responseBody, Map.class);
                List<Map<String,Object>> rows = (List<Map<String,Object>>)map.get("rows");
                if(rows.isEmpty()){
                    throw new ServiceException( "查询失败");
                }
                return rows;
            } else {
                log.error("External system call failed with status code: {}", response.code());
                throw new RuntimeException();
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void callPutApi(String apiUrl, String requestBody) {
        log.info("Calling the external system for Assets with URL: {}", apiUrl);

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(requestBody, mediaType);
        Request request = new Request.Builder()
                .url(apiUrl)
                .put(body)
                .addHeader("accept", "application/json")
                .addHeader("Authorization", BEARER_TOKEN)
                .addHeader("content-type", "application/json")
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                log.info("External system response: {}", responseBody);
            } else {
                log.error("External system call failed with status code: {}", response.code());
                // 抛出BPMN错误，触发错误边界事件
            }
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
