package com.ysmjjsy.goya.security.authentication.provider.login;

import com.ysmjjsy.goya.component.social.enums.SocialTypeEnum;
import com.ysmjjsy.goya.component.social.service.SocialService;
import com.ysmjjsy.goya.security.authentication.provider.AbstractAuthenticationProvider;
import com.ysmjjsy.goya.security.core.enums.LoginTypeEnum;
import com.ysmjjsy.goya.security.core.manager.SecurityUserManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * <p>短信登录认证提供者</p>
 * <p>处理短信验证码登录方式的认证</p>
 * <p>符合Spring Security标准模式，实现AuthenticationProvider接口</p>
 * <p>用于Authorization Code流程，返回UsernamePasswordAuthenticationToken让SAS框架处理授权码生成</p>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>接收SmsAuthenticationToken（包含手机号和验证码）</li>
 *   <li>验证短信验证码</li>
 *   <li>查找或创建用户</li>
 *   <li>返回UsernamePasswordAuthenticationToken（让SAS框架处理授权码生成）</li>
 * </ol>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
public class SmsAuthenticationProvider extends AbstractAuthenticationProvider {

    private final SocialService socialService;

    public SmsAuthenticationProvider(SecurityUserManager securityUserService, SocialService socialService) {
        super(securityUserService);
        this.socialService = socialService;
    }

    @Override
    protected LoginTypeEnum loginType() {
        return LoginTypeEnum.SMS;
    }

    @Override
    protected Authentication doAuthenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken smsToken = (SmsAuthenticationToken) authentication;
        String phoneNumber = (String) smsToken.getPrincipal();
        String smsCode = (String) smsToken.getCredentials();

        validateSmsCode(phoneNumber,smsCode);

        UserDetails user = retrieveUser(phoneNumber, SocialTypeEnum.SMS);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private void validateSmsCode(String phoneNumber, String smsCode) {
        if (StringUtils.isAnyBlank(phoneNumber, smsCode)) {
            throw new BadCredentialsException("手机号或短信验证码不能为空");
        }

        if (!socialService.verify(phoneNumber, smsCode)) {
            log.warn("[Goya] |- SMS code mismatch or expired for phoneNumber: {}", phoneNumber);
            throw new BadCredentialsException("短信验证码错误或已过期");
        }
    }

    @Override
    protected void assertSupports(Authentication authentication) {
        if (!(authentication instanceof SmsAuthenticationToken)) {
            throw new AuthenticationServiceException("Unsupported authentication type: " + authentication.getClass());
        }
    }

    @Override
    protected boolean supportsAuthentication(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

