package com.formssi.web.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.formssi.common.core.constant.UserConstants;
import com.formssi.common.core.domain.R;
import com.formssi.common.core.domain.model.LoginBody;
import com.formssi.common.core.domain.model.PasswordLoginBody;
import com.formssi.common.core.domain.model.RegisterBody;
import com.formssi.common.core.domain.model.SocialLoginBody;
import com.formssi.common.core.utils.*;
import com.formssi.common.encrypt.annotation.ApiEncrypt;
import com.formssi.common.json.utils.JsonUtils;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.common.social.config.properties.SocialLoginConfigProperties;
import com.formssi.common.social.config.properties.SocialProperties;
import com.formssi.common.social.utils.SocialUtils;
import com.formssi.common.sse.dto.SseMessageDto;
import com.formssi.common.sse.utils.SseMessageUtils;
import com.formssi.common.tenant.helper.TenantHelper;
import com.formssi.system.domain.bo.SysTenantBo;
import com.formssi.system.domain.vo.SysClientVo;
import com.formssi.system.domain.vo.SysTenantVo;
import com.formssi.system.service.ISysClientService;
import com.formssi.system.service.ISysConfigService;
import com.formssi.system.service.ISysSocialService;
import com.formssi.system.service.ISysTenantService;
import com.formssi.utils.SecurityUtil;
import com.formssi.web.domain.bo.LoginTokenBo;
import com.formssi.web.domain.bo.LoginUserBo;
import com.formssi.web.domain.vo.*;
import com.formssi.web.service.IAuthStrategy;
import com.formssi.web.service.SysLoginService;
import com.formssi.web.service.SysRegisterService;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 认证
 *
 * @author Lion Li
 */
