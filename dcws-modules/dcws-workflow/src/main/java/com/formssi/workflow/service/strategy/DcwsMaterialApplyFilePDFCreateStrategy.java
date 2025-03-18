package com.formssi.workflow.service.strategy;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.service.UserService;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.tenant.helper.TenantHelper;
import com.formssi.workflow.domain.vo.DcwsActHistoryInfoVo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.service.DcwsIActProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.formssi.workflow.externalsystem.assets.constant.AssetsConstant.*;

/**
 * dcws 物料申请申请单生成PDF数据处理接口策略
 *
 * @author yqh
 */
@Slf4j
@Service("material")
@RequiredArgsConstructor
public class DcwsMaterialApplyFilePDFCreateStrategy implements DcwsApplyFilePDFCreateStrategy<TaskNodeDataVo> {
    @Autowired
    private UserService userService;
    @Autowired
    private DcwsIActProcessInstanceService dcwsIActProcessInstanceService;
    @Autowired
    private CreateApplyFilePDFService createApplyFilePDFService;


    @Override
    public Map<String, Object> process(String templateName,TaskNodeDataVo taskNodeDataVo) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Map<String, Object> data = new HashMap<>();
            ObjectMapper objectMapper = new ObjectMapper();
            data.put("id",taskNodeDataVo.getId());//申请编号
            data.put("applyDept",taskNodeDataVo.getApplyDept());//申请部门
            data.put("applicant",taskNodeDataVo.getApplicant());//申请人
            data.put("applyDate",taskNodeDataVo.getApplyDate());//申请日期
            data.put("status","已完成");//当前环节 TODO
            String requiredDateType = taskNodeDataVo.getRequiredDateType();
            String requiredDate = switch (StringUtils.blankToDefault(requiredDateType,"4")) {
                case "1" -> "截止日期  " + DateUtil.format(taskNodeDataVo.getCompleteDate(), "yyyy-MM-dd");
                case "2" -> taskNodeDataVo.getRequiredDesc();
                case "3" -> "尽快";
                default -> null;
            };
            data.put("requiredDateType",requiredDate);//需求日期
            data.put("checkTo",taskNodeDataVo.getCheckTo());//预计使用人
            data.put("applyReson",taskNodeDataVo.getApplyReson());//申请原因
            data.put("applyRemarks",taskNodeDataVo.getApplyRemarks());//备注

            String applyDetail = taskNodeDataVo.getApplyDetail();
            Map<String,Object> applyDetails = objectMapper.readValue(JSONUtil.toJsonStr(applyDetail), Map.class);
            //附属品-accessories、组件-components、许可证-licenses、消耗品-consumables、资产-hardware
            ArrayList<Map<String, Object>> hardware = (ArrayList<Map<String, Object>>) applyDetails.get("hardware");
            ArrayList<Map<String, Object>> licenses = (ArrayList<Map<String, Object>>) applyDetails.get("licenses");
            ArrayList<Map<String, Object>> accessories = (ArrayList<Map<String, Object>>) applyDetails.get("accessories");
            ArrayList<Map<String, Object>> components = (ArrayList<Map<String, Object>>) applyDetails.get("components");
            ArrayList<Map<String, Object>> consumables = (ArrayList<Map<String, Object>>) applyDetails.get("consumables");
            Map<String, Object> purchaseDetail = (Map<String, Object>) applyDetails.get("purchaseDetail");
            String purchase = StringUtils.blankToDefault(Convert.toStr(applyDetails.get("purchase")),"3");
            String purchaseTrans = switch (purchase) {
                case "0" -> "无需采购";
                case "1", "2" -> "需采购";
                default -> null;
            };
            data.put("purchaseTrans",purchaseTrans);//2:部分采购
            data.put("purchase",purchase);//2:部分采购
            data.put(CATEGORIES_HARDWARE, hardware);
            data.put(CATEGORIES_LICENSES, licenses);
            data.put(CATEGORIES_ACCESSORIES, accessories);
            data.put(CATEGORIES_COMPONENTS, components);
            data.put(CATEGORIES_CONSUMABLES, consumables);
            data.put("purchaseDetail", purchaseDetail);
            String customApplyDetail1 = taskNodeDataVo.getCustomApplyDetail();
            List<Map<String,Object>> customApplyDetails = objectMapper.readValue(JSONUtil.toJsonStr(customApplyDetail1), List.class);
            data.put("customApplyDetails", customApplyDetails);
            // 设置动态租户ID,默认000000（审批记录查询接口用到了租户ID）
            TenantHelper.setDynamic(StringUtils.blankToDefault(taskNodeDataVo.getTenantId(),"000000"));
            // 审批记录
            List<DcwsActHistoryInfoVo> historyRecords = dcwsIActProcessInstanceService.getHistoryRecord(taskNodeDataVo.getId());
            // 查询审批人昵称名称
            List<DcwsActHistoryInfoVo> collect = historyRecords.stream()
                    .map(h -> {
                        if(!StringUtils.isEmpty(h.getAssignee())){
                            h.setNickName(userService.selectNicknameById(Convert.toLong(h.getAssignee())));
                        }else {
                            h.setNickName("无");
                        }
                        if(StringUtils.isEmpty(h.getComment())){
                            h.setComment("无");
                        }
                        if(StringUtils.isEmpty(h.getStatusName())){
                            h.setStatusName("通过");
                        }
                        return h;
                    }).toList();
            data.put("historyRecords", collect);

            // 生成PDF
            byte[] pdfBytes = createApplyFilePDFService.generatePdf(templateName, data);
            String documentTypeId=null;
            String storagePathId=null;
            String[] tags=null;
            String objName=null;
            String applyType = taskNodeDataVo.getApplyType();//19 IT 21 非IT
            if("19".equals(applyType)){
                documentTypeId="3";//TODO 需要维护
                storagePathId="2";
                tags=new String[]{"6"};
                objName="IT物料申请-";
            }
            if("21".equals(applyType)){
                documentTypeId="3";
                storagePathId="3";
                tags=new String[]{"7"};
                objName="非IT物料申请-";
            }
            Map<String, Object> documentServerParam = MapUtil.createMap(HashMap.class);
            documentServerParam.put(DOCUMENTTYPEID,documentTypeId);//档案系统文件类型
            documentServerParam.put(STORAGEPATHID,storagePathId);//档案系统文件路径
            documentServerParam.put(TAGS,tags);//档案系统文件标签
            String fileName = objName + taskNodeDataVo.getId() + ".pdf";
            resultMap.put(FILENAME,fileName);
            resultMap.put(PDFBYTES,pdfBytes);
            resultMap.put(DOCUMENTSERVERPARAM,documentServerParam);
        } catch (Exception e) {
            log.error("生成PDF失败:{}",e.getMessage());
            throw new RuntimeException(e);
        } finally {
            // 清除动态租户ID
            TenantHelper.clearDynamic();
        }
        return resultMap;
    }
}
