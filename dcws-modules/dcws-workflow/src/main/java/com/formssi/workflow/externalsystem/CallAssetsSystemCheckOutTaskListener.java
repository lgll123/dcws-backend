package com.formssi.workflow.externalsystem;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.constant.HttpStatus;
import com.formssi.common.core.exception.ApiCallException;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.DcwsAssetsCheckOutBo;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
//import com.formssi.workflow.externalsystem.converpdf.ExcelToPDFConverter;
import com.formssi.workflow.service.IAssetsCheckOutRecordService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CallAssetsSystemCheckOutTaskListener implements TaskListener {
    private static  final IAssetsCheckOutRecordService iAssetsCheckOutRecordService = SpringUtils.getBean(IAssetsCheckOutRecordService.class);
    private static final IExternalSystemAPIStrategy instance = SpringUtils.getBean("assets" + IExternalSystemAPIStrategy.BASE_NAME);
    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        TaskNodeDataBo taskNodeDataBo = null;
        HashMap<String,Object> hashMap =null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if(variables.get("entity")!=null) {
                taskNodeDataBo = objectMapper.readValue(JSON.toJSONString(variables.get("entity")), TaskNodeDataBo.class);
                //TODO yqh
//                ExcelToPDFConverter.convert("C:\\Users\\forms\\Desktop\\物料申请\\IT类物料申请表 (202410).xlsx", "C:\\Users\\forms\\Desktop\\物料申请\\output-test.pdf",taskNodeDataBo);
                hashMap = objectMapper.readValue(taskNodeDataBo.getApplyDetail(), HashMap.class);
                //附属品-accessories、组件-components、许可证-licenses、消耗品-consumables、资产-hardware
                ArrayList<Map<String, Object>> hardware = (ArrayList<Map<String, Object>>) hashMap.get("hardware");
                ArrayList<Map<String, Object>> licenses = (ArrayList<Map<String, Object>>) hashMap.get("licenses");
                ArrayList<Map<String, Object>> accessories = (ArrayList<Map<String, Object>>) hashMap.get("accessories");
                ArrayList<Map<String, Object>> components = (ArrayList<Map<String, Object>>) hashMap.get("components");
                ArrayList<Map<String, Object>> consumables = (ArrayList<Map<String, Object>>) hashMap.get("consumables");
//                Long applicantId = taskNodeDataBo.getApplicantId();
                Long assetUserId = taskNodeDataBo.getAssetUserId();
                Long taskNodeDataId = taskNodeDataBo.getId();
                // 资产-hardware 借出
                if (!CollectionUtil.isEmpty(hardware)) {
                    Map<String, String> requestBodyMap = new HashMap<>();
                    requestBodyMap.put("checkout_to_type",  "user");
                    requestBodyMap.put("assigned_user",  assetUserId.toString());
                    checkOutByCategoryType(hardware,assetUserId,taskNodeDataId,requestBodyMap,"hardware");
                }
                // 附属品-accessories 借出
                if (!CollectionUtil.isEmpty(accessories)) {
                    Map<String, String> requestBodyMap = new HashMap<>();
                    requestBodyMap.put("assigned_user",  assetUserId.toString());
                    checkOutByCategoryType(accessories,assetUserId,taskNodeDataId,requestBodyMap,"accessories");
                }
                // 组件-components 借出
                if (!CollectionUtil.isEmpty(components)) {
                    Map<String, String> requestBodyMap = new HashMap<>();
                    //User ID of an asset to check a component out to
                    requestBodyMap.put("assigned_to",  assetUserId.toString());
                    checkOutByCategoryType(components,assetUserId,taskNodeDataId,requestBodyMap,"components");
                }
                // 消耗品-consumables 借出
                if (!CollectionUtil.isEmpty(consumables)) {
                    Map<String, String> requestBodyMap = new HashMap<>();
                    requestBodyMap.put("assigned_to",  assetUserId.toString());
                    checkOutByCategoryType(consumables,assetUserId,taskNodeDataId,requestBodyMap,"consumables");
                }
                //许可证-licenses 借出
                if (!CollectionUtil.isEmpty(licenses)) {
                    checkOutLicenses(licenses,assetUserId,taskNodeDataId);
                }
            }
        } catch (Exception e) {
            log.error("An error occurred while calling the external system", e);
            Map<String,Object> entityMap = (Map<String,Object>)variables.get("entity");
            DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
            bo.setTaskNodeDataId(Long.valueOf(entityMap.get("id").toString()));
            bo.setMessage(e.getMessage());
            bo.setAssetsDetail(entityMap.get("applyDetail").toString());
            bo.setCheckOutUser(entityMap.get("applicantId").toString());
            bo.setStatus("2");//部分异常待处理
            bo.setAssetsType("0");
            // 记录物料checkOut 记录
            addAssetsCheckOutRecord(bo,iAssetsCheckOutRecordService);
            // 抛出BPMN错误，触发错误边界事件
//            throw new BpmnError("An error occurred while calling the external system", "调用外部接口失败: " + e.getMessage());
        }
    }
    //记录checkOut 记录
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
    //附属品-accessories、组件-components、消耗品-consumables、资产-hardware 借出
    private static void checkOutByCategoryType(ArrayList<Map<String, Object>> assets,Long applicantId,Long taskNodeDataId,Map<String, String> requestBodyMap,String categoryType){
        for (int i = 0; i < assets.size(); i++) {
            DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
            bo.setAssetsType(categoryType);
            bo.setAssetsDetail(JSON.toJSONString(assets.get(i)));
            bo.setTaskNodeDataId(taskNodeDataId);
            bo.setCheckOutUser(applicantId.toString());
            bo.setAutoHandleNum(0);
            bo.setStatus("1");//成功

            Integer id = (Integer) assets.get(i).get("id");
            String url = categoryType+"/"+id+"/checkout";
            try {
                switch (categoryType){
                    case "hardware":
                        requestBodyMap.put("status_id",  String.valueOf(assets.get(i).get("assetStatusId")));
                        break;
                    case "consumables":
                        requestBodyMap.put("checkout_qty",  String.valueOf(assets.get(i).get("applyNum")));
                        break;
                    case "components":
                        requestBodyMap.put("assigned_qty",  String.valueOf(assets.get(i).get("applyNum")));
                        break;
                    case "accessories":
                        requestBodyMap.put("checkout_qty",  String.valueOf(assets.get(i).get("applyNum")));
                        break;
                    default:
                        break;
                }

                Map<String, Object> responseMap = instance.process(requestBodyMap,url,"post");
                if("error".equals(responseMap.get("status"))){
                    log.info("资产系统API接口返回错误：" + responseMap.get("messages"));
                    bo.setMessage(responseMap.get("messages").toString());
                    bo.setCode(responseMap.get("status").toString());
                    bo.setStatus("0");//失败
                }
            } catch (ApiCallException e) {
                bo.setStatus("0");//失败
                if(e.getCode()>0 && e.getCode()!=HttpStatus.SUCCESS){
                    bo.setStatus("3");//失败待处理:网络或者权限或者接口url原因导致失败的需要重新发请求处理
                }
                bo.setCode(String.valueOf(e.getCode()));
                bo.setMessage(e.getMessage());
            }
            // 记录物料checkOut 记录
            addAssetsCheckOutRecord(bo,iAssetsCheckOutRecordService);
        }
    }

    //许可证-licenses 借出
    private static void checkOutLicenses(ArrayList<Map<String, Object>> licenses,Long applicantId,Long taskNodeDataId){
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
                Integer seatId = maps.stream().filter(l -> l.get("assigned_user") == null && l.get("location") == null)
                        .map(seat ->(Integer) seat.get("id"))
                        .findFirst().orElse(0);
                if(seatId==0){
                    bo.setMessage("licenses可用库存不足 seatId = "+seatId);
                    bo.setStatus("0");//失败
                    // 记录物料checkOut 记录
                    addAssetsCheckOutRecord(bo,iAssetsCheckOutRecordService);
                    continue;
                }
                Map<String, String> requestBodyMap = new HashMap<>();
                requestBodyMap.put("seat_id", seatId.toString());
                requestBodyMap.put("assigned_to", applicantId.toString());

                Map<String, Object> responseMap1 = instance.process(requestBodyMap,"licenses/"+id+"/seats/"+seatId,"put");
                if("error".equals(responseMap1.get("status"))){
                    log.info("资产系统API接口返回错误：" + responseMap1.get("messages"));
                    bo.setMessage(responseMap1.get("messages").toString());
                    bo.setCode(responseMap1.get("status").toString());
                    bo.setStatus("0");//失败
                }
            } catch (ApiCallException e) {
                bo.setStatus("0");//失败
                if(e.getCode()>0 && e.getCode()!=HttpStatus.SUCCESS){
                    bo.setStatus("3");//失败待处理:网络或者权限或者接口url原因导致失败的需要重新发请求处理
                }
                bo.setCode(String.valueOf(e.getCode()));
                bo.setMessage(e.getMessage());
            }
            // 记录物料checkOut 记录
            addAssetsCheckOutRecord(bo,iAssetsCheckOutRecordService);
        }
    }

}
