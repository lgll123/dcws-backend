package com.formssi.workflow.externalsystem.assets.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.constant.HttpStatus;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.utils.SpringUtils;
import com.formssi.workflow.domain.bo.DcwsAssetsCheckOutBo;
import com.formssi.workflow.externalsystem.assets.strategy.IExternalSystemAPIStrategy;
import com.formssi.workflow.externalsystem.exception.ApiCallException;
import com.formssi.workflow.service.IAssetsCheckOutRecordService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.impl.el.FixedValue;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.formssi.workflow.externalsystem.assets.constant.AssetsConstant.ASSETS_STATUS_12;

@Slf4j
@Component("CallAssetsSystemCheckOutTaskListener")
public class CallAssetsSystemCheckOutTaskListener implements TaskListener {
    private static final IAssetsCheckOutRecordService assetsCheckOutRecordService = SpringUtils.getBean(IAssetsCheckOutRecordService.class);
    private static final IExternalSystemAPIStrategy instance = SpringUtils.getBean("assets" + IExternalSystemAPIStrategy.BASE_NAME);
    private FixedValue ITClear;

    @Override
    public void notify(DelegateTask delegateTask) {
        Map<String, Object> variables = delegateTask.getVariables();
        try {
            Object entity = variables.get("entity");
            if (ObjectUtil.isEmpty(entity)) return;
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> taskNodeData = (Map<String, Object>)mapper.readValue(JSONUtil.toJsonStr(entity), Map.class);
            if (ObjectUtil.isEmpty(taskNodeData)) return;
            Map<String ,Object> map = mapper.readValue(Convert.toStr(taskNodeData.get("applyDetail")), Map.class);
            // 统一处理checkout 集合数据,根据isIT 是否IT资产（0否1是），ITClear （0否1是）在流程里的监听器（IT澄清节点和行政澄清节点）配置参数值和isIT对应
            // IT澄清节点 checkout IT资产,行政澄清节点checkout 非IT资产
            List<Map<String, Object>> hardware = selectByIsIT(map.get("hardware"));//资产-hardware
            List<Map<String, Object>> licenses = selectByIsIT(map.get("licenses"));//许可证-licenses
            List<Map<String, Object>> accessories = selectByIsIT(map.get("accessories"));//附属品-accessories
            List<Map<String, Object>> components = selectByIsIT(map.get("components"));//组件-components
            List<Map<String, Object>> consumables = selectByIsIT(map.get("consumables"));//消耗品-consumables
            Long assetUserId = Convert.toLong(taskNodeData.get("assetUserId"));// 资产系统用户ID
            String taskNodeDataId = Convert.toStr(taskNodeData.get("id"));

            // 资产-hardware 借出
            if(!ObjectUtil.isEmpty(hardware)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                requestBodyMap.put("checkout_to_type", "user");
                requestBodyMap.put("assigned_user", Convert.toStr(assetUserId));
                checkOutByCategoryType(hardware, assetUserId, taskNodeDataId, requestBodyMap, "hardware");
            }
            // 附属品-accessories 借出
            if (!CollectionUtil.isEmpty(accessories)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                requestBodyMap.put("assigned_user", Convert.toStr(assetUserId));
                checkOutByCategoryType(accessories, assetUserId, taskNodeDataId, requestBodyMap, "accessories");
            }
            // 组件-components 借出
            if (!CollectionUtil.isEmpty(components)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                checkOutByCategoryType(components, assetUserId, taskNodeDataId, requestBodyMap, "components");
            }
            // 消耗品-consumables 借出
            if (!CollectionUtil.isEmpty(consumables)) {
                Map<String, String> requestBodyMap = new HashMap<>();
                requestBodyMap.put("assigned_to", Convert.toStr(assetUserId));
                checkOutByCategoryType(consumables, assetUserId, taskNodeDataId, requestBodyMap, "consumables");
            }
            //许可证-licenses 借出 可以借出到人或资产
            if (!CollectionUtil.isEmpty(licenses)) {
                checkOutLicenses(licenses, assetUserId, taskNodeDataId);
            }
    } catch(Exception e)
    {
        log.error("An error occurred while calling the external system", e);
        Map<String, Object> entityMap = (Map<String, Object>) variables.get("entity");
        DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
        bo.setTaskNodeDataId(entityMap.get("id").toString());
        bo.setMessage(e.getMessage());
        bo.setAssetsDetail(String.valueOf(Convert.toStr(entityMap.get("applyDetail"))));
        bo.setCheckOutUser(String.valueOf(entityMap.get("applicantId")));
        bo.setStatus("3");//部分或全部异常待处理
        bo.setCheckType("1");
        bo.setAssetsType("0");
        bo.setCheckOutIn("1");
        // 记录物料checkOut 记录
        saveCheckOutRecord(bo);
    }
}

