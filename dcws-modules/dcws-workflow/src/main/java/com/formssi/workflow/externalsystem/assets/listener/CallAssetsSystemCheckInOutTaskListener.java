package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.constant.HttpStatus;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.DcwsAssetsCheckOutBo;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
import com.formssi.workflow.externalsystem.exception.ApiCallException;
import com.formssi.workflow.service.IAssetsCheckOutRecordService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.formssi.workflow.externalsystem.assets.constant.AssetsConstant.*;

/**
 * 更换领用人，先归还，再借出
 */
@Slf4j
@Component("CallAssetsSystemCheckInOutTaskListener")
public class CallAssetsSystemCheckInOutTaskListener implements TaskListener {
    private static  final IAssetsCheckOutRecordService iAssetsCheckOutRecordService = SpringUtils.getBean(IAssetsCheckOutRecordService.class);
    private final IExternalSystemAPIStrategy instance = SpringUtils.getBean("assets" + IExternalSystemAPIStrategy.BASE_NAME);
    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        try {
            Object entity = variables.get("entity");
            if (ObjectUtil.isEmpty(entity)) return;
            ObjectMapper mapper = new ObjectMapper();
            TaskNodeDataBo taskNodeDataBo = mapper.readValue(JSONUtil.toJsonStr(entity), TaskNodeDataBo.class);
            if (ObjectUtil.isEmpty(taskNodeDataBo)) return;
            Map<String ,Object> map = mapper.readValue(taskNodeDataBo.getApplyDetail(), Map.class);
                //附属品-accessories、组件-components、许可证-licenses、消耗品-consumables、资产-hardware
                ArrayList<Map<String, Object>> hardware = (ArrayList<Map<String, Object>>) map.get("hardware");
                ArrayList<Map<String, Object>> accessories = (ArrayList<Map<String, Object>>) map.get("accessories");
                // 资产
                processHardwareAssets(taskNodeDataBo, hardware);
                // 附属品
                processAccessories(taskNodeDataBo,accessories);

        } catch (Exception e) {
            // 记录物料checkOut 记录
            Map<String,Object> entityMap = (Map<String,Object>)variables.get("entity");
            DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
            bo.setTaskNodeDataId(entityMap.get("id").toString());
            bo.setMessage(e.getMessage());
            bo.setAssetsDetail(Convert.toStr(entityMap.get("applyDetail")));
            bo.setCheckOutUser(Convert.toStr(entityMap.get("applicantId")));
            bo.setStatus("2");//部分异常待处理
            bo.setCheckType("1");
            bo.setAssetsType("0");
            bo.setCheckOutIn("2");
            saveCheckOutRecord(bo);
            log.error("An error occurred while calling the external system", e);
        }
    }
    // 处理硬件资产
    private void processHardwareAssets(TaskNodeDataBo taskNode, List<Map<String, Object>> hardwareList) {
        if (CollectionUtils.isEmpty(hardwareList)) return;
        for (int i = 0; i < hardwareList.size(); i++) {
            DcwsAssetsCheckOutBo recordBo = createBaseRecord(taskNode, "hardware");
            Map<String, Object> hardware = hardwareList.get(i);
            Map<String, Object> recipient = (Map<String, Object>) hardware.get("recipient");
            //未设置领用人
            if (!validateRecipient(recipient, recordBo)) {
                recordBo.setCheckType("1");
                recordBo.setAssetsDetail(JSONUtil.toJsonStr(hardwareList.get(i)));
                saveCheckOutRecord(recordBo);
                continue;
            }
            String assetId = Convert.toStr(hardware.get("id"));//资产id
            String assetUserId = Convert.toStr(recipient.get("assetUserId"));//领用人
            // 资产归还操作
            if (!processHardwareCheckIn(assetId, assetUserId, hardware, recordBo)) continue;

            // 资产借出操作
            processHardwareCheckOut(assetId, assetUserId, taskNode.getApplicant(),
                    Convert.toStr(recipient.get("name")), hardware, recordBo);
        }
    }
    // 资产归还操作
    private boolean processHardwareCheckIn(String assetIdIn, String assetUserId, Map<String, Object> hardware, DcwsAssetsCheckOutBo bo){
        //1、归还
        String apiUrlIn = "hardware/" + assetIdIn + "/checkin";
        Map<String, String> requestBodyMapIn = new HashMap<>();
        requestBodyMapIn.put(STATUS_ID, ASSETS_STATUS_7);
        Map<String, Object> responseMap = null;
        try {
            responseMap = instance.process(requestBodyMapIn, apiUrlIn, "post");
        } catch (ApiCallException e) {
            bo.setStatus("0");//失败
            if (e.getCode() > 0 && e.getCode() != HttpStatus.SUCCESS) {
                bo.setStatus("2");//失败待处理:网络或者权限或者接口url原因导致失败的需要重新发请求处理
            }
            bo.setMessage(e.getMessage());
            bo.setCode(Convert.toStr(e.getCode()));
            bo.setCheckType("2");
            bo.setAssetsDetail(JSONUtil.toJsonStr(hardware));
            bo.setCheckOutUser(assetUserId);
            bo.setAssetsType("hardware");
            // 记录物料checkOut 记录
            saveCheckOutRecord(bo);
            return false;
        }
        if ("error".equals(responseMap.get("status"))) {
            bo.setMessage(Convert.toStr(responseMap.get("messages")));
            bo.setCode(Convert.toStr(responseMap.get("status")));
            bo.setStatus("0");
            bo.setCheckType("2");
            bo.setAssetsDetail(JSONUtil.toJsonStr(hardware));
            bo.setCheckOutUser(assetUserId);
            bo.setAssetsType("hardware");
            // 记录物料checkOut 记录
            saveCheckOutRecord(bo);
            return false;
        }
        //资产归还成功
        bo.setMessage("资产归还成功");
        bo.setStatus("1");
        bo.setCheckType("2");
        bo.setAssetsDetail(JSONUtil.toJsonStr(hardware));
        bo.setCheckOutUser(assetUserId);
        bo.setAssetsType("hardware");
        // 记录物料checkOut 记录
        saveCheckOutRecord(bo);
        return true;
    }

    // 资产借出操作
    private void processHardwareCheckOut(String assetId, String assetUserId, String applicant,
                                         String recipientName, Map<String, Object> hardware, DcwsAssetsCheckOutBo bo){
        //借出
        String apiUrlOut = "hardware/" + assetId + "/checkout";
        Map<String, String> requestBodyMapOut = new HashMap<>();
        requestBodyMapOut.put(STATUS_ID, ASSETS_STATUS_11);
        requestBodyMapOut.put("checkout_to_type", "user");
        requestBodyMapOut.put("assigned_user", assetUserId); //领用人Id"4"
        requestBodyMapOut.put("note", "领用人：" + applicant + " 变更到：" + recipientName);
        Map<String, Object> responseMapOut = null;
        try {
            responseMapOut = instance.process(requestBodyMapOut, apiUrlOut, "post");
        } catch (ApiCallException e) {
            bo.setStatus("0");//失败
            if (e.getCode() > 0 && e.getCode() != HttpStatus.SUCCESS) {
                bo.setStatus("2");//失败待处理:网络或者权限或者接口url原因导致失败的需要重新发请求处理
            }
            bo.setMessage(e.getMessage());
            bo.setCode(Convert.toStr(e.getCode()));
            bo.setCheckType("1");
            bo.setCheckOutUser(assetUserId);
            bo.setAssetsDetail(JSONUtil.toJsonStr(hardware));
            bo.setAssetsType("hardware");
            // 记录物料checkOut 记录
            saveCheckOutRecord(bo);
            return;
        }
        if ("error".equals(responseMapOut.get("status"))) {
            bo.setMessage(Convert.toStr(responseMapOut.get("messages")));
            bo.setCode(Convert.toStr(responseMapOut.get("status")));
            bo.setStatus("0");
            bo.setCheckType("1");
            bo.setAssetsDetail(JSONUtil.toJsonStr(hardware));
            bo.setCheckOutUser(assetUserId);
            bo.setAssetsType("hardware");
            // 记录物料checkOut 记录
            saveCheckOutRecord(bo);
            return;
        }
        //资产借出成功
        bo.setMessage("资产借出成功" + "领用人：" + applicant + " 变更到：" + assetUserId);
        bo.setStatus("1");
        bo.setCheckType("1");
        bo.setAssetsDetail(JSONUtil.toJsonStr(hardware));
        bo.setCheckOutUser(assetUserId);
        bo.setAssetsType("hardware");
        // 记录物料checkOut 记录
        saveCheckOutRecord(bo);

    }

    // 处理附属品
    private void processAccessories(TaskNodeDataBo taskNode, List<Map<String, Object>> accessories){
        if (CollectionUtils.isEmpty(accessories)) return;
        String assetUserId = Convert.toStr(taskNode.getAssetUserId());//申请人资产系统ID
        String applicant = taskNode.getApplicant();
        for (int j = 0; j < accessories.size(); j++) {
            DcwsAssetsCheckOutBo recordBo = createBaseRecord(taskNode, "accessories");
            Map<String, Object> accessory = accessories.get(j);
            //附属品要分配的人列表，默认一人一个附属品
            List<Map<String, Object>> recipients = (List<Map<String, Object>>) accessories.get(j).get("recipient");
            //未设置领用人
            if (ObjectUtil.isEmpty(recipients)) {
                recordBo.setMessage("未设置领用人");
                recordBo.setCheckType("1");
                recordBo.setAssetsDetail(JSONUtil.toJsonStr(accessory));
                recordBo.setAssetsType("accessories");
                // 记录物料checkOut 记录
                saveCheckOutRecord(recordBo);
                continue;
            }
            //查询附属品在资产系统checkout记录，用于变更领用人
            List<String> checkOutIds = queryAccessoriesChecks(assetUserId, accessory, recordBo);
            if(checkOutIds==null) continue;

            for (int i = 0; i < recipients.size(); i++) {
                Map<String, Object> recipient = recipients.get(i);
                if (i+1 > checkOutIds.size()) {
                    recordBo.setMessage("申请人的checkOut数量不足：" + checkOutIds.size() + " 分配数量：" + i);
                    recordBo.setCode("");
                    recordBo.setStatus("0");
                    recordBo.setCheckType("4");
                    recordBo.setCheckOutUser(Convert.toStr(recipient.get("assetUserId")));
                    recordBo.setAssetsDetail(JSONUtil.toJsonStr(accessories.get(j)));
                    recordBo.setAssetsType("accessories");
                    // 记录物料checkOut 记录
                    saveCheckOutRecord(recordBo);
                    continue;
                }
                String accessoryUserId = checkOutIds.get(i);
                // 附属品归还操作
                if(!processAccessoriesCheckIn(accessoryUserId, accessory,recipient, recordBo)) continue;
                // 附属品借出操作
                processAccessoriesCheckOut(accessoryUserId,applicant, accessory, recipient,  recordBo);
            }

        }
    }

    //查询附属品在资产系统checkout记录，用于变更领用人
    private List<String> queryAccessoriesChecks(String assetUserId, Map<String, Object> accessory, DcwsAssetsCheckOutBo bo){
        // 查询附属品借出记录列表
        String accessoryId = Convert.toStr(accessory.get("id")); //附属品id
        String apiUrl = "accessories/" + accessoryId + "/checkedout";
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("limit","9999");
        requestMap.put("offset","140");
        requestMap.put("order","asc");
        Map<String, Object> responseMap = null;
        try {
            responseMap = instance.process(requestMap, apiUrl, "get");
        } catch (ApiCallException e) {
            bo.setStatus("0");//失败
            if (e.getCode() > 0 && e.getCode() != HttpStatus.SUCCESS) {
                bo.setStatus("2");//失败待处理:网络或者权限或者接口url原因导致失败的需要重新发请求处理
            }
            bo.setMessage(e.getMessage());
            bo.setCode(Convert.toStr(e.getCode()));
            bo.setCheckType("3");
            bo.setAssetsDetail(JSONUtil.toJsonStr(accessory));
            bo.setAssetsType("accessories");
            // 记录物料checkOut 记录
            saveCheckOutRecord(bo);
            return null;
        }
        if ("error".equals(responseMap.get("status"))) {
            log.info("后台API接口返回错误：" + responseMap.get("messages"));
            bo.setMessage(Convert.toStr(responseMap.get("messages")));
            bo.setCode(Convert.toStr(responseMap.get("status")));
            bo.setStatus("2");
            bo.setCheckType("3");
            bo.setAssetsDetail(JSONUtil.toJsonStr(accessory));
            bo.setAssetsType("accessories");
            // 记录物料checkOut 记录
            saveCheckOutRecord(bo);
            return null;
        }
        List<Map<String, Object>> rows = (List<Map<String, Object>>) responseMap.get("rows");
        //筛选出申请人的checkOut记录，This is the ID of the accessory+user relationships in the accessories_users table
        return rows.stream().filter(r->Convert.toStr(((Map<String, Object>) r.get("assigned_to")).get("id")).equals(assetUserId))
                .map(a -> Convert.toStr(a.get("id")))
                .collect(Collectors.toList());

    }
    // 附属品归还操作
    private boolean processAccessoriesCheckIn(String accessoryUserId,Map<String, Object> accessory,Map<String, Object> recipient, DcwsAssetsCheckOutBo bo){
            //1、归还
            String apiUrlCheckIn = "accessories/" + accessoryUserId + "/checkin";
            Map<String, Object> responseMap = null;
            try {
                responseMap = instance.process(null, apiUrlCheckIn, "post");
            } catch (ApiCallException e) {
                bo.setStatus("0");//失败
                if (e.getCode() > 0 && e.getCode() != HttpStatus.SUCCESS) {
                    bo.setStatus("2");//失败待处理:网络或者权限或者接口url原因导致失败的需要重新发请求处理
                }
                bo.setMessage(e.getMessage());
                bo.setCode(Convert.toStr(e.getCode()));
                bo.setCheckType("2");
                bo.setAccessoryUserId(accessoryUserId);
                bo.setCheckOutUser(Convert.toStr(recipient.get("assetUserId")));
                bo.setAssetsDetail(JSONUtil.toJsonStr(accessory));
                bo.setAssetsType("accessories");
                // 记录物料checkOut 记录
                saveCheckOutRecord(bo);
                return false;
            }
            if ("error".equals(responseMap.get("status"))) {
                log.info("后台API接口返回错误：" + responseMap.get("messages"));
                bo.setMessage(Convert.toStr(responseMap.get("messages")));
                bo.setCode(Convert.toStr(responseMap.get("status")));
                bo.setStatus("0");
                bo.setCheckType("2");
                bo.setAccessoryUserId(accessoryUserId);
                bo.setCheckOutUser(Convert.toStr(recipient.get("assetUserId")));
                bo.setAssetsDetail(JSONUtil.toJsonStr(accessory));
                bo.setAssetsType("accessories");
                // 记录物料checkOut 记录
                saveCheckOutRecord(bo);
                return false;
            }
            //归还成功
            bo.setStatus("1");
            bo.setCheckType("2");
            bo.setAccessoryUserId(accessoryUserId);
            bo.setCheckOutUser(Convert.toStr(recipient.get("assetUserId")));
            bo.setMessage("附属品归还成功accessoryUserIdIn: " + accessoryUserId);
            bo.setAssetsDetail(JSONUtil.toJsonStr(accessory));
            saveCheckOutRecord(bo);
            return true;
    }
    // 附属品借出操作
    private void processAccessoriesCheckOut(String accessoryUserIdIn,String applicant,Map<String, Object> accessory,Map<String, Object> recipient, DcwsAssetsCheckOutBo bo){
        //2、借出
        String apiUrlOut = "accessories/" + accessory.get("id") + "/checkout";
        Map<String, String> requestBodyMapOut = new HashMap<>();
        requestBodyMapOut.put("assigned_user", Convert.toStr(recipient.get("assetUserId"))); //领用人Id"4"
        requestBodyMapOut.put("note", "领用人：" + applicant + " 变更到：" + recipient.get("name"));
        Map<String, Object> responseMapOut = null;
        try {
            responseMapOut = instance.process(requestBodyMapOut, apiUrlOut, "post");
        } catch (ApiCallException e) {
            bo.setStatus("0");//失败
            if (e.getCode() > 0 && e.getCode() != HttpStatus.SUCCESS) {
                bo.setStatus("2");//失败待处理:网络或者权限或者接口url原因导致失败的需要重新发请求处理
            }
            bo.setMessage(e.getMessage());
            bo.setCode(Convert.toStr(e.getCode()));
            bo.setCheckType("1");
            bo.setAccessoryUserId(accessoryUserIdIn);
            bo.setCheckOutUser(Convert.toStr(recipient.get("assetUserId")));
            bo.setAssetsDetail(JSONUtil.toJsonStr(accessory));
            bo.setAssetsType("accessories");
            // 记录物料checkOut 记录
            saveCheckOutRecord(bo);
            return;
        }
        if ("error".equals(responseMapOut.get("status"))) {
            log.info("后台API接口返回错误：" + responseMapOut.get("messages"));
            bo.setMessage(Convert.toStr(responseMapOut.get("messages")));
            bo.setCode(Convert.toStr(responseMapOut.get("status")));
            bo.setStatus("0");
            bo.setCheckType("1");
            bo.setAccessoryUserId(accessoryUserIdIn);
            bo.setCheckOutUser(Convert.toStr(recipient.get("assetUserId")));
            bo.setAssetsDetail(JSONUtil.toJsonStr(accessory));
            bo.setAssetsType("accessories");
            // 记录物料checkOut 记录
            saveCheckOutRecord(bo);
            return;
        }
        //借出成功
        bo.setStatus("1");
        bo.setCheckType("1");
        bo.setAccessoryUserId(accessoryUserIdIn);
        bo.setCheckOutUser(Convert.toStr(recipient.get("assetUserId")));
        bo.setMessage("领用人：" + applicant + " 变更到：" + recipient.get("name"));
        bo.setAssetsDetail(JSONUtil.toJsonStr(accessory));
        saveCheckOutRecord(bo);
    }
    // 统一校验方法
    private boolean validateRecipient(Map<String, Object> recipient, DcwsAssetsCheckOutBo recordBo) {
        if (ObjectUtil.isEmpty(recipient) || StrUtil.isBlank(Convert.toStr(recipient.get("assetUserId")))) {
            recordBo.setMessage("未设置领用人");
            return false;
        }
        return true;
    }
    // 创建基础记录对象
    private DcwsAssetsCheckOutBo createBaseRecord(TaskNodeDataBo taskNode, String assetType) {
        DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
        bo.setTaskNodeDataId(taskNode.getId());
        bo.setAssetsType(assetType);
        bo.setCheckOutIn("2");
        bo.setStatus("1");
        return bo;
    }
    // 统一保存记录
    private void saveCheckOutRecord(DcwsAssetsCheckOutBo bo) {
        try {
            iAssetsCheckOutRecordService.insertByBo(bo);
            if(bo.getId()==null){
                log.info("物料申请借出记录新增失败：id == null");
            }
            bo.setId(null);
        } catch (Exception e) {
            log.error("物料申请借出记录新增失败", e);
        }
    }
    // 数据解析方法
    private TaskNodeDataBo parseTaskNodeData(Map<String, Object> variables) throws JsonProcessingException {
        Object entity = variables.get("entity");
        if (entity == null) return null;

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(JSON.toJSONString(entity), TaskNodeDataBo.class);
    }


}
