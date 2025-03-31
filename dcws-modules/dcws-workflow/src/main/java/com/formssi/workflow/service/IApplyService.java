package com.formssi.workflow.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.formssi.common.mybatis.core.page.PageQuery;
import com.formssi.common.mybatis.core.page.TableDataInfo;
import com.formssi.system.domain.vo.InfoChangeImportVo;
import com.formssi.workflow.domain.bo.TaskNodeDataBo;
import com.formssi.workflow.domain.bo.TaskNodeDataQueryBo;
import com.formssi.workflow.domain.vo.DcwsInvoiceVo;
import com.formssi.workflow.domain.vo.DcwsSysFileVo;
import com.formssi.workflow.domain.vo.TaskNodeDataHisVo;
import com.formssi.workflow.domain.vo.TaskNodeDataVo;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

/**
 * 申请Service接口
 *
 * @author may
 * @date 2023-07-21
 */
public interface IApplyService {

    /**
     * 查询申请
     */
    TaskNodeDataVo queryById(String id);
    /**
     * 根据任务ID查询申请
     */
    TaskNodeDataHisVo queryByTaskId(String taskId);

    /**
     * 查询申请列表
     */
    TableDataInfo<TaskNodeDataVo> queryPageList(TaskNodeDataQueryBo bo, PageQuery pageQuery);

    /**
     * 查询用印台账表单
     */
    TableDataInfo<TaskNodeDataVo> queryPageSealList(TaskNodeDataQueryBo bo, PageQuery pageQuery);

    /**
     * 查询申请列表
     */
    List<TaskNodeDataVo> queryList(TaskNodeDataQueryBo bo);

    /**
     * 新增申请
     */
    TaskNodeDataVo insertByBo(TaskNodeDataBo bo);

    /**
     * 修改申请
     */
    TaskNodeDataVo updateByBo(TaskNodeDataBo bo);

    /**
     * 校验并批量删除申请信息
     */
    Boolean deleteWithValidByIds(Collection<String> ids);
    /**
     * 查询申请单PDFURL
     */
    List<DcwsSysFileVo> getApplyPDF(String id);

    /**
     * 上传发票并匹配信息
     */
    List<DcwsInvoiceVo> uploadInvoice(String fileIds) throws Exception;

    /**
     * 查询已完成finish待生成PDF的申请列表，分页
     */
    Page<TaskNodeDataVo> queryPageApplyPDF();

    List<InfoChangeImportVo> readExcel(MultipartFile file);
}
