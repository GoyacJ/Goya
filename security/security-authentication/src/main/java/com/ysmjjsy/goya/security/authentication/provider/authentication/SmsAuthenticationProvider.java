package com.ysmjjsy.goya.security.authentication.provider.authentication;

import com.ysmjjsy.goya.component.cache.service.ICacheService;
import com.ysmjjsy.goya.security.authentication.exception.SecurityCaptchaArgumentIllegalException;
import com.ysmjjsy.goya.security.authentication.exception.SecurityCaptchaHasExpiredException;
import com.ysmjjsy.goya.security.authentication.exception.SecurityCaptchaMisMatchException;
import com.ysmjjsy.goya.security.core.domain.SecurityUser;
import com.ysmjjsy.goya.security.core.service.ISecurityUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

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
@RequiredArgsConstructor
public class SmsAuthenticationProvider implements AuthenticationProvider {

    /**
     * 短信验证码缓存名称
     */
    private static final String SMS_CODE_CACHE_NAME = "sms:verification:code";

    /**
     * 短信验证码缓存键前缀
     */
    private static final String SMS_CODE_CACHE_KEY_PREFIX = "sms:code:";

    private final ISecurityUserService securityUserService;
    private final ICacheService cacheService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SmsAuthenticationToken smsToken = (SmsAuthenticationToken) authentication;

        String phone = smsToken.getPhone();
        String smsCode = smsToken.getSmsCode();

        log.debug("[Goya] |- security [authentication] Authenticating user with SMS code: {}", phone);

        // 1. 验证短信验证码
        validateSmsCode(phone, smsCode);

        // 2. 根据手机号查找或创建用户
        SecurityUser user = findOrCreateUserByPhone(phone);

        // 返回 Spring Security 标准认证对象
        // 关键：这里返回 UsernamePasswordAuthenticationToken，让 SAS 框架处理授权码生成
        UsernamePasswordAuthenticationToken authenticatedToken =
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,  // credentials 已擦除
                        user.getAuthorities()
                );

        log.debug("[Goya] |- security [authentication] SMS authentication successful for phone: {}, returning UsernamePasswordAuthenticationToken for Authorization Code flow",
                phone);

        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SmsAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 验证短信验证码
     *
     * @param phone   手机号
     * @param smsCode 短信验证码
     * @throws SecurityCaptchaArgumentIllegalException 如果参数不合法
     * @throws SecurityCaptchaHasExpiredException      如果验证码已过期
     * @throws SecurityCaptchaMisMatchException        如果验证码不匹配
     */
    private void validateSmsCode(String phone, String smsCode) {
        if (StringUtils.isBlank(phone)) {
            throw new SecurityCaptchaArgumentIllegalException("手机号不能为空");
        }

        if (StringUtils.isBlank(smsCode)) {
            throw new SecurityCaptchaArgumentIllegalException("短信验证码不能为空");
        }

        // 从缓存中获取验证码
        String cacheKey = SMS_CODE_CACHE_KEY_PREFIX + phone;
        String cachedCode = cacheService.get(SMS_CODE_CACHE_NAME, cacheKey);

        if (StringUtils.isBlank(cachedCode)) {
            log.warn("[Goya] |- security [authentication] SMS code not found or expired for phone: {}", phone);
            throw new SecurityCaptchaHasExpiredException("短信验证码已过期或不存在");
        }

        // 验证验证码
        if (!smsCode.equals(cachedCode)) {
            log.warn("[Goya] |- security [authentication] SMS code mismatch for phone: {}", phone);
            throw new SecurityCaptchaMisMatchException("短信验证码错误");
        }

        // 验证成功后，删除验证码（一次性使用）
        cacheService.evict(SMS_CODE_CACHE_NAME, cacheKey);

        log.debug("[Goya] |- security [authentication] SMS code verification successful for phone: {}", phone);
    }

    /**
     * 根据手机号查找或创建用户
     *
     * @param phone 手机号
     * @return 用户信息
     * @throws AuthenticationException 如果用户查找或创建失败
     */
    private SecurityUser findOrCreateUserByPhone(String phone) {
        try {
            // 尝试通过手机号查找用户
            // 注意：这里假设ISecurityUserService可以通过手机号查找
            // 如果没有，可以通过其他方式实现（如通过用户名查找，用户名即为手机号）
            SecurityUser user = null;
            try {
                // 尝试将手机号作为用户名查找
                user = securityUserService.findUser(phone);
            } catch (Exception e) {
                log.debug("[Goya] |- security [authentication] User not found by phone as username: {}", phone);
            }

            // 如果用户不存在，需要创建新用户
            // 注意：这里需要根据实际业务需求实现用户创建逻辑
            // 为了简化，这里假设用户必须已存在
            if (user == null) {
                log.warn("[Goya] |- security [authentication] User not found for phone: {}, user creation not implemented", phone);
                throw new BadCredentialsException("用户不存在，请先注册");
            }

            // 验证用户手机号是否匹配
            if (!phone.equals(user.getPhoneNumber())) {
                log.warn("[Goya] |- security [authentication] Phone number mismatch for user: {}", user.getUsername());
                throw new BadCredentialsException("手机号与用户信息不匹配");
            }

            // 检查账户状态
            if (!user.isEnabled()) {
                throw new BadCredentialsException("账户已被禁用");
            }
            if (!user.isAccountNonLocked()) {
                throw new BadCredentialsException("账户已被锁定");
            }
            if (!user.isAccountNonExpired()) {
                throw new BadCredentialsException("账户已过期");
            }

            return user;
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Goya] |- security [authentication] Error finding user by phone: {}", phone, e);
            throw new BadCredentialsException("用户查找失败", e);
        }
    }
}

