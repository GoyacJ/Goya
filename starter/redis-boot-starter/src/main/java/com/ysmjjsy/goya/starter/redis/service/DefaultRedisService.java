package com.ysmjjsy.goya.starter.redis.service;

import com.ysmjjsy.goya.component.cache.exception.CacheException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.api.listener.MessageListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/26 15:00
 */
@Slf4j
public class DefaultRedisService implements IRedisService {

    /**
     * Redisson 客户端
     */
    private final RedissonClient redissonClient;

    /**
     * 锁实例缓存（避免重复创建）
     * Key: lock key
     * Value: RLock 实例
     */
    private final Map<String, RLock> lockCache = new ConcurrentHashMap<>();

    /**
     * 订阅 ID 缓存（用于管理订阅）
     * Key: channel
     * Value: listener ID
     */
    private final Map<String, Integer> subscriptionCache = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param redissonClient Redisson 客户端
     * @throws IllegalArgumentException 如果 redissonClient 为 null
     */
    public DefaultRedisService(RedissonClient redissonClient) {
        if (redissonClient == null) {
            throw new IllegalArgumentException("RedissonClient cannot be null");
        }
        this.redissonClient = redissonClient;
    }

    // ========== 分布式锁 ==========

    @Override
    public RLock getLock(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return lockCache.computeIfAbsent(key, k -> redissonClient.getLock(k));
    }

