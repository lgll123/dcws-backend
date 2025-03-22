package com.formssi.workflow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.formssi.common.core.domain.event.ProcessEvent;
import com.formssi.common.core.domain.event.ProcessTaskEvent;
import com.formssi.common.core.enums.BusinessStatusEnum;
import com.formssi.common.core.exception.ServiceException;
import com.formssi.common.core.service.WorkflowService;
import com.formssi.common.core.utils.MapstructUtils;
import com.formssi.common.core.utils.StreamUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.minio.util.MinioUtil;
import com.formssi.common.mybatis.core.domain.BaseEntity;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.system.domain.SealInfo;
import com.formssi.system.domain.vo.SealJsonVo;
import com.formssi.system.domain.vo.SysFileVo;
import com.formssi.system.service.ISysFileService;
import com.formssi.workflow.common.enums.ApplyTypeEnum;
import com.formssi.workflow.domain.DcwsSysFile;
import com.formssi.workflow.domain.TaskNodeData;
import com.formssi.workflow.domain.TaskNodeDataHis;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.bo.TaskNodeDataQueryBo;
import com.formssi.workflow.domain.vo.DcwsInvoiceVo;
import com.formssi.workflow.domain.vo.DcwsSysFileVo;
import com.formssi.workflow.domain.vo.TaskNodeDataHisVo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import com.formssi.workflow.mapper.DcwsSysFileMapper;
import com.formssi.workflow.mapper.TaskNodeDataHisMapper;
import com.formssi.workflow.mapper.TaskNodeDataMapper;
import com.formssi.workflow.service.IApplyService;
import com.formssi.workflow.service.TaskSerialService;
import com.formssi.workflow.utils.DcwsAiUtils;
import com.formssi.workflow.utils.DcwsDateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.formssi.workflow.common.enums.ApplyTypeEnum.values;
import static com.formssi.workflow.common.enums.StorageFileStatusEnum.STORAGEFILESTATUS_4;

