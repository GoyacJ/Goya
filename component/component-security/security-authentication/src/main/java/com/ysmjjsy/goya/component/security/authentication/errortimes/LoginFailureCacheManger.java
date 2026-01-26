package com.ysmjjsy.goya.component.security.authentication.errortimes;

import com.ysmjjsy.goya.component.cache.multilevel.resolver.CacheSpecification;
import com.ysmjjsy.goya.component.cache.multilevel.template.AbstractCounterTemplate;
import com.ysmjjsy.goya.component.cache.multilevel.ttl.TtlStrategy;
import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.security.authentication.configuration.properties.SecurityAuthenticationProperties;
import com.ysmjjsy.goya.component.security.authentication.constants.SecurityAuthenticationConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/4 13:31
 */
@Slf4j
public class LoginFailureCacheManger extends AbstractCounterTemplate<String> {

    private final SecurityAuthenticationProperties.LoginFailureConfig loginFailureConfig;

    public LoginFailureCacheManger(SecurityAuthenticationProperties properties) {
        this.loginFailureConfig = properties.loginFailure();
    }

    @Override
    protected String getCacheName() {
        return SecurityAuthenticationConst.CACHE_SECURITY_AUTHENTICATION_LOGIN_PREFIX;
    }

    /**
     * 在缓存有效期内进行计数
     *
     * @param identity 缓存 Key 的区分标识
     * @return 当前错误次数
     * @throws CommonException 超出最大限制次数错误
     */
    public boolean checkErrorTimes(String identity) throws CommonException {
        if (loginFailureConfig.enabled()) {
            Assert.notNull(identity, "identity cannot be null");
            boolean exists = exists(identity);
            long times;
            if (exists) {
                // 如果传入的 expire 不为零，那么就用 expire 参数值
                times = increment(identity);
            } else {
                times = increment(identity, loginFailureConfig.expire());
            }

            if (times >= loginFailureConfig.maxTimes() - 1) {
                log.warn("Requests are too frequent. Please try again later!, identity:{},times:{}", identity, times);
                return true;
            }

            log.debug("[HZ-ZHG] |- {} has been recorded [{}] times.", identity, times);
            return false;
        } else {
            return false;
        }
    }

    /**
     * 获取错误次数
     *
     * @param identity key
     * @return 错误次数
     */
    public int errorTimes(String identity) {
        return get(identity).intValue();
    }

    /**
     * 清除错误次数
     *
     * @param identity key
     */
    public void clear(String identity) {
        evict(identity);
    }

    @Override

    protected CacheSpecification buildCacheSpecification(CacheSpecification defaultSpec) {
        CacheSpecification.Builder builder = defaultSpec.toBuilder();
        builder.ttl(loginFailureConfig.expire());
        TtlStrategy.FixedRatioStrategy fixedRatioStrategy = new TtlStrategy.FixedRatioStrategy(0.8);
        builder.localTtlStrategy(fixedRatioStrategy);
        return super.buildCacheSpecification(builder.build());
    }
}