@Slf4j
@SaIgnore
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final SocialProperties socialProperties;
    private final SysLoginService loginService;
    private final SysRegisterService registerService;
    private final ISysConfigService configService;
    private final ISysTenantService tenantService;
    private final ISysSocialService socialUserService;
    private final ISysClientService clientService;
    private final ScheduledExecutorService scheduledExecutorService;

    @Value("${securityKey.dcwsPrivateKey}")
    private String dcwsPrivateKey;

    @Value("${securityKey.llPublicKey}")
    private String llPublicKey;

    /**
     * 登录方法
     *
     * @param body 登录信息
     * @return 结果
     */
    @ApiEncrypt
    @PostMapping("/login")
    public R<LoginVo> login(@RequestBody String body) {
        LoginBody loginBody = JsonUtils.parseObject(body, LoginBody.class);
        ValidatorUtils.validate(loginBody);
        // 授权类型和客户端id
        String clientId = loginBody.getClientId();
        String grantType = loginBody.getGrantType();
        SysClientVo client = clientService.queryByClientId(clientId);
        // 查询不到 client 或 client 内不包含 grantType
        if (ObjectUtil.isNull(client) || !StringUtils.contains(client.getGrantType(), grantType)) {
            log.info("客户端id: {} 认证类型：{} 异常!.", clientId, grantType);
            return R.fail(MessageUtils.message("auth.grant.type.error"));
        } else if (!UserConstants.NORMAL.equals(client.getStatus())) {
            return R.fail(MessageUtils.message("auth.grant.type.blocked"));
        }
        // 校验租户
        loginService.checkTenant(loginBody.getTenantId());
        // 登录
        LoginVo loginVo = IAuthStrategy.login(body, client, grantType);

        Long userId = LoginHelper.getUserId();
        scheduledExecutorService.schedule(() -> {
            SseMessageDto dto = new SseMessageDto();
            dto.setMessage("欢迎登录数字化协同办公系统");
            dto.setUserIds(List.of(userId));
            SseMessageUtils.publishMessage(dto);
        }, 5, TimeUnit.SECONDS);
        return R.ok(loginVo);
    }

    /**
     * 第三方登录请求
     *
     * @param source 登录来源
     * @return 结果
     */
    @GetMapping("/binding/{source}")
    public R<String> authBinding(@PathVariable("source") String source,
                                 @RequestParam String tenantId, @RequestParam String domain) {
        SocialLoginConfigProperties obj = socialProperties.getType().get(source);
        if (ObjectUtil.isNull(obj)) {
            return R.fail(source + "平台账号暂不支持");
        }
        AuthRequest authRequest = SocialUtils.getAuthRequest(source, socialProperties);
        Map<String, String> map = new HashMap<>();
        map.put("tenantId", tenantId);
        map.put("domain", domain);
        map.put("state", AuthStateUtils.createState());
        String authorizeUrl = authRequest.authorize(Base64.encode(JsonUtils.toJsonString(map), StandardCharsets.UTF_8));
        return R.ok("操作成功", authorizeUrl);
    }

    /**
     * 乐联获取token
     *
     * @param loginTokenBo 请求体
     * @return 结果
     */
    @PostMapping("/getDcwsToken")
    public LoginTokenResVo getDcwsToken(@RequestBody LoginTokenBo loginTokenBo) throws Exception {
        String data = loginTokenBo.getData();
        String sign = loginTokenBo.getSign();
        //数据验签
        PublicKey publicKey = SecurityUtil.getPublicKeyFromString(llPublicKey);
        if (SecurityUtil.verify(data,sign,publicKey)){
            PrivateKey privateKey = SecurityUtil.getPrivateKeyFromString(dcwsPrivateKey);
            //数据解密
            String dataBase = SecurityUtil.decrypt(data,privateKey);
            LoginUserBo loginToken = JsonUtils.parseObject(dataBase, LoginUserBo.class);
            SysClientVo client = clientService.queryByClientId("428a8310cd442757ae699df5d894f051");
            PasswordLoginBody loginBody = new PasswordLoginBody();
            loginBody.setUsername(loginToken.getEmpNo());
            loginBody.setTenantId("000000");
            String body = JsonUtils.toJsonString(loginBody);
            LoginVo loginVo = IAuthStrategy.login(body, client, "password");
            LoginUserVo loginUserVo = new LoginUserVo();
            loginUserVo.setToken(loginVo.getAccessToken());
            loginUserVo.setEmpId(loginVo.getUserId());

            LoginTokenVo loginTokenVo = new LoginTokenVo();
            String dataRes = SecurityUtil.encrypt(JsonUtils.toJsonString(R.ok(loginUserVo)),publicKey);
            loginTokenVo.setData(dataRes);
            loginTokenVo.setSign(SecurityUtil.sign(dataRes,privateKey));

            LoginTokenResVo loginTokenRes = new LoginTokenResVo();
            loginTokenRes.setSucc("0");
            loginTokenRes.setData(loginTokenVo);
            return loginTokenRes;
        }else {
            return null;
        }
    }




    /**
     * 取消授权
     *
     * @param socialId socialId
     */
    @DeleteMapping(value = "/unlock/{socialId}")
    public R<Void> unlockSocial(@PathVariable Long socialId) {
        Boolean rows = socialUserService.deleteWithValidById(socialId);
        return rows ? R.ok() : R.fail("取消授权失败");
    }


    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        loginService.logout();
        return R.ok("退出成功");
    }

    /**
     * 用户注册
     */
    @ApiEncrypt
    @PostMapping("/register")
    public R<Void> register(@Validated @RequestBody RegisterBody user) {
        if (!configService.selectRegisterEnabled(user.getTenantId())) {
            return R.fail("当前系统没有开启注册功能！");
        }
        registerService.register(user);
        return R.ok();
    }

    /**
     * 登录页面租户下拉框
     *
     * @return 租户列表
     */
    @GetMapping("/tenant/list")
    public R<LoginTenantVo> tenantList(HttpServletRequest request) throws Exception {
        // 返回对象
        LoginTenantVo result = new LoginTenantVo();
        boolean enable = TenantHelper.isEnable();
        result.setTenantEnabled(enable);
        // 如果未开启租户这直接返回
        if (!enable) {
            return R.ok(result);
        }

        List<SysTenantVo> tenantList = tenantService.queryList(new SysTenantBo());
        List<TenantListVo> voList = MapstructUtils.convert(tenantList, TenantListVo.class);
        try {
            // 如果只超管返回所有租户
            if (LoginHelper.isSuperAdmin()) {
                result.setVoList(voList);
                return R.ok(result);
            }
        } catch (NotLoginException ignored) {
        }

        // 获取域名
        String host;
        String referer = request.getHeader("referer");
        if (StringUtils.isNotBlank(referer)) {
            // 这里从referer中取值是为了本地使用hosts添加虚拟域名，方便本地环境调试
            host = referer.split("//")[1].split("/")[0];
        } else {
            host = new URL(request.getRequestURL().toString()).getHost();
        }
        // 根据域名进行筛选
        List<TenantListVo> list = StreamUtils.filter(voList, vo ->
                StringUtils.equals(vo.getDomain(), host));
        result.setVoList(CollUtil.isNotEmpty(list) ? list : voList);
        return R.ok(result);
    }

    /**
     * 第三方登录回调业务处理 绑定授权
     *
     * @param loginBody 请求体
     * @return 结果
     */
    @PostMapping("/social/callback")
    public R<Void> socialCallback(@RequestBody SocialLoginBody loginBody) {
        // 获取第三方登录信息
        AuthResponse<AuthUser> response = SocialUtils.loginAuth(
                loginBody.getSource(), loginBody.getSocialCode(),
                loginBody.getSocialState(), socialProperties);
        AuthUser authUserData = response.getData();
        // 判断授权响应是否成功
        if (!response.ok()) {
            return R.fail(response.getMsg());
        }
        loginService.socialRegister(authUserData);
        return R.ok();
    }

}