    //附属品-accessories、组件-components、消耗品-consumables、资产-hardware 借出
    private void checkOutByCategoryType(List<Map<String, Object>> assets,Long applicantId,String taskNodeDataId,Map<String, String> requestBodyMap,String categoryType){
        DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
        bo.setAssetsType(categoryType);
        bo.setTaskNodeDataId(taskNodeDataId);
        bo.setCheckOutUser(applicantId.toString());
        bo.setAutoHandleNum(0);
        bo.setStatus("1");//成功
        bo.setCheckOutIn("1");
        bo.setCheckType("1");
        for (int i = 0; i < assets.size(); i++) {
            bo.setAssetsDetail(JSONUtil.toJsonStr(assets.get(i)));
            Integer id = (Integer) assets.get(i).get("id");
            String url = categoryType+"/"+id+"/checkout";
            try {
                switch (categoryType){
                    case "hardware":
                        requestBodyMap.put("status_id",  ASSETS_STATUS_12);//已预定
                        break;
                    case "consumables":
                        requestBodyMap.put("checkout_qty",  String.valueOf(assets.get(i).get("applyNum")));
                        break;
                    case "components":
                        //User ID of an asset to check a component out to 资产ID(组件需要借出到资产下面)
                        Map<String, Object> asset = (Map<String, Object>) assets.get(i).get("asset");
                        if(CollectionUtil.isEmpty(asset)|| asset.get("id")==null){
                            throw new ServiceException("组件借出的资产id为空");
                        }
                        requestBodyMap.put("assigned_to",  String.valueOf(asset.get("id")));
                        requestBodyMap.put("assigned_qty",  String.valueOf(assets.get(i).get("applyNum")));
                        break;
                    case "accessories":
                        requestBodyMap.put("checkout_qty",  String.valueOf(assets.get(i).get("applyNum")));
                        break;
                    default:
                        break;
                }

                Map<String, Object> responseMap = instance.process(requestBodyMap,url,"post");
                bo.setMessage(String.valueOf(responseMap.get("messages")));
                bo.setCode(String.valueOf(responseMap.get("status")));
                if("error".equals(responseMap.get("status"))){
                    log.info("资产系统API接口返回错误：" + responseMap.get("messages"));
                    bo.setStatus("0");//失败
                    bo.setCheckType("1");
                }
            } catch (ApiCallException e) {
                bo.setStatus("0");//失败
                if(e.getCode()>0 && e.getCode()!=HttpStatus.SUCCESS){
                    bo.setStatus("2");//失败待处理:网络或者权限或者接口url原因导致失败的需要重新发请求处理
                }
                bo.setCode(String.valueOf(e.getCode()));
                bo.setMessage(e.getMessage());
            } catch (ServiceException e) {
                bo.setStatus("0");//失败
                bo.setMessage(e.getMessage());
            }
            // 记录物料checkOut 记录
            saveCheckOutRecord(bo);
        }
    }

