package com.ysmjjsy.goya.component.core.utils;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>身份标识工具类</p>
 * <p>提供多种唯一 ID 生成方法，包括 UUID、雪花算法、NanoId、ObjectId 等</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 18:30
 */
@UtilityClass
public class GoyaIdUtils {

    // ==================== 常量定义 ====================

    /**
     * NanoId 默认字符集（URL 友好）
     */
    private static final String NANOID_ALPHABET = "_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * NanoId 默认长度
     */
    private static final int NANOID_DEFAULT_SIZE = 21;

    /**
     * ObjectId 字符集（十六进制）
     */
    private static final String HEX_ALPHABET = "0123456789abcdef";

    /**
     * 雪花算法：时间戳起始点（2020-01-01 00:00:00）
     */
    private static final long EPOCH = 1577836800000L;

    /**
     * 雪花算法：机器 ID 位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 雪花算法：数据中心 ID 位数
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /**
     * 雪花算法：序列号位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 雪花算法：机器 ID 最大值
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 雪花算法：数据中心 ID 最大值
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /**
     * 雪花算法：序列号掩码
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    /**
     * 雪花算法：机器 ID 左移位数
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;

    /**
     * 雪花算法：数据中心 ID 左移位数
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;

    /**
     * 雪花算法：时间戳左移位数
     */
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    // ==================== 雪花算法实例 ====================

    /**
     * 默认雪花算法生成器（单例）
     */
    private static class DefaultSnowflake {
        private final long workerId;
        private final long datacenterId;
        private final AtomicLong sequence = new AtomicLong(0);
        private volatile long lastTimestamp = -1L;

        DefaultSnowflake() {
            // 使用默认值：workerId=0, datacenterId=0
            this(0L, 0L);
        }

        DefaultSnowflake(long workerId, long datacenterId) {
            if (workerId > MAX_WORKER_ID || workerId < 0) {
                throw new IllegalArgumentException("Worker ID must be between 0 and " + MAX_WORKER_ID);
            }
            if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
                throw new IllegalArgumentException("Datacenter ID must be between 0 and " + MAX_DATACENTER_ID);
            }
            this.workerId = workerId;
            this.datacenterId = datacenterId;
        }

        synchronized long nextId() {
            long timestamp = System.currentTimeMillis();

            if (timestamp < lastTimestamp) {
                throw new CommonException("Clock moved backwards. Refusing to generate id");
            }

            if (timestamp == lastTimestamp) {
                long seq = sequence.incrementAndGet() & SEQUENCE_MASK;
                if (seq == 0) {
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence.set(0);
            }

            lastTimestamp = timestamp;

            return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                    | (datacenterId << DATACENTER_ID_SHIFT)
                    | (workerId << WORKER_ID_SHIFT)
                    | sequence.get();
        }

        private long tilNextMillis(long lastTimestamp) {
            long timestamp = System.currentTimeMillis();
            while (timestamp <= lastTimestamp) {
                timestamp = System.currentTimeMillis();
            }
            return timestamp;
        }
    }

    /**
     * 默认雪花算法生成器实例
     */
    private static final DefaultSnowflake DEFAULT_SNOWFLAKE = new DefaultSnowflake();

    /**
     * 线程安全的随机数生成器（用于 NanoId）
     */
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM = ThreadLocal.withInitial(SecureRandom::new);

    /**
     * 普通随机数生成器（用于 ObjectId）
     */
    private static final Random RANDOM = new Random();

    // ==================== UUID 相关 ====================

