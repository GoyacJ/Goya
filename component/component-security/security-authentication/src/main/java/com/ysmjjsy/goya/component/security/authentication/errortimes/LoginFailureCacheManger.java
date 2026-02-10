package com.ysmjjsy.goya.component.security.authentication.errortimes;

import com.ysmjjsy.goya.component.framework.cache.support.CacheCounterSupport;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.constants.SecurityAuthenticationConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * <p>登录失败次数缓存管理器</p>
 * <p>记录用户登录失败次数，用于账号锁定功能</p>
 *
 * @author goya
 * @since 2026/1/4 13:31
 */
@Slf4j
public class LoginFailureCacheManger extends CacheCounterSupport {

    private final SecurityAuthenticationProperties.LoginFailureConfig loginFailureConfig;

    public LoginFailureCacheManger(SecurityAuthenticationProperties properties) {
        super(
                SecurityAuthenticationConst.CACHE_SECURITY_AUTHENTICATION_LOGIN_PREFIX,
                properties.loginFailure().expire()
        );
        this.loginFailureConfig = properties.loginFailure();
    }

    /**
     * 在缓存有效期内进行计数
     *
     * @param identity 缓存 Key 的区分标识
     * @return 是否达到最大失败次数（true表示达到，需要锁定账号）
     */
    public boolean checkErrorTimes(String identity) {
        if (!loginFailureConfig.enabled()) {
            return false;
        }

        Assert.notNull(identity, "identity cannot be null");

        // 检查 key 是否存在
        boolean exists = exists(identity);
        long times;

        if (exists) {
            // 如果已存在，自增1（使用默认TTL）
            times = incr(identity);
        } else {
            // 如果不存在，自增1并设置TTL（首次创建时设置过期时间）
            times = incr(identity, 1L, loginFailureConfig.expire());
        }

        if (times >= loginFailureConfig.maxTimes() - 1) {
            log.warn("[Goya] |- security [authentication] Login failure limit reached. identity:{}, times:{}", identity, times);
            return true;
        }

        log.debug("[Goya] |- security [authentication] Login failure recorded. identity:{}, times:{}", identity, times);
        return false;
    }

    /**
     * 获取错误次数
     *
     * @param identity key
     * @return 错误次数，如果不存在则返回 0
     */
    public int errorTimes(String identity) {
        Long count = get(identity);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 清除错误次数
     *
     * @param identity key
     */
    public void clear(String identity) {
        reset(identity);
    }
}