    @Override
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }

        RLock lock = getLock(key);
        try {
            if (leaseTime < 0) {
                return lock.tryLock(waitTime, unit);
            } else {
                return lock.tryLock(waitTime, leaseTime, unit);
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while trying to acquire lock: key={}", key);
            throw e;
        }
    }

    @Override
    public void unlock(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        RLock lock = lockCache.get(key);
        if (lock != null && lock.isHeldByCurrentThread()) {
            try {
                lock.unlock();
            } catch (Exception e) {
                log.warn("Failed to unlock: key={}", key, e);
            }
        }
    }

    // ========== 发布订阅 ==========

    @Override
    public void publish(String channel, Object message) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel cannot be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }

        try {
            RTopic topic = redissonClient.getTopic(channel);
            topic.publish(message);

            if (log.isTraceEnabled()) {
                log.trace("Published message to channel: channel={}, message={}", channel, message);
            }
        } catch (Exception e) {
            log.error("Failed to publish message: channel={}, message={}", channel, message, e);
            throw new CacheException("Failed to publish message", e);
        }
    }

    @Override
    public <T> int subscribe(String channel, MessageListener<T> listener) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel cannot be null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        try {
            RTopic topic = redissonClient.getTopic(channel);
            int listenerId = topic.addListener(listener);
            subscriptionCache.put(channel, listenerId);

            if (log.isDebugEnabled()) {
                log.debug("Subscribed to channel: channel={}, listenerId={}", channel, listenerId);
            }

            return listenerId;
        } catch (Exception e) {
            log.error("Failed to subscribe to channel: channel={}", channel, e);
            throw new CacheException("Failed to subscribe to channel", e);
        }
    }

    @Override
    public void unsubscribe(String channel, int listenerId) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel cannot be null");
        }

        try {
            RTopic topic = redissonClient.getTopic(channel);
            topic.removeListener(listenerId);
            subscriptionCache.remove(channel);

            if (log.isDebugEnabled()) {
                log.debug("Unsubscribed from channel: channel={}, listenerId={}", channel, listenerId);
            }
        } catch (Exception e) {
            log.warn("Failed to unsubscribe from channel: channel={}, listenerId={}", channel, listenerId, e);
        }
    }

    // ========== 原子操作 ==========

    @Override
    public long increment(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            return atomicLong.incrementAndGet();
        } catch (Exception e) {
            log.error("Failed to increment: key={}", key, e);
            throw new CacheException("Failed to increment", e);
        }
    }

    @Override
    public long decrement(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            return atomicLong.decrementAndGet();
        } catch (Exception e) {
            log.error("Failed to decrement: key={}", key, e);
            throw new CacheException("Failed to decrement", e);
        }
    }

    @Override
    public long incrementBy(String key, long delta) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
            return atomicLong.addAndGet(delta);
        } catch (Exception e) {
            log.error("Failed to increment by: key={}, delta={}", key, delta, e);
            throw new CacheException("Failed to increment by", e);
        }
    }

    @Override
    public int append(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        try {
            RBucket<String> bucket = redissonClient.getBucket(key);
            String currentValue = bucket.get();
            if (currentValue == null) {
                currentValue = "";
            }
            String newValue = currentValue + value;
            bucket.set(newValue);
            return newValue.length();
        } catch (Exception e) {
            log.error("Failed to append: key={}, value={}", key, value, e);
            throw new CacheException("Failed to append", e);
        }
    }

    // ========== 过期时间管理 ==========

    @Override
    public boolean expire(String key, long time, TimeUnit unit) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (unit == null) {
            throw new IllegalArgumentException("TimeUnit cannot be null");
        }
        if (time <= 0) {
            throw new IllegalArgumentException("Time must be positive");
        }

        try {
            RBucket<Object> bucket = redissonClient.getBucket(key);
            return bucket.expire(time, unit);
        } catch (Exception e) {
            log.error("Failed to set expire: key={}, time={}, unit={}", key, time, unit, e);
            throw new CacheException("Failed to set expire", e);
        }
    }

    @Override
    public long getExpire(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            RBucket<Object> bucket = redissonClient.getBucket(key);
            return bucket.remainTimeToLive();
        } catch (Exception e) {
            log.error("Failed to get expire: key={}", key, e);
            throw new CacheException("Failed to get expire", e);
        }
    }

    @Override
    public boolean persist(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            RBucket<Object> bucket = redissonClient.getBucket(key);
            return bucket.clearExpire();
        } catch (Exception e) {
            log.error("Failed to persist: key={}", key, e);
            throw new CacheException("Failed to persist", e);
        }
    }

    // ========== 管道操作 ==========

    @Override
    public <T> List<T> pipeline(List<Function<RBatch, RFuture<T>>> operations) {
        if (operations == null) {
            throw new IllegalArgumentException("Operations cannot be null");
        }
        if (operations.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            RBatch batch = redissonClient.createBatch();
            List<RFuture<T>> futures = new ArrayList<>();

            for (Function<RBatch, RFuture<T>> operation : operations) {
                RFuture<T> future = operation.apply(batch);
                futures.add(future);
            }

            // 执行批量操作
            batch.execute();

            // 获取结果
            List<T> results = new ArrayList<>();
            for (RFuture<T> future : futures) {
                try {
                    T result = future.get();
                    results.add(result);
                } catch (Exception e) {
                    log.warn("Failed to get pipeline result", e);
                    results.add(null);
                }
            }

            return results;
        } catch (Exception e) {
            log.error("Failed to execute pipeline operations", e);
            throw new CacheException("Failed to execute pipeline operations", e);
        }
    }

    // ========== 高级数据结构 - List ==========

    @Override
    public <T> RList<T> getList(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return redissonClient.getList(key);
    }

    @Override
    public int addToList(String key, Object... values) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (values == null) {
            throw new IllegalArgumentException("Values cannot be null");
        }

        try {
            RList<Object> list = redissonClient.getList(key);
            return list.addAll(Arrays.asList(values));
        } catch (Exception e) {
            log.error("Failed to add to list: key={}, values={}", key, Arrays.toString(values), e);
            throw new CacheException("Failed to add to list", e);
        }
    }

    @Override
    public <T> T getFromList(String key, int index) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            RList<T> list = redissonClient.getList(key);
            if (index < 0 || index >= list.size()) {
                return null;
            }
            return list.get(index);
        } catch (Exception e) {
            log.error("Failed to get from list: key={}, index={}", key, index, e);
            throw new CacheException("Failed to get from list", e);
        }
    }

    @Override
    public int getListSize(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            RList<Object> list = redissonClient.getList(key);
            return list.size();
        } catch (Exception e) {
            log.error("Failed to get list size: key={}", key, e);
            throw new CacheException("Failed to get list size", e);
        }
    }

    // ========== 高级数据结构 - Set ==========

    @Override
    public <T> RSet<T> getSet(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return redissonClient.getSet(key);
    }

    @Override
    public int addToSet(String key, Object... values) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (values == null) {
            throw new IllegalArgumentException("Values cannot be null");
        }

        try {
            RSet<Object> set = redissonClient.getSet(key);
            int addedCount = 0;
            for (Object value : values) {
                if (set.add(value)) {
                    addedCount++;
                }
            }
            return addedCount;
        } catch (Exception e) {
            log.error("Failed to add to set: key={}, values={}", key, Arrays.toString(values), e);
            throw new CacheException("Failed to add to set", e);
        }
    }

    @Override
    public boolean containsInSet(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        try {
            RSet<Object> set = redissonClient.getSet(key);
            return set.contains(value);
        } catch (Exception e) {
            log.error("Failed to check set contains: key={}, value={}", key, value, e);
            throw new CacheException("Failed to check set contains", e);
        }
    }

    @Override
    public int getSetSize(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            RSet<Object> set = redissonClient.getSet(key);
            return set.size();
        } catch (Exception e) {
            log.error("Failed to get set size: key={}", key, e);
            throw new CacheException("Failed to get set size", e);
        }
    }

    // ========== 高级数据结构 - SortedSet ==========

    @Override
    public <T> RSortedSet<T> getSortedSet(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return redissonClient.getSortedSet(key);
    }

    @Override
    public boolean addToSortedSet(String key, double score, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        try {
            RSortedSet<Object> sortedSet = redissonClient.getSortedSet(key);
            return sortedSet.add(score, value);
        } catch (Exception e) {
            log.error("Failed to add to sorted set: key={}, score={}, value={}", key, score, value, e);
            throw new CacheException("Failed to add to sorted set", e);
        }
    }

    @Override
    public <T> Set<T> getSortedSetRange(String key, int startRank, int endRank) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            RSortedSet<T> sortedSet = redissonClient.getSortedSet(key);
            return sortedSet.valueRange(startRank, endRank);
        } catch (Exception e) {
            log.error("Failed to get sorted set range: key={}, startRank={}, endRank={}", key, startRank, endRank, e);
            throw new CacheException("Failed to get sorted set range", e);
        }
    }

    // ========== 高级数据结构 - Hash ==========

    @Override
    public <K, V> RMap<K, V> getHash(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        return redissonClient.getMap(key);
    }

    @Override
    public void setHashField(String key, Object field, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        try {
            RMap<Object, Object> map = redissonClient.getMap(key);
            map.put(field, value);
        } catch (Exception e) {
            log.error("Failed to set hash field: key={}, field={}, value={}", key, field, value, e);
            throw new CacheException("Failed to set hash field", e);
        }
    }

    @Override
    public <T> T getHashField(String key, Object field) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null");
        }

        try {
            RMap<Object, T> map = redissonClient.getMap(key);
            return map.get(field);
        } catch (Exception e) {
            log.error("Failed to get hash field: key={}, field={}", key, field, e);
            throw new CacheException("Failed to get hash field", e);
        }
    }

    @Override
    public <K, V> Map<K, V> getHashAll(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            RMap<K, V> map = redissonClient.getMap(key);
            return map.readAllMap();
        } catch (Exception e) {
            log.error("Failed to get hash all: key={}", key, e);
            throw new CacheException("Failed to get hash all", e);
        }
    }

    @Override
    public int getHashSize(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        try {
            RMap<Object, Object> map = redissonClient.getMap(key);
            return map.size();
        } catch (Exception e) {
            log.error("Failed to get hash size: key={}", key, e);
            throw new CacheException("Failed to get hash size", e);
        }
    }
}

