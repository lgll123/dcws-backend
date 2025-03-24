package com.formssi.web.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.formssi.common.core.constant.Constants;
import com.formssi.common.core.constant.GlobalConstants;
import com.formssi.common.core.domain.model.LoginUser;
import com.formssi.common.core.domain.model.PasswordLoginBody;
import com.formssi.common.core.enums.LoginType;
import com.formssi.common.core.enums.UserStatus;
import com.formssi.common.core.exception.user.CaptchaException;
import com.formssi.common.core.exception.user.CaptchaExpireException;
import com.formssi.common.core.exception.user.UserException;
import com.formssi.common.core.utils.MessageUtils;
import com.formssi.common.core.utils.StringUtils;
import com.formssi.common.core.utils.ValidatorUtils;
import com.formssi.common.json.utils.JsonUtils;
import com.formssi.common.redis.utils.RedisUtils;
import com.formssi.common.satoken.utils.LoginHelper;
import com.formssi.common.tenant.helper.TenantHelper;
import com.formssi.common.web.config.properties.CaptchaProperties;
import com.formssi.system.domain.SysUser;
import com.formssi.system.domain.vo.SysClientVo;
import com.formssi.system.domain.vo.SysUserVo;
import com.formssi.system.mapper.SysUserMapper;
import com.formssi.web.domain.vo.LoginVo;
import com.formssi.web.service.IAuthStrategy;
import com.formssi.web.service.SysLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 密码认证策略
 *
 * @author Michelle.Chung
 */
@Slf4j
@Service("password" + IAuthStrategy.BASE_NAME)
@RequiredArgsConstructor
public class PasswordAuthStrategy implements IAuthStrategy {

    private final CaptchaProperties captchaProperties;
    private final SysLoginService loginService;
    private final SysUserMapper userMapper;

    @Override
    public LoginVo login(String body, SysClientVo client) {
        PasswordLoginBody loginBody = JsonUtils.parseObject(body, PasswordLoginBody.class);
        if (!"app".equals(client.getClientKey())){
            ValidatorUtils.validate(loginBody);
        }
        String tenantId = loginBody.getTenantId();
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();
        String code = loginBody.getCode();
        String uuid = loginBody.getUuid();

        boolean captchaEnabled = captchaProperties.getEnable();
        // 验证码开关
        if (captchaEnabled && !"app".equals(client.getClientKey())) {
            validateCaptcha(tenantId, username, code, uuid);
        }
        LoginUser loginUser = TenantHelper.dynamic(tenantId, () -> {
            SysUserVo user = loadUserByUsername(username);
            loginService.checkLogin(LoginType.PASSWORD, tenantId, username, () -> !"app".equals(client.getClientKey()) ? !BCrypt.checkpw(password, user.getPassword()) : false);
            // 此处可根据登录用户的数据不同 自行创建 loginUser
            return loginService.buildLoginUser(user);
        });
        loginUser.setClientKey(client.getClientKey());
        loginUser.setDeviceType(client.getDeviceType());
        SaLoginModel model = new SaLoginModel();
        model.setDevice(client.getDeviceType());
        // 自定义分配 不同用户体系 不同 token 授权时间 不设置默认走全局 yml 配置
        // 例如: 后台用户30分钟过期 app用户1天过期
        model.setTimeout(client.getTimeout());
        model.setActiveTimeout(client.getActiveTimeout());
        //model.setExtra(LoginHelper.CLIENT_KEY, client.getClientId());
        // 生成token
        LoginHelper.login(loginUser, model);

        LoginVo loginVo = new LoginVo();
        loginVo.setAccessToken(StpUtil.getTokenValue());
        loginVo.setExpireIn(StpUtil.getTokenTimeout());
        loginVo.setClientId(client.getClientId());
        loginVo.setUserId(loginUser.getUserId());
        return loginVo;
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    private void validateCaptcha(String tenantId, String username, String code, String uuid) {
        String verifyKey = GlobalConstants.CAPTCHA_CODE_KEY + StringUtils.blankToDefault(uuid, "");
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        if (captcha == null) {
            loginService.recordLogininfor(tenantId, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            loginService.recordLogininfor(tenantId, username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error"));
            throw new CaptchaException();
        }
    }

    private SysUserVo loadUserByUsername(String username) {
        SysUserVo finalUser = null;
        SysUserVo user = null;
        SysUserVo user2 = null;
        SysUserVo user3 = null;
        try {
            user = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmpNo, username));
            user2 = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, username));
            user3 = userMapper.selectVoOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhonenumber, username));
        } catch (Exception e) {
            log.info("登录账号：{} 在系统中重复，请更换其他登录方式.", username);
            throw new UserException("user.repeat", username);
        }
        if (!ObjectUtil.isNull(user)){
            finalUser = user;
        }else if(!ObjectUtil.isNull(user2)){
            finalUser = user2;
        }else if(!ObjectUtil.isNull(user3)){
            finalUser = user3;
        }
        if (ObjectUtil.isNull(finalUser)) {
            log.info("登录账号：{} 不存在.", username);
            throw new UserException("user.not.exists", username);
        } else if (UserStatus.DISABLE.getCode().equals(finalUser.getStatus())) {
            log.info("登录账号：{} 已被停用.", username);
            throw new UserException("user.blocked", username);
        }
        return finalUser;
    }

}
