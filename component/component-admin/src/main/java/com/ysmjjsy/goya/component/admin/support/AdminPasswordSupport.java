package com.ysmjjsy.goya.component.admin.support;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * <p>密码编解码支持</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public class AdminPasswordSupport {

    private final ObjectProvider<PasswordEncoder> passwordEncoderProvider;

    public AdminPasswordSupport(ObjectProvider<PasswordEncoder> passwordEncoderProvider) {
        this.passwordEncoderProvider = passwordEncoderProvider;
    }

    public String encode(String rawPassword) {
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();
        if (passwordEncoder == null || StringUtils.isBlank(rawPassword)) {
            return rawPassword;
        }
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        PasswordEncoder passwordEncoder = passwordEncoderProvider.getIfAvailable();
        if (passwordEncoder == null) {
            return StringUtils.equals(rawPassword, encodedPassword);
        }
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