/**
 * 申请Service业务层处理
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class ApplyServiceImpl implements IApplyService {

    private final TaskNodeDataMapper taskNodeDataMapper;
    private final TaskNodeDataHisMapper taskNodeDataHisMapper;
    private final DcwsSysFileMapper dcwsSysFileMapper;
    private final WorkflowService workflowService;
    private final TaskSerialService taskSerialService;
    private static final String keys = "{'non_IT_assets_apply','IT_assets_apply','seal_apply','claim_apply'" +
            ",'data_apply','server_apply','info_apply',','info_change''}.contains(#event.key)";
    private final ISysFileService sysFileService;

    @Autowired
    private MinioUtil minioUtil;

    /**
     * 查询申请
     */
    @Override
    public TaskNodeDataVo queryById(String id) {
        TaskNodeDataVo taskNodeDataVo = taskNodeDataMapper.selectVoById(id);
        if(ApplyTypeEnum.SEAL.getCode().equals(taskNodeDataVo.getApplyType())){
            try {
                //如果是用印申请，则给出当前登录人需要使用哪些印章
                ObjectMapper mapper = new ObjectMapper();
                String data = taskNodeDataVo.getApplyDetail();
                List<SealJsonVo> list = mapper.readValue(data, new TypeReference<>() {});
                List<SealInfo> sealList = new ArrayList<>();
                list.forEach(e ->{
                    sealList.addAll(e.getSealInfoList());
                });
                String userId = String.valueOf(LoginHelper.getUserId());
                String sealNameStr = sealList.stream()
                        .filter(e -> userId.equals(String.valueOf(e.getSealUser())))
                        .map(SealInfo::getSealName)
                        .distinct()
                        .collect(Collectors.joining(","));
                SealJsonVo sealJsonVo = new SealJsonVo();
                sealJsonVo.setSealNameStr(sealNameStr);
                taskNodeDataVo.setSealJsonVo(sealJsonVo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return taskNodeDataVo;
    }

    /**
     * 根据任务ID查询申请表单信息
     */
    @Override
    public TaskNodeDataHisVo queryByTaskId(String taskId){
        LambdaQueryWrapper<TaskNodeDataHis> lqw = Wrappers.lambdaQuery();
        lqw.eq(TaskNodeDataHis::getTaskId, taskId);
        return taskNodeDataHisMapper.selectVoOne(lqw);
    }

    /**
     * 查询申请列表
     */
    @Override
    public TableDataInfo<TaskNodeDataVo> queryPageList(TaskNodeDataQueryBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TaskNodeData> lqw = buildQueryWrapper(bo);
        Page<TaskNodeDataVo> result = taskNodeDataMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询用印台账表单
     */
    @Override
    public TableDataInfo<TaskNodeDataVo> queryPageSealList(TaskNodeDataQueryBo bo, PageQuery pageQuery) {
        List<TaskNodeDataVo> sealInfoList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        //查询已完成的
        bo.setStatus(BusinessStatusEnum.FINISH.getStatus());
        bo.setApplyType(ApplyTypeEnum.SEAL.getCode());
        LambdaQueryWrapper<TaskNodeData> lqw = buildQueryWrapper2(bo);
        List<TaskNodeDataVo> sealList = taskNodeDataMapper.selectVoList(lqw);
        if(!CollectionUtil.isEmpty(sealList)){
            sealList.forEach(e ->{
                String data = e.getApplyDetail();
                if(!StringUtils.isBlank(data)){
                    try {
                        List<SealJsonVo> list = mapper.readValue(data, new TypeReference<>() {});
                        if(!CollectionUtil.isEmpty(list)){
                            list.forEach(i ->{
                                TaskNodeDataVo taskNodeDataVo = new TaskNodeDataVo();
                                BeanUtils.copyProperties(e,taskNodeDataVo);
                                taskNodeDataVo.setSealJsonVo(i);
                                sealInfoList.add(taskNodeDataVo);
                            });
                        }
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            // 分页
            List<TaskNodeDataVo> pageData = paginateData(sealInfoList, pageQuery.getPageNum(), pageQuery.getPageSize());
            // 封装成 TableDataInfo
            TableDataInfo<TaskNodeDataVo> tableDataInfo = new TableDataInfo<>();
            tableDataInfo.setTotal(sealInfoList.size());
            tableDataInfo.setRows(pageData);
            tableDataInfo.setCode(200);
            tableDataInfo.setMsg("查询成功");

            return tableDataInfo;
        }else {
            return TableDataInfo.build();
        }
    }

    public <T> List<T> paginateData(List<T> data, int pageNum, int pageSize) {
        int total = data.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex  + pageSize, total);
        return data.subList(fromIndex,  toIndex);
    }

    /**
     * 查询申请列表
     */
    @Override
    public List<TaskNodeDataVo> queryList(TaskNodeDataQueryBo bo) {
        LambdaQueryWrapper<TaskNodeData> lqw = buildQueryWrapper(bo);
        return taskNodeDataMapper.selectVoList(lqw);
    }

    private LambdaQueryWrapper<TaskNodeData> buildQueryWrapper(TaskNodeDataQueryBo bo) {
        LambdaQueryWrapper<TaskNodeData> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getApplyDate()!=null, TaskNodeData::getApplyDate, bo.getApplyDate());
        lqw.like(StringUtils.isNotBlank(bo.getApplyDept()), TaskNodeData::getApplyDept, bo.getApplyDept());
        lqw.like(StringUtils.isNotBlank(bo.getApplicant()), TaskNodeData::getApplicant, bo.getApplicant());
        lqw.eq(StringUtils.isNotBlank(bo.getApplyType()), TaskNodeData::getApplyType, bo.getApplyType());
        lqw.in(TaskNodeData::getStatus, Arrays.asList("back","draft"));
        lqw.eq(TaskNodeData::getCreateBy, LoginHelper.getUserId());
        lqw.orderByDesc(BaseEntity::getCreateTime);
        return lqw;
    }

    private LambdaQueryWrapper<TaskNodeData> buildQueryWrapper2(TaskNodeDataQueryBo bo) {
        LambdaQueryWrapper<TaskNodeData> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getApplyDate()!=null, TaskNodeData::getApplyDate, bo.getApplyDate());
        lqw.like(StringUtils.isNotBlank(bo.getApplyDept()), TaskNodeData::getApplyDept, bo.getApplyDept());
        lqw.like(StringUtils.isNotBlank(bo.getApplicant()), TaskNodeData::getApplicant, bo.getApplicant());
        lqw.eq(StringUtils.isNotBlank(bo.getApplyType()), TaskNodeData::getApplyType, bo.getApplyType());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), TaskNodeData::getStatus, bo.getStatus());
        lqw.orderByDesc(BaseEntity::getCreateTime);
        return lqw;
    }

    /**
     * 新增申请
     */
    @Override
    public TaskNodeDataVo insertByBo(TaskNodeDataBo bo) {
        TaskNodeData add = MapstructUtils.convert(bo, TaskNodeData.class);
        if (StringUtils.isBlank(add.getStatus())) {
            add.setStatus(BusinessStatusEnum.DRAFT.getStatus());
        }
        String id = taskSerialService.getTaskSerial(bo.getApplyType(), DcwsDateUtils.dateTime());
        add.setId(id);
        boolean flag = taskNodeDataMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return MapstructUtils.convert(add, TaskNodeDataVo.class);
    }

    /**
     * 修改申请
     */
    @Override
    public TaskNodeDataVo updateByBo(TaskNodeDataBo bo) {
        TaskNodeData update = MapstructUtils.convert(bo, TaskNodeData.class);
        taskNodeDataMapper.updateById(update);
        return MapstructUtils.convert(update, TaskNodeDataVo.class);
    }

    /**
     * 批量删除申请
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Collection<String> ids) {
        List<String> idList = StreamUtils.toList(ids, String::valueOf);
        workflowService.deleteRunAndHisInstance(idList);
        return taskNodeDataMapper.deleteByIds(ids) > 0;
    }
    /**
     * 查询申请单PDF
     */
    @Override
    public List<DcwsSysFileVo> getApplyPDF(String id){
        LambdaQueryWrapper<DcwsSysFile> lqw = Wrappers.lambdaQuery();
        lqw.eq(id!=null, DcwsSysFile::getTaskNodeDataId, id);
        return dcwsSysFileMapper.selectVoList(lqw);
    }

    /**
     * 总体流程监听(例如: 提交 退回 撤销 终止 作废等)
     * 正常使用只需#processEvent.key=='leave1'
     * 示例为了方便则使用startsWith匹配了全部示例key
     *
     * @param event 参数
     */
    @EventListener(condition = keys)
    public void processHandler(ProcessEvent event) {
        log.info("当前任务执行了{}", event.toString());
        TaskNodeData taskNodeData = taskNodeDataMapper.selectById(event.getBusinessKey());
        taskNodeData.setStatus(event.getStatus());
        if (event.isSubmit()) {
            taskNodeData.setStatus(event.getStatus());
        }
        taskNodeDataMapper.updateById(taskNodeData);
    }

    /**
     * 执行办理任务监听
     * 示例：也可通过  @EventListener(condition = "#processTaskEvent.key=='leave1'")进行判断
     * 在方法中判断流程节点key
     * if ("xxx".equals(processTaskEvent.getTaskDefinitionKey())) {
     * //执行业务逻辑
     * }
     *
     * @param event 参数
     */
    @EventListener(condition = keys)
    public void processTaskHandler(ProcessTaskEvent event) {
        log.info("当前任务执行了{}", event.toString());
        TaskNodeData taskNodeData = taskNodeDataMapper.selectById(event.getBusinessKey());
        taskNodeData.setStatus(BusinessStatusEnum.WAITING.getStatus());
        Map<String,Object> taskNodeDataMap = new HashMap<>();
        if (CollUtil.isNotEmpty(event.getVariables())) {
            Map<String, Object> variables = event.getVariables();
            Object entity = variables.get("entity");
            if(variables.get("entity")!=null){
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    taskNodeDataMap = (Map<String,Object>)objectMapper.readValue(JSON.toJSONString(entity), Map.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        taskNodeData.setApplyDetail(Convert.toStr(taskNodeDataMap.get("applyDetail")));
        taskNodeData.setTaskId(event.getTaskId());
        taskNodeDataMapper.updateById(taskNodeData);
        QueryWrapper<TaskNodeDataHis> query = Wrappers.query();
        query.eq("task_id",event.getTaskId());
        TaskNodeDataHisVo taskNodeDataHisVo = taskNodeDataHisMapper.selectVoOne(query);
        taskNodeDataMap.put("taskNodeDataId",taskNodeData.getId());
        if(taskNodeDataHisVo!=null){
            TaskNodeDataHis taskNodeDataHis = MapstructUtils.convert(taskNodeDataHisVo, TaskNodeDataHis.class);
            taskNodeDataHis.setApplyDetail(Convert.toStr(taskNodeDataMap.get("applyDetail")));
            taskNodeDataHis.setStatus(taskNodeData.getStatus());
            taskNodeDataHisMapper.updateById(taskNodeDataHis);
        }else {
            TaskNodeDataHis taskNodeDataHis = new TaskNodeDataHis();
            taskNodeDataHis.setApplicant(Convert.toStr(taskNodeDataMap.get("applicant")));
            taskNodeDataHis.setApplicantId(Convert.toLong(taskNodeDataMap.get("applicantId")));
            taskNodeDataHis.setAssetUserId(Convert.toLong(taskNodeDataMap.get("assetUserId")));
            taskNodeDataHis.setApplyDate(DateUtil.parse(Convert.toStr(taskNodeDataMap.get("applyDate"))));
            taskNodeDataHis.setApplyDetail(Convert.toStr(taskNodeDataMap.get("applyDetail")));
            taskNodeDataHis.setApplyReson(Convert.toStr(taskNodeDataMap.get("applyReson")));
            taskNodeDataHis.setApplyRemarks(Convert.toStr(taskNodeDataMap.get("applyRemarks")));
            taskNodeDataHis.setRequiredDateType(Convert.toStr(taskNodeDataMap.get("requiredDateType")));
            taskNodeDataHis.setCompleteDate(DateUtil.parse(Convert.toStr(taskNodeDataMap.get("completeDate"))));
            taskNodeDataHis.setRequiredDesc(Convert.toStr(taskNodeDataMap.get("requiredDesc")));
            taskNodeDataHis.setApplyDept(Convert.toStr(taskNodeDataMap.get("applyDept")));
            taskNodeDataHis.setApplyType(Convert.toStr(taskNodeDataMap.get("applyType")));
            taskNodeDataHis.setStatus(taskNodeData.getStatus());
            taskNodeDataHis.setTaskNodeDataId(taskNodeData.getId());
            taskNodeDataHis.setTaskId(event.getTaskId());
            taskNodeDataHis.setCheckTo(taskNodeData.getCheckTo());
            taskNodeDataHis.setApplyContentType(taskNodeData.getApplyContentType());
            taskNodeDataHis.setCreateDept(taskNodeData.getCreateDept());
            taskNodeDataHis.setCreateBy(taskNodeData.getCreateBy());
            taskNodeDataHis.setUpdateBy(taskNodeData.getUpdateBy());
            taskNodeDataHisMapper.insert(taskNodeDataHis);
        }

    }

    @Override
    public List<DcwsInvoiceVo> uploadInvoice(String fileIds) throws Exception {
        List<String> str = Arrays.asList(fileIds.split(","));
        List<SysFileVo> fileList = sysFileService.listByFileIds(str.stream().map(Long::parseLong).collect(Collectors.toList()));
        List<DcwsInvoiceVo> list = new ArrayList<>();
        for (SysFileVo sysFileVo : fileList){
            DcwsInvoiceVo dcwsInvoiceVo = new DcwsInvoiceVo();
            dcwsInvoiceVo.setFileId(sysFileVo.getFileId().toString());
            dcwsInvoiceVo.setInvoiceName(sysFileVo.getFileName());
            InputStream file = null;
            if (".pdf".equals(sysFileVo.getFileSuffix())){
                List<Long> associationFileIds = new ArrayList<>();
                associationFileIds.add(sysFileVo.getAssociationFileId());
                SysFileVo sysFileVoJpg = sysFileService.listByFileIds(associationFileIds).get(0);
                file = minioUtil.download("dcws-assets",sysFileVoJpg.getFileName());
            }else{
                file = minioUtil.download("dcws-assets",sysFileVo.getFileName());
            }
            //获取发票信息
            String invoiceInfo = DcwsAiUtils.invoiceIdentification(file,"请识别图中的纳税人识别号，价税合计小写(不带币种)，并输出为纳税人识别号重命名为:taxnum,价税合计小写重命名为:amount的标准json字符串");
            if (StringUtils.isEmpty(invoiceInfo)){
                throw new ServiceException("发票识别错误");
            }
            JSONObject invoice = JSON.parseObject(invoiceInfo.replace("```json","").replace("```",""));
            if(Objects.isNull(invoice.get("taxnum")) || Objects.isNull(invoice.get("amount"))){
                throw new ServiceException("发票识别错误");
            }
            dcwsInvoiceVo.setVerify("91440300754269153R".equals(invoice.get("taxnum")) ? "Y":"N");
            dcwsInvoiceVo.setAmount(new BigDecimal(String.valueOf(invoice.get("amount"))));
            //dcwsInvoiceVo.setInvoiceType((String) invoice.get("invoiceType"));
            list.add(dcwsInvoiceVo);
        }
        return list;
    }
    /**
     * 查询已完成finish待生成PDF的申请列表，分页
     */
    @Override
    public Page<TaskNodeDataVo> queryPageApplyPDF() {
        LambdaQueryWrapper<TaskNodeData> lqw = Wrappers.lambdaQuery();
        lqw.eq(TaskNodeData::getStatus, BusinessStatusEnum.FINISH.getStatus());
        lqw.eq(TaskNodeData::getStorageFileStatus, STORAGEFILESTATUS_4.getCode());// 待生成PDF
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum(1);
        pageQuery.setPageSize(10);
        // 查询所有枚举中的申请类型交易
        lqw.in(TaskNodeData::getApplyType,Arrays.stream(values()).map(ApplyTypeEnum::getCode).toList());
        lqw.orderByDesc(BaseEntity::getCreateTime);
        return taskNodeDataMapper.selectVoPage(pageQuery.build(), lqw);
    }
}
