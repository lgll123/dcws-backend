package com.formssi.workflow.externalsystem;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.constant.HttpStatus;
import com.formssi.common.core.exception.ApiCallException;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.DcwsAssetsCheckOutBo;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.service.IAssetsCheckOutRecordService;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class CallAssetsSystemCheckOutTaskListener implements TaskListener {
    private final String beanName = "assets" + IExternalSystemAPIStrategy.BASE_NAME;
    private final IAssetsCheckOutRecordService iAssetsCheckOutRecordService = SpringUtils.getBean(IAssetsCheckOutRecordService.class);
    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        TaskNodeDataBo taskNodeDataBo = null;
        HashMap<String,Object> hashMap =null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Object entity = variables.get("entity");
            if(variables.get("entity")!=null) {
                taskNodeDataBo = objectMapper.readValue(JSON.toJSONString(entity), TaskNodeDataBo.class);
                hashMap = objectMapper.readValue(taskNodeDataBo.getApplyDetail(), HashMap.class);
                ArrayList<Map<String, Object>> assets = (ArrayList<Map<String, Object>>) hashMap.get("asset");
                ArrayList<Map<String, Object>> licenses = (ArrayList<Map<String, Object>>) hashMap.get("license");
                ArrayList<Map<String, Object>> hards = (ArrayList<Map<String, Object>>) hashMap.get("hard");
                Long applicantId = taskNodeDataBo.getApplicantId();
                Long taskNodeDataId = taskNodeDataBo.getId();
                if (!CollectionUtil.isEmpty(assets)) {
                    for (int i = 0; i < assets.size(); i++) {
                        DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
                        bo.setAssetsType("hardware");
                        bo.setAssetsDetail(JSON.toJSONString(assets.get(i)));
                        bo.setTaskNodeDataId(taskNodeDataId);
                        bo.setCheckOutUser(applicantId.toString());
                        bo.setAutoHandleNum(0);
                        bo.setStatus("1");//成功
                        Integer id = (Integer) assets.get(i).get("id");
                        Map<String, String> requestBodyMap = new HashMap<>();
                        requestBodyMap.put("status_id",  String.valueOf(assets.get(i).get("assetStatusId")));
                        requestBodyMap.put("checkout_to_type",  "user");
                        requestBodyMap.put("assigned_user",  applicantId.toString());
                        IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
                        try {
                            Map<String, Object> responseMap = instance.process(requestBodyMap,"hardware/"+id+"/checkout","post");
                            if("error".equals(responseMap.get("status"))){
                                log.info("后台API接口返回错误：" + responseMap.get("messages"));
                                bo.setMessage(responseMap.get("messages").toString());
                                bo.setCode(responseMap.get("status").toString());
                                bo.setStatus("0");//失败
                            }
                        } catch (ApiCallException e) {
                            if(e.getCode()>0 && e.getCode()!=200){
                                bo.setStatus("3");//失败待处理
                            }
                            bo.setMessage(e.getMessage());
                            bo.setStatus("0");//失败
//                            throw e;
                        }
                        // 记录物料checkOut 记录
                        addAssetsCheckOutRecord(bo,iAssetsCheckOutRecordService);
                    }
                }
                if (!CollectionUtil.isEmpty(licenses)) {
                    IExternalSystemAPIStrategy instance = SpringUtils.getBean(beanName);
                    for (int i = 0; i < licenses.size(); i++) {
                        DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
                        bo.setAssetsType("licenses");
                        bo.setAssetsDetail(JSON.toJSONString(licenses.get(i)));
                        bo.setTaskNodeDataId(taskNodeDataId);
                        bo.setCheckOutUser(applicantId.toString());
                        bo.setAutoHandleNum(0);
                        bo.setStatus("1");//成功
                        Integer id = (Integer) licenses.get(i).get("id");
                        try {
                        Map<String, Object> responseMap = instance.process(null,"licenses/"+id+"/seats","get");
                        if("error".equals(responseMap.get("status"))){
                            log.info("后台API接口返回错误：" + responseMap.get("messages"));
                            bo.setMessage(responseMap.get("messages").toString());
                            bo.setCode(responseMap.get("status").toString());
                            bo.setStatus("0");//失败
                            // 记录物料checkOut 记录
                            addAssetsCheckOutRecord(bo,iAssetsCheckOutRecordService);
                            continue;
                        }
                        List<Map<String,Object>> maps = (List<Map<String,Object>>)responseMap.get("rows");

                        List<Integer> seatIds = maps.stream().filter(l -> l.get("assigned_user") == null && l.get("location") == null).map(seat -> {
                            return (Integer) seat.get("id");
                        }).collect(Collectors.toList());
                        if(seatIds.size()==0){
                            bo.setMessage("licenses可用库存不足 seatIds："+seatIds.size());
                            bo.setStatus("0");//失败
                            // 记录物料checkOut 记录
                            addAssetsCheckOutRecord(bo,iAssetsCheckOutRecordService);
                            continue;
                        }
                        Map<String, String> requestBodyMap = new HashMap<>();
                        requestBodyMap.put("seat_id", seatIds.get(0).toString());
                        requestBodyMap.put("assigned_to", applicantId.toString());

                            Map<String, Object> responseMap1 = instance.process(requestBodyMap,"licenses/"+id+"/seats/"+seatIds.get(i),"put");
                            if("error".equals(responseMap1.get("status"))){
                                log.info("后台API接口返回错误：" + responseMap1.get("messages"));
                                bo.setMessage(responseMap1.get("messages").toString());
                                bo.setCode(responseMap1.get("status").toString());
                                bo.setStatus("0");//失败
                            }
                        } catch (ApiCallException e) {
                            if(e.getCode()>0 && e.getCode()!= HttpStatus.SUCCESS){
                                bo.setStatus("3");//失败待处理
                            }
                            bo.setMessage(e.getMessage());
                            bo.setStatus("0");//失败
//                            throw e;
                        }
                        // 记录物料checkOut 记录
                        addAssetsCheckOutRecord(bo,iAssetsCheckOutRecordService);
                    }
                }

                /*if(!CollectionUtil.isEmpty(hards)){
                    hards.forEach(m->{
                        Integer id = (Integer) m.get("id");
                        Map<String, Object> requestBodyMap = new HashMap<>();
                        requestBodyMap.put("assigned_user", String.valueOf(finalApplicantId));
                        requestBodyMap.put("asset_tag", (String)m.get("assetTag"));
                        requestBodyMap.put("status_id", m.get("assetStatusId"));
                        requestBodyMap.put("model_id", m.get("modelId"));
                        try {
                            this.callSystem(requestBodyMap,"hardware");
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    });
                }*/


            }
        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
            Map<String,Object> entityMap = (Map<String,Object>)variables.get("entity");
            DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
            bo.setTaskNodeDataId(Long.valueOf(entityMap.get("id").toString()));
            bo.setMessage(e.getMessage());
            bo.setAssetsDetail(entityMap.get("applyDetail").toString());
            bo.setStatus("2");//部分异常待处理
            // 记录物料checkOut 记录
            addAssetsCheckOutRecord(bo,iAssetsCheckOutRecordService);
            // 抛出BPMN错误，触发错误边界事件
//            throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: " + e.getMessage());
        }
    }

    private static void addAssetsCheckOutRecord(DcwsAssetsCheckOutBo bo,IAssetsCheckOutRecordService assetsCheckOutRecordService){
        try {
            assetsCheckOutRecordService.insertByBo(bo);
            if(bo.getId()==null){
                log.info("物料申请借出记录新增失败：id == null");
            }
        } catch (Exception e) {
            log.info("物料申请借出记录新增失败：" + e.getMessage(),e);
        }
    }

}
