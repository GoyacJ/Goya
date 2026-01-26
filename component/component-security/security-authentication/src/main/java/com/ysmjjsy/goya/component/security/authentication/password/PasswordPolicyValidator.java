package com.ysmjjsy.goya.component.security.authentication.password;

import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.exception.PasswordPolicyException;
import com.ysmjjsy.goya.component.security.core.manager.SecurityUserManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Pattern;

/**
 * <p>密码策略验证器</p>
 * <p>验证密码复杂度，检查历史密码（防止重复使用）</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Slf4j
public class PasswordPolicyValidator {

    private final SecurityUserManager securityUserManager;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuthenticationProperties.PasswordPolicy passwordPolicy;

    public PasswordPolicyValidator(SecurityUserManager securityUserManager,
                                   PasswordEncoder passwordEncoder,
                                   SecurityAuthenticationProperties properties) {
        this.securityUserManager = securityUserManager;
        this.passwordEncoder = passwordEncoder;
        this.passwordPolicy = properties.passwordPolicy();
    }

    /**
     * 校验密码是否相等
     *
     * @param password      密码
     * @param storePassword 存储的密码
     */
    public boolean matched(String password, String storePassword) {
        return passwordEncoder.matches(password, storePassword);
    }

    /**
     * 验证密码是否符合策略
     *
     * @param password 密码
     * @throws AuthenticationException 如果密码不符合策略
     */
    public void validate(String password) throws AuthenticationException {
        if (passwordPolicy.enabled() == null || !passwordPolicy.enabled()) {
            return;
        }

        if (StringUtils.isBlank(password)) {
            throw new PasswordPolicyException("密码不能为空");
        }

        // 1. 长度检查
        int length = password.length();
        if (passwordPolicy.minLength() != null && length < passwordPolicy.minLength()) {
            throw new PasswordPolicyException(
                    String.format("密码长度不能少于%d个字符", passwordPolicy.minLength()));
        }
        if (passwordPolicy.maxLength() != null && length > passwordPolicy.maxLength()) {
            throw new PasswordPolicyException(
                    String.format("密码长度不能超过%d个字符", passwordPolicy.maxLength()));
        }

        // 2. 复杂度检查
        if (Boolean.TRUE.equals(passwordPolicy.requireUppercase()) && !hasUppercase(password)) {
            throw new PasswordPolicyException("密码必须包含至少一个大写字母");
        }

        if (Boolean.TRUE.equals(passwordPolicy.requireLowercase()) && !hasLowercase(password)) {
            throw new PasswordPolicyException("密码必须包含至少一个小写字母");
        }

        if (Boolean.TRUE.equals(passwordPolicy.requireDigit()) && !hasDigit(password)) {
            throw new PasswordPolicyException("密码必须包含至少一个数字");
        }

        if (Boolean.TRUE.equals(passwordPolicy.requireSpecialChar()) && !hasSpecialChar(password)) {
            throw new PasswordPolicyException("密码必须包含至少一个特殊字符");
        }

        log.debug("[Goya] |- security [authentication] Password policy validation passed.");
    }

    /**
     * 检查密码是否在历史密码中（防止重复使用）
     * <p>注意：此方法需要用户服务实现历史密码存储和检查逻辑</p>
     *
     * @param userId   用户名
     * @param password 新密码
     * @return true如果密码在历史中，false如果不在
     */
    public boolean isPasswordInHistory(String userId, String password) {
        if (passwordPolicy.preventReuse() == null || !passwordPolicy.preventReuse()) {
            return false;
        }

        log.debug("[Goya] |- security [authentication] Password history check not implemented yet.");
        return securityUserManager.isPasswordInHistory(userId, password);
    }

    /**
     * 检查是否包含大写字母
     *
     * @param password 密码
     * @return true如果包含大写字母
     */
    private boolean hasUppercase(String password) {
        return Pattern.compile("[A-Z]").matcher(password).find();
    }

    /**
     * 检查是否包含小写字母
     *
     * @param password 密码
     * @return true如果包含小写字母
     */
    private boolean hasLowercase(String password) {
        return Pattern.compile("[a-z]").matcher(password).find();
    }

    /**
     * 检查是否包含数字
     *
     * @param password 密码
     * @return true如果包含数字
     */
    private boolean hasDigit(String password) {
        return Pattern.compile("[0-9]").matcher(password).find();
    }

    /**
     * 检查是否包含特殊字符
     *
     * @param password 密码
     * @return true如果包含特殊字符
     */
    private boolean hasSpecialChar(String password) {
        String specialChars = passwordPolicy.specialChars();
        if (StringUtils.isBlank(specialChars)) {
            specialChars = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        }
        String regex = "[" + Pattern.quote(specialChars) + "]";
        return Pattern.compile(regex).matcher(password).find();
    }
}

