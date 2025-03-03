package com.formssi.workflow.externalsystem.assets.listener;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 更换领用人，先归还，再借出
 */
@Slf4j
public class CallAssetsSystemCheckInOutTaskListener implements TaskListener {
    private final IExternalSystemAPIStrategy instance = SpringUtils.getBean("assets" + IExternalSystemAPIStrategy.BASE_NAME);
    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        TaskNodeDataBo taskNodeDataBo = null;
        HashMap<String,Object> hashMap =null;
        ArrayList<Map<String, Object>> hardware = null;
        ArrayList<Map<String, Object>> licenseList = null;
        ArrayList<Map<String, Object>> hardList = null;
        try {
            Object entity = variables.get("entity");
            if(variables.get("entity")!=null) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    taskNodeDataBo = objectMapper.readValue(JSON.toJSONString(entity), TaskNodeDataBo.class);
                    hashMap = objectMapper.readValue(taskNodeDataBo.getApplyDetail(), HashMap.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                //附属品-accessories、组件-components、许可证-licenses、消耗品-consumables、资产-hardware
                hardware = (ArrayList<Map<String, Object>>) hashMap.get("hardware");
                licenseList = (ArrayList<Map<String, Object>>) hashMap.get("license");
                hardList = (ArrayList<Map<String, Object>>) hashMap.get("hard");
            }
            //一、资产
            if(!CollectionUtils.isEmpty(hardware)){
                hardware.forEach(e ->{
                    //1、归还
                    String assetIdIn = String.valueOf(e.get("id")); //资产id
                    String apiUrlIn = "hardware/" + assetIdIn + "/checkin";
                    Map<String, String> requestBodyMapIn = new HashMap<>();
                    requestBodyMapIn.put("status_id", "2");
                    Map<String, Object> responseMap = instance.process(requestBodyMapIn,apiUrlIn,"post");
                    if("error".equals(responseMap.get("status"))) {
                        log.info("后台API接口返回错误：" + responseMap.get("messages"));
                    }
                    //2、借出
                    String apiUrlOut = "hardware/" + assetIdIn + "/checkout";
                    Map<String, String> requestBodyMapOut = new HashMap<>();
                    requestBodyMapOut.put("status_id",  String.valueOf(e.get("assetStatusId")));
                    requestBodyMapOut.put("checkout_to_type", "user");
                    requestBodyMapOut.put("assigned_user", String.valueOf(((Map<String, Object>)e.get("recipient")).get("assetUserId"))); //领用人Id"4"
                    Map<String, Object> responseMapOut = instance.process(requestBodyMapOut,apiUrlOut,"post");
                    if("error".equals(responseMapOut.get("status"))) {
                        log.info("后台API接口返回错误：" + responseMapOut.get("messages"));
                    }
                });
            }
            //二、许可证
            /*if(!CollectionUtils.isEmpty(licenseList)){
                String applicant = taskNodeDataBo.getApplicant();
                licenseList.forEach(e ->{
                    //1、根据许可证id查询席位id
                    //String assetId = "1"; //许可证id 先写死 todo
                    String assetId = e.get("id").toString();
                    String apiUrl =  "licenses/" + assetId + "/seats?sort=name&order=asc";
                    Map<String, Object> responseMap = instance.process(null,apiUrl,"get");
                    if("error".equals(responseMap.get("status"))) {
                        log.info("后台API接口返回错误：" + responseMap.get("messages"));
                    }
                    List<Map<String,Object>> rows = (List<Map<String,Object>>)responseMap.get("rows");
                    if(rows.isEmpty()){
                        throw new ServiceException( "查询失败");
                    }
                    Map<String, Object> seatMap = rows.stream().filter(u -> {
                        Map<String,String> o = (HashMap)u.get("assigned_user");
                        if(!ObjectUtils.isEmpty(o)){
                            return applicant.equals(String.valueOf(o.get("id")));//筛选出被领用用户的席位
                        }else {
                            return false;
                        }
                    }).findFirst().get();
                    String seatId = String.valueOf(seatMap.get("id"));//席位id
                    //2、根据席位id更换许可证领用人
                    String apiUrl2 = "licenses/" + assetId + "/seats/" + seatId;
                    Map<String, String> requestBodyMapOut = new HashMap<>();
                    requestBodyMapOut.put("assigned_to", "4"); //先写死用户（协同用户） todo
                    Map<String, Object> responseMap1 = instance.process(requestBodyMapOut,apiUrl2,"post");
                    if("error".equals(responseMap1.get("status"))) {
                        log.info("后台API接口返回错误：" + responseMap1.get("messages"));
                    }
                });
            }*/
            //三、附属品
            /*if(!CollectionUtils.isEmpty(hardList)){
                hardList.forEach(a ->{
                    //1、根据附属品id 查询 附属品用户关联id
//                    String assetId = "6"; //附属品id 先写死 todo
                    String assetId = a.get("id").toString();
                    String userId = "2"; //先写死被领用用户（张三） todo
                    int num = 1; //借出的附属品数量 先写死 todo
                    String apiUrl = "accessories/" + assetId + "/checkedout";
                Map<String, Object> responseMap1 = instance.process(null,apiUrl,"get");
                if("error".equals(responseMap1.get("status"))) {
                    log.info("后台API接口返回错误：" + responseMap1.get("messages"));
                }
                List<Map<String,Object>> rows = (List<Map<String,Object>>)responseMap1.get("rows");
                if(rows.isEmpty()){
                    throw new ServiceException( "查询失败");
                }
                    List<String> idList = rows.stream().filter(u -> { //附属品用户关联id
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
                        String apiUrl2 = "accessories/" + id + "/checkin";
                        Map<String, Object> responseMap = instance.process(null,apiUrl2,"post");
                        if("error".equals(responseMap.get("status"))) {
                            log.info("后台API接口返回错误：" + responseMap.get("messages"));
                        }
                    });
                    //3、根据附属品id 借出附属品
                    String apiUrlOut = "accessories/" + assetId + "/checkout";
                    Map<String, String> requestBodyMapOut = new HashMap<>();
                    requestBodyMapOut.put("checkout_qty", String.valueOf(num));
                    requestBodyMapOut.put("assigned_user", "4"); //借出的用户（协同用户）先写死 todo
                    Map<String, Object> responseMap = instance.process(requestBodyMapOut,apiUrlOut,"post");
                    if("error".equals(responseMap.get("status"))) {
                        log.info("后台API接口返回错误：" + responseMap.get("messages"));
                    }
                });
            }*/
            //四、消耗品---无需更换领用人

            //五、组件---无需更换领用人



        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
            // 抛出BPMN错误，触发错误边界事件
            throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: " + e.getMessage());
        }
    }




}
