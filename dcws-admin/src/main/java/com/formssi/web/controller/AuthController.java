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
    private String privateKey;

    @Value("${securityKey.llPublicKey}")
    private String publicKey;

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
        PublicKey publicKey = SecurityUtil.getPublicKeyFromString("MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAr2wlA5EfxphMQIs85sLWvui8rtArUWaQeUFl0QpEz13RVU2+WZBdbLJ0J8q3t2KSx3r61OJIPnYE3X5/4KzlEZalbMqpTp+cbCQ3ZDctnGSWWiLfoKeBRqjZj2uvc35w6lQ15k58ShK/WOHgEevapAQojGUFykvOYpa4A389XOyRybmSERjBFF2Bn4SiVSmO62Lh3ItveSaiLhgHsjtccY1m+HKP/8+OO3cbnIrXHm7WMDTT6ETKHBRz10u1i6WJIgW5Mt1n4+Mo28egJjcJmQjFrWeLFYEVqqweP9RBDj8t/Rnryan/acc8QrqtuIo4B3OaDKnjAAIrYOxJV98wWB+qB5QV2Jl6870Gq4Fo1+siHxkrEHgVMNyBkNlZWEFRGNMH7sBap7fleEU7vtLtGg/3JSIUYbqgZc07Ommroh2LPCUoVpSsGy8dwd5cwIrML8jqCu8NxGHsIvuVWEY4c8+IAfWFQgisI5zhGVfbmbkb2TgIg2dHXFXrqXnDtUN47ZhmSemcEjVIxSR/vrIgxqoJgsT8dsoLf8qhYvyc2JyDEnfZlLJ3CxvpcKa8OFKrzWF2/6Xe4M1pwhaPskQA+uUKIgSl/Q0jZPayMDQ0y3Tj9PF2x+5Ux1GefG7O1Y1l5W1X7O/jLd9T/1wN+y8UemFS9yh16OHgqurQeo2cNf8CAwEAAQ==");
        if (SecurityUtil.verify(data,sign,publicKey)){
            PrivateKey privateKey = SecurityUtil.getPrivateKeyFromString("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");
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
