package com.ysmjjsy.goya.component.security.authentication.provider.login;

import com.ysmjjsy.goya.component.security.authentication.provider.AbstractAuthenticationProvider;
import com.ysmjjsy.goya.component.security.core.enums.LoginTypeEnum;
import com.ysmjjsy.goya.component.security.core.enums.SocialTypeEnum;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

/**
 * <p>社交登录认证提供者</p>
 * <p>处理第三方社交登录方式的认证（微信、Gitee、GitHub等）</p>
 * <p>符合Spring Security标准模式，实现AuthenticationProvider接口</p>
 * <p>用于Authorization Code流程，返回UsernamePasswordAuthenticationToken让SAS框架处理授权码生成</p>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>接收SocialAuthenticationToken（包含社交提供商ID和用户属性）</li>
 *   <li>查找或创建用户</li>
 *   <li>返回UsernamePasswordAuthenticationToken（让SAS框架处理授权码生成）</li>
 * </ol>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
public class SocialAuthenticationProvider extends AbstractAuthenticationProvider {

    public SocialAuthenticationProvider(SecurityUserManager securityUserManager) {
        super(securityUserManager);
    }

    @Override
    protected LoginTypeEnum loginType() {
        return LoginTypeEnum.SOCIAL;
    }

    @Override
    protected Authentication doAuthenticate(Authentication authentication) throws AuthenticationException {
        SocialAuthenticationToken socialAuthenticationToken = (SocialAuthenticationToken) authentication;
        Map<String, Object> additionalParameters = (Map<String, Object>) socialAuthenticationToken.getDetails();
        SocialTypeEnum socialType = (SocialTypeEnum) socialAuthenticationToken.getPrincipal();
        UserDetails userDetails;
        if (SocialTypeEnum.WECHAT_MINI_PROGRAM.equals(socialType)) {
            String openId = additionalParameters.get("open_id").toString();
            String appId = additionalParameters.get("appId").toString();
            String sessionKey = additionalParameters.get("sessionKey").toString();
            String encryptedData = additionalParameters.get("encryptedData").toString();
            String iv = additionalParameters.get("iv").toString();
            userDetails = retrieveUser(openId, appId, sessionKey, encryptedData, iv);
        } else if (SocialTypeEnum.THIRD_PART.equals(socialType)) {
            userDetails = retrieveUser((String) socialAuthenticationToken.getCredentials(), additionalParameters);
        } else {
            throw new BadCredentialsException("社交信息错误");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    }

    @Override
    protected void assertSupports(Authentication authentication) {
        if (!(authentication instanceof SocialAuthenticationToken)) {
            throw new AuthenticationServiceException("Unsupported authentication type: " + authentication.getClass());
        }
    }

    @Override
    protected boolean supportsAuthentication(Class<?> authentication) {
        return SocialAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
