package com.ysmjjsy.goya.starter.redis.service;

import com.ysmjjsy.goya.starter.redis.constants.IRedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * <p>Redis 特有功能服务实现类</p>
 * <p>基于 Redisson 实现 Redis 独有的分布式功能</p>
 *
 * @author goya
 * @since 2025/12/22 00:20
 * @see IRedisService
 * @see RedissonClient
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService implements IRedisService {

    private final RedissonClient redissonClient;

    /* ---------- 原子计数器操作 ---------- */

    @Override
    public Long increment(String key) {
        try {
            RAtomicLong atomicLong = getAtomicLong(key);
            long result = atomicLong.incrementAndGet();
            log.trace("[Goya] |- starter [redis] |- increment counter [{}] to [{}]", key, result);
            return result;
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- increment counter [{}] failed", key, e);
            throw e;
        }
    }

    @Override
    public Long incrementBy(String key, long delta) {
        try {
            RAtomicLong atomicLong = getAtomicLong(key);
            long result = atomicLong.addAndGet(delta);
            log.trace("[Goya] |- starter [redis] |- increment counter [{}] by [{}] to [{}]", 
                    key, delta, result);
            return result;
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- increment counter [{}] by [{}] failed", key, delta, e);
            throw e;
        }
    }

    @Override
    public Long decrement(String key) {
        try {
            RAtomicLong atomicLong = getAtomicLong(key);
            long result = atomicLong.decrementAndGet();
            log.trace("[Goya] |- starter [redis] |- decrement counter [{}] to [{}]", key, result);
            return result;
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- decrement counter [{}] failed", key, e);
            throw e;
        }
    }

    @Override
    public Long decrementBy(String key, long delta) {
        try {
            RAtomicLong atomicLong = getAtomicLong(key);
            long result = atomicLong.addAndGet(-delta);
            log.trace("[Goya] |- starter [redis] |- decrement counter [{}] by [{}] to [{}]", 
                    key, delta, result);
            return result;
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- decrement counter [{}] by [{}] failed", key, delta, e);
            throw e;
        }
    }

    @Override
    public Long getCounter(String key) {
        try {
            RAtomicLong atomicLong = getAtomicLong(key);
            return atomicLong.get();
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- get counter [{}] failed", key, e);
            throw e;
        }
    }

    @Override
    public void setCounter(String key, long value) {
        try {
            RAtomicLong atomicLong = getAtomicLong(key);
            atomicLong.set(value);
            log.trace("[Goya] |- starter [redis] |- set counter [{}] to [{}]", key, value);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- set counter [{}] to [{}] failed", key, value, e);
            throw e;
        }
    }

    @Override
    public RAtomicLong getAtomicLong(String key) {
        String atomicKey = IRedisConstants.REDIS_COUNTER_PREFIX + key;
        return redissonClient.getAtomicLong(atomicKey);
    }

    /* ---------- 发布订阅操作 ---------- */

    @Override
    public <T> long publish(String topic, T message) {
        try {
            String topicKey = IRedisConstants.REDIS_TOPIC_PREFIX + topic;
            RTopic rTopic = redissonClient.getTopic(topicKey);
            long receivers = rTopic.publish(message);
            log.trace("[Goya] |- starter [redis] |- publish message to topic [{}], receivers [{}]", 
                    topic, receivers);
            return receivers;
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- publish message to topic [{}] failed", topic, e);
            throw e;
        }
    }

    @Override
    public <T> int subscribe(String topic, Consumer<T> listener) {
        try {
            String topicKey = IRedisConstants.REDIS_TOPIC_PREFIX + topic;
            RTopic rTopic = redissonClient.getTopic(topicKey);
            int listenerId = rTopic.addListener(Object.class, (channel, msg) -> {
                @SuppressWarnings("unchecked")
                T typedMsg = (T) msg;
                listener.accept(typedMsg);
            });
            log.debug("[Goya] |- starter [redis] |- subscribe to topic [{}], listener id [{}]", 
                    topic, listenerId);
            return listenerId;
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- subscribe to topic [{}] failed", topic, e);
            throw e;
        }
    }

    @Override
    public void unsubscribe(String topic, int listenerId) {
        try {
            String topicKey = IRedisConstants.REDIS_TOPIC_PREFIX + topic;
            RTopic rTopic = redissonClient.getTopic(topicKey);
            rTopic.removeListener(listenerId);
            log.debug("[Goya] |- starter [redis] |- unsubscribe from topic [{}], listener id [{}]", 
                    topic, listenerId);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- unsubscribe from topic [{}] failed", topic, e);
            throw e;
        }
    }

    /* ---------- 分布式信号量操作 ---------- */

    @Override
    public RSemaphore getSemaphore(String name) {
        String semaphoreKey = IRedisConstants.REDIS_SEMAPHORE_PREFIX + name;
        return redissonClient.getSemaphore(semaphoreKey);
    }

    @Override
    public boolean trySetPermits(String name, int permits) {
        try {
            RSemaphore semaphore = getSemaphore(name);
            boolean result = semaphore.trySetPermits(permits);
            log.trace("[Goya] |- starter [redis] |- try set semaphore [{}] permits to [{}], result [{}]",
                    name, permits, result);
            return result;
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- try set semaphore [{}] permits failed", name, e);
            throw e;
        }
    }

    @Override
    public int availablePermits(String name) {
        try {
            RSemaphore semaphore = getSemaphore(name);
            return semaphore.availablePermits();
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- get semaphore [{}] available permits failed", name, e);
            throw e;
        }
    }

    @Override
    public void acquire(String name) {
        try {
            RSemaphore semaphore = getSemaphore(name);
            semaphore.acquire();
            log.trace("[Goya] |- starter [redis] |- semaphore [{}] acquired", name);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Goya] |- starter [redis] |- semaphore [{}] acquire interrupted", name, e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- semaphore [{}] acquire failed", name, e);
            throw e;
        }
    }

    @Override
    public boolean tryAcquire(String name) {
        try {
            RSemaphore semaphore = getSemaphore(name);
            boolean result = semaphore.tryAcquire();
            log.trace("[Goya] |- starter [redis] |- semaphore [{}] try acquire, result [{}]", name, result);
            return result;
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- semaphore [{}] try acquire failed", name, e);
            throw e;
        }
    }

    @Override
    public boolean tryAcquire(String name, Duration timeout) {
        try {
            RSemaphore semaphore = getSemaphore(name);
            boolean result = semaphore.tryAcquire(timeout.toMillis(), TimeUnit.MILLISECONDS);
            log.trace("[Goya] |- starter [redis] |- semaphore [{}] try acquire with timeout [{}], result [{}]",
                    name, timeout, result);
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Goya] |- starter [redis] |- semaphore [{}] try acquire interrupted", name, e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- semaphore [{}] try acquire failed", name, e);
            throw e;
        }
    }

    @Override
    public void release(String name) {
        try {
            RSemaphore semaphore = getSemaphore(name);
            semaphore.release();
            log.trace("[Goya] |- starter [redis] |- semaphore [{}] released", name);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- semaphore [{}] release failed", name, e);
            throw e;
        }
    }

    /* ---------- 分布式倒计时门闩操作 ---------- */

    @Override
    public RCountDownLatch getCountDownLatch(String name) {
        String latchKey = IRedisConstants.REDIS_COUNTDOWN_PREFIX + name;
        return redissonClient.getCountDownLatch(latchKey);
    }

    @Override
    public boolean trySetCount(String name, long count) {
        try {
            RCountDownLatch latch = getCountDownLatch(name);
            boolean result = latch.trySetCount(count);
            log.trace("[Goya] |- starter [redis] |- try set countdown latch [{}] count to [{}], result [{}]",
                    name, count, result);
            return result;
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- try set countdown latch [{}] count failed", name, e);
            throw e;
        }
    }

    @Override
    public void countDown(String name) {
        try {
            RCountDownLatch latch = getCountDownLatch(name);
            latch.countDown();
            log.trace("[Goya] |- starter [redis] |- countdown latch [{}] count down", name);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- countdown latch [{}] count down failed", name, e);
            throw e;
        }
    }

    @Override
    public long getCount(String name) {
        try {
            RCountDownLatch latch = getCountDownLatch(name);
            return latch.getCount();
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- get countdown latch [{}] count failed", name, e);
            throw e;
        }
    }

    @Override
    public void await(String name) {
        try {
            RCountDownLatch latch = getCountDownLatch(name);
            latch.await();
            log.trace("[Goya] |- starter [redis] |- countdown latch [{}] await completed", name);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Goya] |- starter [redis] |- countdown latch [{}] await interrupted", name, e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- countdown latch [{}] await failed", name, e);
            throw e;
        }
    }

    @Override
    public boolean await(String name, Duration timeout) {
        try {
            RCountDownLatch latch = getCountDownLatch(name);
            boolean result = latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
            log.trace("[Goya] |- starter [redis] |- countdown latch [{}] await with timeout [{}], result [{}]",
                    name, timeout, result);
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[Goya] |- starter [redis] |- countdown latch [{}] await interrupted", name, e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- countdown latch [{}] await failed", name, e);
            throw e;
        }
    }

    /* ---------- 工具方法 ---------- */

    @Override
    public String info() {
        try {
            // Redisson 不直接提供 info 命令，这里返回基本信息
            StringBuilder info = new StringBuilder();
            info.append("Redisson Client Info:\n");
            info.append("Config: ").append(redissonClient.getConfig().toString()).append("\n");
            log.trace("[Goya] |- starter [redis] |- get info success");
            return info.toString();
        } catch (Exception e) {
            log.error("[Goya] |- starter [redis] |- get info failed", e);
            return "Error getting Redis info: " + e.getMessage();
        }
    }
}

