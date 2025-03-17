package com.formssi.workflow.service.strategy;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.service.UserService;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.workflow.domain.vo.DcwsActHistoryInfoVo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.externalsystem.assets.service.UploadFileServerService;
import com.formssi.workflow.service.DcwsIActProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * dcws 用印申请申请单生成PDF数据处理接口策略
 *
 * @author yqh
 */
@Slf4j
@Service("seal")
@RequiredArgsConstructor
public class DcwsSealApplyFilePDFCreateStrategy implements DcwsApplyFilePDFCreateStrategy<TaskNodeDataVo> {
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
            data.put("completeDate",taskNodeDataVo.getCompleteDate());//使用日期
            data.put("status","已完成");//当前环节 TODO
            data.put("applyReson",taskNodeDataVo.getApplyReson());//申请原因
            data.put("applyRemarks",taskNodeDataVo.getApplyRemarks());//备注
            String applyDetail = taskNodeDataVo.getApplyDetail();
            List<Map<String,Object>> applyDetails = objectMapper.readValue(JSONUtil.toJsonStr(applyDetail), List.class);
            data.put("applyDetails", applyDetails);

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
            String applyType = taskNodeDataVo.getApplyType();//19 IT 21 非IT 22 用印
            if("22".equals(applyType)){
                documentTypeId="3";
                storagePathId="4";
                tags=new String[]{"8"};
                objName="用印申请-";
            }
            Map<String, Object> documentServerParam = new HashMap();
            documentServerParam.put("documentTypeId",documentTypeId);
            documentServerParam.put("storagePathId",storagePathId);
            documentServerParam.put("tags",tags);
            String fileName = objName + taskNodeDataVo.getId() + ".pdf";
            resultMap.put("fileName",fileName);
            resultMap.put("pdfBytes",pdfBytes);
            resultMap.put("documentServerParam",documentServerParam);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return resultMap;
    }
}
