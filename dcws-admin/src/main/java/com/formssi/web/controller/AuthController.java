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
import com.formssi.web.domain.vo.LoginTenantVo;
import com.formssi.web.domain.vo.LoginTokenVo;
import com.formssi.web.domain.vo.LoginVo;
import com.formssi.web.domain.vo.TenantListVo;
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
    public R<LoginTokenVo> getDcwsToken(@RequestBody LoginTokenBo loginTokenBo) throws Exception {
        String data = loginTokenBo.getData();
        if (StringUtils.isNotEmpty(data)){
            PrivateKey privateKey = SecurityUtil.getPrivateKeyFromString("MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDR9FemQT9UVq2kuompakrdh1lRfz9TlaljPsUSL4FJu+VaWZ2of7nQNQeDCueCs6VACz6z53PKNCh7N70iWc52QpjWqrGDgXQRU7vpnAysHDZRy8e1ENtYZar8vW/uRm3yUq3gzelo1jP3dVEE7E6OJlyC3+CLl6k1CgN3ym1IKewrG0rflLevbDZh4FQpgoeUhXqKFh+nfbE7CSMIOsdjSmaWoZsE3SEBBRbh1PHIVmUIB3hKth0qNxxDV9uHo7OlHPEDN2ZK24+EXbIMrcyYDhJbmn8KDqkDdCdWRZkiWMkG1NrFjjrV+r7CuhDUbMiRD/MMeIQ/KrgC5gLXX3bVAgMBAAECggEACFJuYpIltixk+Wl4otHD27anAfwQhzntVJg8pbH8jQiMGKho5ePOLZd0gt62eq+vn9nyQQPhfitsF8ChWBCF02bKk4CBK1/A7IQZQWZXxqV41IXGlptvV+uLuzUWqZpHQ5QmyglCmuiARDfbuSpVGmOHeWgk7xOMKvFVkUF7H6GRtDh0elkGEH7wsz0GQoJ/IgHWQFjpwdKTbaKjmQ6C8FZvvSQa2RwTgqC4bw0n+loUPy+zCxxjukb0toLW3ZLLBk4xvxpRjRF4lRNFdr6I4ydf1vkAvAtZK6+2mjvBaSbGXtWyyPC1k6swdqBzGo3JKCLPtDfLLioLO3um2+fqrwKBgQDSlvkvh8BWvAYAX+ciaytPZUV69R1omdiJsu8Zy4zavaSmKuPclE6+5LKk1Ca151z/Oc6obr0in+TKtWLwKLDd3e9RR55R+Gm9kmLaQnr2ClhcYkp/B3w6ec4B6FaNH+d07Ax7gLqhXdU5TDJ/ktwLS2nZQvct6nFaY8tvxFy3CwKBgQD/OkzeBBUu4te1vSJ8hNeBiLSlC/j9lMMUItshEQf/lVIEYdy/l1y+BtDRfaSNs/ijlEzfoES9N3uuMY1i7c8SaktPwsQSJTpwypWrQQ7vBjqTKW382hH2UxWL+YCrAeHjgwLKNcnhgrbeIC4Nd3Fo32/vNhEUn3D7ZTU/XH21nwKBgQCJaP+htveW4MsdtXYw7DLvdIo4p/YPictUVlBTyZDYLkRgNL5H8PHM95dlnBTCPvxcgVDKcK+zBxgX+PFc+YAm1SjSJWQ14lzE2N7twdFP+AIeDfjEGJND6LS2Y+8N2NKDZX7jm2Sr5Hk8EO8mdSJlsEiZ/mshJ8fdDh7xh/RjbwKBgQCPUG1ZPXGnojj+E/YJdY6NbfYBt3dY7O+dnvTs3GNhYLdtPoZ2DshE7A7Vk3eTGjvDnsKLz7LJjR4l8i0yH9bmwEkJwJPYnI70Rs1EHIQGM7kwaVMZaFottvmiX7egTq5I0of+g7WYq42DrQ4vAaLtAIoaCIIO0njesTX1Hjp4gQKBgCJTWFleiiTnx9+B6UcvU3ZCUnrHU/qPd8jqf7pwpkeT5M/8v5gb/Ck0sc/rvgQVY0izZdFhK/ueYKTlO1wka/auy/h5CVdCJpNdeLPo75mAvjWdtCMe0C+eHv0ZfNPKXVrYjz6AJSm3/8nUn1g+ZMC16P64Wv1p7uIdEFhXDsMV");
            //数据解密
            String dataBase = SecurityUtil.decrypt(data,privateKey);
            String sign = loginTokenBo.getSign();
            if (StringUtils.isNotEmpty(sign)){
                //数据验签
                PublicKey publicKey = SecurityUtil.getPublicKeyFromString("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0fRXpkE/VFatpLqJqWpK3YdZUX8/U5WpYz7FEi+BSbvlWlmdqH+50DUHgwrngrOlQAs+s+dzyjQoeze9IlnOdkKY1qqxg4F0EVO76ZwMrBw2UcvHtRDbWGWq/L1v7kZt8lKt4M3paNYz93VRBOxOjiZcgt/gi5epNQoDd8ptSCnsKxtK35S3r2w2YeBUKYKHlIV6ihYfp32xOwkjCDrHY0pmlqGbBN0hAQUW4dTxyFZlCAd4SrYdKjccQ1fbh6OzpRzxAzdmStuPhF2yDK3MmA4SW5p/Cg6pA3QnVkWZIljJBtTaxY461fq+wroQ1GzIkQ/zDHiEPyq4AuYC11921QIDAQAB");
                if (SecurityUtil.verify(dataBase,sign,publicKey)){
                    LoginTokenBo loginToken = JsonUtils.parseObject(dataBase, LoginTokenBo.class);
                    SysClientVo client = clientService.queryByClientId("428a8310cd442757ae699df5d894f051");
                    PasswordLoginBody loginBody = new PasswordLoginBody();
                    loginBody.setUsername(loginToken.getEmpNo());
                    loginBody.setTenantId("000000");
                    String body = JsonUtils.toJsonString(loginBody);
                    LoginVo loginVo = IAuthStrategy.login(body, client, "password");
                    LoginTokenVo loginTokenVo = new LoginTokenVo();
                    loginTokenVo.setToken(loginVo.getAccessToken());
                    loginTokenVo.setEmpId(loginVo.getUserId());
                    return R.ok(loginTokenVo);
                }else {
                    return R.fail();
                }
            }else {
                return R.fail();
            }
        }else {
            return R.fail();
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