    /**
     * 生成标准 UUID（带连字符）
     * <p>格式：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx</p>
     * <p>示例：f81d4fae-7dec-11d0-a765-00a0c91e6bf6</p>
     *
     * @return UUID 字符串
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成简单 UUID（不带连字符）
     * <p>格式：xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</p>
     * <p>示例：f81d4fae7dec11d0a76500a0c91e6bf6</p>
     *
     * @return UUID 字符串
     */
    public static String simpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 快速生成 UUID（带连字符）
     * <p>使用 ThreadLocalRandom 生成，性能优于 randomUUID()</p>
     * <p>格式：xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx</p>
     *
     * @return UUID 字符串
     */
    public static String fastUUID() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong()).toString();
    }

    /**
     * 快速生成简单 UUID（不带连字符）
     * <p>使用 ThreadLocalRandom 生成，性能优于 simpleUUID()</p>
     * <p>格式：xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx</p>
     * <p>框架中常用方法，用于生成各种标识符</p>
     *
     * @return UUID 字符串
     */
    public static String fastSimpleUUID() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong()).toString().replace("-", "");
    }

    /**
     * 生成 UUID 对象
     *
     * @return UUID 对象
     */
    public static UUID createUUID() {
        return UUID.randomUUID();
    }

    // ==================== 雪花算法相关 ====================

    /**
     * 生成雪花算法 ID（字符串格式）
     * <p>框架中用于生成实体主键的默认方法</p>
     * <p>使用默认的 workerId=0 和 datacenterId=0</p>
     *
     * @return 雪花算法 ID 字符串
     */
    public static String getSeataSnowflakeNextIdStr() {
        return String.valueOf(DEFAULT_SNOWFLAKE.nextId());
    }

    /**
     * 生成雪花算法 ID（长整型）
     * <p>使用默认的 workerId 和 datacenterId</p>
     *
     * @return 雪花算法 ID
     */
    public static long getSnowflakeNextId() {
        return DEFAULT_SNOWFLAKE.nextId();
    }

    /**
     * 使用指定 workerId 和 datacenterId 生成雪花算法 ID
     *
     * @param workerId     工作机器 ID（0-31）
     * @param datacenterId 数据中心 ID（0-31）
     * @return 雪花算法 ID
     */
    public static long getSnowflakeNextId(long workerId, long datacenterId) {
        return new DefaultSnowflake(workerId, datacenterId).nextId();
    }

    /**
     * 使用指定 workerId 和 datacenterId 生成雪花算法 ID（字符串格式）
     *
     * @param workerId     工作机器 ID（0-31）
     * @param datacenterId 数据中心 ID（0-31）
     * @return 雪花算法 ID 字符串
     */
    public static String getSnowflakeNextIdStr(long workerId, long datacenterId) {
        return String.valueOf(getSnowflakeNextId(workerId, datacenterId));
    }

    // ==================== NanoId 相关 ====================

    /**
     * 生成 NanoId
     * <p>NanoId 是一个小巧、安全、URL 友好的唯一字符串 ID 生成器</p>
     * <p>默认长度为 21 个字符</p>
     *
     * @return NanoId 字符串
     */
    public static String nanoId() {
        return nanoId(NANOID_DEFAULT_SIZE);
    }

    /**
     * 生成指定长度的 NanoId
     * <p>框架中用于生成用户名后缀等场景</p>
     *
     * @param size 长度
     * @return NanoId 字符串
     */
    public static String nanoId(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }

        SecureRandom random = SECURE_RANDOM.get();
        StringBuilder sb = new StringBuilder(size);
        int alphabetLength = NANOID_ALPHABET.length();

        for (int i = 0; i < size; i++) {
            sb.append(NANOID_ALPHABET.charAt(random.nextInt(alphabetLength)));
        }

        return sb.toString();
    }

    // ==================== ObjectId 相关 ====================

    /**
     * 生成 MongoDB 风格的 ObjectId
     * <p>格式：24 位十六进制字符串</p>
     * <p>结构：8位时间戳 + 6位机器标识 + 4位进程ID + 6位随机数</p>
     * <p>示例：507f1f77bcf86cd799439011</p>
     *
     * @return ObjectId 字符串
     */
    public static String objectId() {
        // 8位：时间戳（秒）
        long timestamp = Instant.now().getEpochSecond();
        String timestampHex = Long.toHexString(timestamp);

        // 6位：机器标识（使用随机数模拟）
        String machineId = generateHexString(6);

        // 4位：进程ID（使用随机数模拟）
        String processId = generateHexString(4);

        // 6位：随机数
        String random = generateHexString(6);

        // 补齐时间戳到8位
        if (timestampHex.length() < 8) {
            timestampHex = String.format("%08s", timestampHex).replace(' ', '0');
        } else if (timestampHex.length() > 8) {
            timestampHex = timestampHex.substring(timestampHex.length() - 8);
        }

        return timestampHex + machineId + processId + random;
    }

    /**
     * 生成指定长度的十六进制字符串
     *
     * @param length 长度
     * @return 十六进制字符串
     */
    private static String generateHexString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(HEX_ALPHABET.charAt(RANDOM.nextInt(HEX_ALPHABET.length())));
        }
        return sb.toString();
    }

    /**
     * 生成短标识符
     * <p>使用 NanoId，适用于需要短 ID 的场景，如验证码、短链接等</p>
     *
     * @param length 长度
     * @return 短标识符
     */
    public static String generateShortId(int length) {
        return nanoId(length);
    }

    /**
     * 生成短标识符（默认长度 6）
     * <p>框架中用于生成用户名后缀等场景</p>
     *
     * @return 短标识符
     */
    public static String generateShortId() {
        return nanoId(6);
    }
}
