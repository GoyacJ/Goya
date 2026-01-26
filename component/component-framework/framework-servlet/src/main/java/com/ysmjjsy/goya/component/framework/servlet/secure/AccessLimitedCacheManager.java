package com.ysmjjsy.goya.component.framework.servlet.secure;

import com.ysmjjsy.goya.component.framework.cache.support.CacheSupport;
import com.ysmjjsy.goya.component.framework.servlet.autoconfigure.properties.GoyaWebProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

import static com.ysmjjsy.goya.component.framework.servlet.constant.WebConst.CACHE_WEB_PREFIX;

/**
 * <p>防刷管理器</p>
 * 这里使用Long类型作为值的存储类型，是为了解决该Cache 同时可以存储Duration相关的数据
 *
 * @author goya
 * @since 2025/10/9 10:26
 */
@Slf4j
public class AccessLimitedCacheManager extends CacheSupport<String, Long> {

    public static final String CACHE_ACCESS_LIMITED_PREFIX = CACHE_WEB_PREFIX + "access_limited:";

    @Getter
    private final GoyaWebProperties.AccessLimited accessLimited;

    public AccessLimitedCacheManager(GoyaWebProperties.AccessLimited accessLimited) {
        super(CACHE_ACCESS_LIMITED_PREFIX, accessLimited.expire());
        this.accessLimited = accessLimited;
    }

    /**
     * 计算剩余过期时间
     * <p>
     * 每次create或者put，缓存的过期时间都会被覆盖。（注意：Jetcache put 方法的参数名：expireAfterWrite）。
     * 因为Jetcache没有Redis的incr之类的方法，那么每次放入Times值，都会更新过期时间，实际操作下来是变相的延长了过期时间。
     *
     * @param configuredDuration 注解上配置的、且可以正常解析的Duration值
     * @param expireKey          时间标记存储Key值。
     * @return 还剩余的过期时间 {@link Duration}
     */
    public Duration calculateRemainingTime(Duration configuredDuration, String expireKey) {
        Long begin = get(expireKey);
        Long current = System.currentTimeMillis();
        long interval = current - begin;

        log.debug("[Goya] |- AccessLimited operation interval [{}] millis.", interval);

        Duration duration;
        if (!configuredDuration.isZero()) {
            duration = configuredDuration.minusMillis(interval);
        } else {
            duration = getExpire().minusMillis(interval);
        }

        return duration;
    }
}