    //许可证-licenses 借出
    private void checkOutLicenses(List<Map<String, Object>> licenses,Long applicantId,String taskNodeDataId){
        DcwsAssetsCheckOutBo bo = new DcwsAssetsCheckOutBo();
        bo.setAssetsType("licenses");
        bo.setTaskNodeDataId(taskNodeDataId);
        bo.setCheckOutUser(applicantId.toString());
        bo.setAutoHandleNum(0);
        bo.setStatus("1");//成功
        bo.setCheckType("1");
        bo.setCheckOutIn("1");
        bo.setMessage("许可证借出成功");
        for (int i = 0; i < licenses.size(); i++) {
            bo.setAssetsDetail(JSONUtil.toJsonStr(licenses.get(i)));
            Integer id = (Integer) licenses.get(i).get("id");
            try {
                Map<String, Object> responseMap = instance.process(null,"licenses/"+id+"/seats","get");
                if("error".equals(responseMap.get("status"))){
                    log.info("后台API接口返回错误：" + responseMap.get("messages"));
                    bo.setMessage(responseMap.get("messages").toString());
                    bo.setCode(responseMap.get("status").toString());
                    bo.setStatus("0");//失败
                    bo.setCheckType("3");
                    // 记录物料checkOut 记录
                    saveCheckOutRecord(bo);
                    continue;
                }
                List<Map<String,Object>> maps = (List<Map<String,Object>>)responseMap.get("rows");
                List<Integer> seatIds = maps.stream().filter(l -> l.get("assigned_user") == null && l.get("location") == null)
                        .map(seat -> (Integer) seat.get("id")).toList();
                if(ObjectUtil.isEmpty(seatIds)){
                    bo.setMessage("licenses可用库存不足，seatIds is null ");
                    bo.setStatus("0");//失败
                    bo.setCheckType("4");
                    // 记录物料checkOut 记录
                    saveCheckOutRecord(bo);
                    continue;
                }
                List<Map<String, Object>> assets = (List<Map<String, Object>>) licenses.get(i).get("asset");
                if(CollectionUtil.isEmpty(assets)){
                    log.info("licenses借出绑定资产列表为空,taskNodeDataId：{}", taskNodeDataId);
                }
                Integer applyNum = (Integer) licenses.get(i).get("applyNum");
                if(ObjectUtil.isEmpty(applyNum)){
                    bo.setMessage("applyNum is null");
                    bo.setStatus("0");//失败
                    bo.setCheckType("4");
                    // 记录物料checkOut 记录
                    saveCheckOutRecord(bo);
                    continue;
                }
                if(applyNum ==0 || applyNum > seatIds.size()){
                    log.info("taskNodeDataId：{} licenses借出数量applyNum:{} 剩余席位数量seatIds:{}",taskNodeDataId, applyNum, seatIds.size());
                }
                for (int j = 0; j < applyNum; j++) {
                    try {
                        if(j >= seatIds.size()) break;
                        Map<String, String> requestBodyMap = new HashMap<>();
                        requestBodyMap.put("seat_id", Convert.toStr(seatIds.get(j)));
                        requestBodyMap.put("assigned_to", Convert.toStr(applicantId));
                        if(!ObjectUtil.isEmpty(assets) && j < assets.size()){
                            requestBodyMap.put("asset_id", Convert.toStr(assets.get(j).get("id")));
                        }
                        Map<String, Object> responseMap1 = instance.process(requestBodyMap,"licenses/"+id+"/seats/"+seatIds.get(j),"put");
                        if("error".equals(responseMap1.get("status"))){
                            log.info("资产系统API接口返回错误：" + responseMap1.get("messages"));
                            bo.setMessage(responseMap1.get("messages").toString());
                            bo.setCode(responseMap1.get("status").toString());
                            bo.setStatus("0");//失败
                            bo.setCheckType("1");
                        }
                    } catch (ApiCallException e) {
                        bo.setStatus("0");//失败
                        if(e.getCode()>0 && e.getCode()!=HttpStatus.SUCCESS){
                            bo.setStatus("2");//失败待处理:网络或者权限或者接口url原因导致失败的需要重新发请求处理
                        }
                        bo.setCode(String.valueOf(e.getCode()));
                        bo.setMessage(e.getMessage());
                    }
                    // 记录物料checkOut 记录
                    saveCheckOutRecord(bo);
                }

            } catch (Exception e) {
                bo.setStatus("0");//失败
                bo.setMessage(e.getMessage());
                // 记录物料checkOut 记录
                saveCheckOutRecord(bo);
            }

        }
    }

    // 统一处理checkout 集合数据,根据isIT 是否IT资产（0否1是），ITClear （0否1是）在流程里的监听器（IT澄清节点和行政澄清节点）配置参数值和isIT对应
    // IT澄清节点 checkout IT资产,行政澄清节点checkout 非IT资产
    private List<Map<String,Object>> selectByIsIT(Object dataList) {
        return  Optional.ofNullable(dataList)
            .map(obj -> (List<Map<String, Object>>) obj)
            .orElse(Collections.emptyList())
                .stream()
                .filter(m -> ITClear.getExpressionText().equals(Convert.toStr(m.get("isIT")))).toList();
    }

    // 统一保存记录
    private void saveCheckOutRecord(DcwsAssetsCheckOutBo bo) {
        try {
            assetsCheckOutRecordService.insertByBo(bo);
            if(bo.getId()==null){
                log.info("物料申请借出记录新增失败：id == null");
            }
            bo.setId(null);
        } catch (Exception e) {
            log.error("物料申请借出记录新增失败", e);
        }
    }
    public FixedValue getITClear() {
        return ITClear;
    }
    public void setITClear(FixedValue ITClear) {
        this.ITClear = ITClear;
    }
}
