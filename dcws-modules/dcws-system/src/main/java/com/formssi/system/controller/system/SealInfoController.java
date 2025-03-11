package com.formssi.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.convert.Convert;
import com.formssi.common.core.constant.UserConstants;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.log.annotation.Log;
import com.formssi.common.log.enums.BusinessType;
import com.formssi.common.web.core.BaseController;
import com.formssi.system.domain.bo.SealInfoBo;
import com.formssi.system.domain.bo.SysDeptBo;
import com.formssi.system.domain.vo.SealInfoVo;
import com.formssi.system.domain.vo.SysDeptVo;
import com.formssi.system.service.ISealInfoService;
import com.formssi.system.service.ISysDeptService;
import com.formssi.system.service.ISysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 印章信息
 *
 * @author Lion Li
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/seal")
public class SealInfoController extends BaseController {

    private final ISealInfoService sealInfoService;

    /**
     * 获取印章列表
     */
    @SaCheckPermission("system:seal:list")
    @GetMapping("/list")
    public R<List<SealInfoVo>> list(SealInfoBo info) {
        List<SealInfoVo> infos = sealInfoService.selectSealInfoList(info);
        return R.ok(infos);
    }


}
