package com.ysmjjsy.goya.component.common.utils;

import lombok.experimental.UtilityClass;

import java.awt.Color;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p>随机数工具类</p>
 * <p>提供各种随机数生成方法，包括整数、浮点数、字符串、字节数组等</p>
 * <p>不依赖第三方库，使用 Java 标准库实现</p>
 *
 * @author goya
 * @since 2025/12/7 19:46
 */
@UtilityClass
public class RandomUtils {

    /**
     * 线程安全的随机数生成器（用于安全场景）
     */
    private static final ThreadLocal<SecureRandom> SECURE_RANDOM = ThreadLocal.withInitial(SecureRandom::new);

    /**
     * 默认字符集（数字+字母，大小写）
     */
    private static final String DEFAULT_CHARSET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 数字字符集
     */
    private static final String NUMBER_CHARSET = "0123456789";

    /**
     * 字母字符集（大小写）
     */
    private static final String LETTER_CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 小写字母字符集
     */
    private static final String LOWER_LETTER_CHARSET = "abcdefghijklmnopqrstuvwxyz";

    /**
     * 大写字母字符集
     */
    private static final String UPPER_LETTER_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 数字和大写字母字符集
     */
    private static final String NUMBER_UPPER_CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    // ==================== 随机整数 ====================

    /**
     * 生成随机整数（0 到 Integer.MAX_VALUE）
     *
     * @return 随机整数
     */
    public static int randomInt() {
        return ThreadLocalRandom.current().nextInt();
    }

    /**
     * 生成指定范围内的随机整数 [0, bound)
     *
     * @param bound 上界（不包含）
     * @return 随机整数
     */
    public static int randomInt(int bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        return ThreadLocalRandom.current().nextInt(bound);
    }

    /**
     * 生成指定范围内的随机整数 [origin, bound)
     *
     * @param origin 起始值（包含）
     * @param bound  上界（不包含）
     * @return 随机整数
     */
    public static int randomInt(int origin, int bound) {
        if (origin >= bound) {
            throw new IllegalArgumentException("Origin must be less than bound");
        }
        return ThreadLocalRandom.current().nextInt(origin, bound);
    }

    /**
     * 生成随机整数数组
     *
     * @param length 数组长度
     * @param bound  上界（不包含）
     * @return 随机整数数组
     */
    public static int[] randomInts(int length, int bound) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        int[] result = new int[length];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            result[i] = random.nextInt(bound);
        }
        return result;
    }

    /**
     * 生成随机整数数组
     *
     * @param length 数组长度
     * @param origin 起始值（包含）
     * @param bound  上界（不包含）
     * @return 随机整数数组
     */
    public static int[] randomInts(int length, int origin, int bound) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        int[] result = new int[length];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            result[i] = random.nextInt(origin, bound);
        }
        return result;
    }

    // ==================== 随机长整数 ====================

    /**
     * 生成随机长整数（0 到 Long.MAX_VALUE）
     *
     * @return 随机长整数
     */
    public static long randomLong() {
        return ThreadLocalRandom.current().nextLong();
    }

    /**
     * 生成指定范围内的随机长整数 [0, bound)
     *
     * @param bound 上界（不包含）
     * @return 随机长整数
     */
    public static long randomLong(long bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        return ThreadLocalRandom.current().nextLong(bound);
    }

    /**
     * 生成指定范围内的随机长整数 [origin, bound)
     *
     * @param origin 起始值（包含）
     * @param bound  上界（不包含）
     * @return 随机长整数
     */
    public static long randomLong(long origin, long bound) {
        if (origin >= bound) {
            throw new IllegalArgumentException("Origin must be less than bound");
        }
        return ThreadLocalRandom.current().nextLong(origin, bound);
    }

    // ==================== 随机浮点数 ====================

    /**
     * 生成随机浮点数 [0.0, 1.0)
     *
     * @return 随机浮点数
     */
    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * 生成指定范围内的随机浮点数 [0.0, bound)
     *
     * @param bound 上界（不包含）
     * @return 随机浮点数
     */
    public static double randomDouble(double bound) {
        if (bound <= 0) {
            throw new IllegalArgumentException("Bound must be positive");
        }
        return ThreadLocalRandom.current().nextDouble(bound);
    }

    /**
     * 生成指定范围内的随机浮点数 [origin, bound)
     *
     * @param origin 起始值（包含）
     * @param bound  上界（不包含）
     * @return 随机浮点数
     */
    public static double randomDouble(double origin, double bound) {
        if (origin >= bound) {
            throw new IllegalArgumentException("Origin must be less than bound");
        }
        return ThreadLocalRandom.current().nextDouble(origin, bound);
    }

    /**
     * 生成随机浮点数 [0.0f, 1.0f)
     *
     * @return 随机浮点数
     */
    public static float randomFloat() {
        return ThreadLocalRandom.current().nextFloat();
    }

    // ==================== 随机布尔值 ====================

    /**
     * 生成随机布尔值
     *
     * @return 随机布尔值
     */
    public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    // ==================== 随机字符串 ====================

    /**
     * 生成随机字符串（默认字符集：数字+字母）
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        return randomString(length, DEFAULT_CHARSET);
    }

    /**
     * 生成随机字符串（指定字符集）
     *
     * @param length  长度
     * @param charset 字符集
     * @return 随机字符串
     */
    public static String randomString(int length, String charset) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        if (charset == null || charset.isEmpty()) {
            throw new IllegalArgumentException("Charset cannot be null or empty");
        }

        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int charsetLength = charset.length();

        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(random.nextInt(charsetLength)));
        }

        return sb.toString();
    }

    /**
     * 生成随机数字字符串
     *
     * @param length 长度
     * @return 随机数字字符串
     */
    public static String randomNumberString(int length) {
        return randomString(length, NUMBER_CHARSET);
    }

    /**
     * 生成随机字母字符串（大小写）
     *
     * @param length 长度
     * @return 随机字母字符串
     */
    public static String randomLetterString(int length) {
        return randomString(length, LETTER_CHARSET);
    }

    /**
     * 生成随机小写字母字符串
     *
     * @param length 长度
     * @return 随机小写字母字符串
     */
    public static String randomLowerLetterString(int length) {
        return randomString(length, LOWER_LETTER_CHARSET);
    }

    /**
     * 生成随机大写字母字符串
     *
     * @param length 长度
     * @return 随机大写字母字符串
     */
    public static String randomUpperLetterString(int length) {
        return randomString(length, UPPER_LETTER_CHARSET);
    }

    // ==================== 随机字节数组 ====================

    /**
     * 生成随机字节数组
     *
     * @param length 长度
     * @return 随机字节数组
     */
    public static byte[] randomBytes(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        byte[] bytes = new byte[length];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    }

    /**
     * 生成安全的随机字节数组（使用 SecureRandom）
     *
     * @param length 长度
     * @return 随机字节数组
     */
    public static byte[] randomSecureBytes(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        byte[] bytes = new byte[length];
        SECURE_RANDOM.get().nextBytes(bytes);
        return bytes;
    }

    // ==================== 集合随机操作 ====================

    /**
     * 从集合中随机选择一个元素
     *
     * @param collection 集合
     * @param <T>        元素类型
     * @return 随机元素，如果集合为空返回 null
     */
    public static <T> T randomElement(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return null;
        }
        int size = collection.size();
        int index = randomInt(size);
        int i = 0;
        for (T element : collection) {
            if (i == index) {
                return element;
            }
            i++;
        }
        return null;
    }

    /**
     * 从数组中随机选择一个元素
     *
     * @param array 数组
     * @param <T>   元素类型
     * @return 随机元素，如果数组为空返回 null
     */
    public static <T> T randomElement(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        return array[randomInt(array.length)];
    }

    /**
     * 从集合中随机选择多个元素（不重复）
     *
     * @param collection 集合
     * @param count      选择数量
     * @param <T>        元素类型
     * @return 随机元素列表
     */
    public static <T> List<T> randomElements(Collection<T> collection, int count) {
        if (collection == null || collection.isEmpty() || count <= 0) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<>(collection);
        int size = list.size();
        count = Math.min(count, size);

        Collections.shuffle(list, new Random(ThreadLocalRandom.current().nextLong()));
        return list.subList(0, count);
    }

    /**
     * 打乱集合顺序
     *
     * @param list 列表
     * @param <T>  元素类型
     */
    public static <T> void shuffle(List<T> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Collections.shuffle(list, new Random(ThreadLocalRandom.current().nextLong()));
    }

    /**
     * 打乱数组顺序
     *
     * @param array 数组
     * @param <T>   元素类型
     */
    public static <T> void shuffle(T[] array) {
        if (array == null || array.length == 0) {
            return;
        }
        List<T> list = Arrays.asList(array);
        Collections.shuffle(list, new Random(ThreadLocalRandom.current().nextLong()));
        // 注意：Arrays.asList 返回的列表是固定大小的，修改会影响原数组
        // 这里需要手动复制
        for (int i = 0; i < array.length; i++) {
            array[i] = list.get(i);
        }
    }

    // ==================== UUID 相关 ====================

    /**
     * 生成随机 UUID 字符串（带连字符）
     *
     * @return UUID 字符串
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成随机 UUID 字符串（不带连字符）
     *
     * @return UUID 字符串
     */
    public static String randomSimpleUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // ==================== 随机颜色 ====================

    /**
     * 生成随机颜色
     *
     * @return 随机颜色
     */
    public static Color randomColor() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    /**
     * 生成随机颜色（指定透明度）
     *
     * @param alpha 透明度（0-255）
     * @return 随机颜色
     */
    public static Color randomColor(int alpha) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256), alpha);
    }

    // ==================== 其他工具方法 ====================

    /**
     * 生成指定范围内的随机整数（包含边界）
     * <p>与 randomInt 不同，此方法包含上界</p>
     *
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 随机整数
     */
    public static int randomIntInclusive(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("Min must be less than or equal to max");
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * 生成指定范围内的随机长整数（包含边界）
     *
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 随机长整数
     */
    public static long randomLongInclusive(long min, long max) {
        if (min > max) {
            throw new IllegalArgumentException("Min must be less than or equal to max");
        }
        return ThreadLocalRandom.current().nextLong(min, max + 1);
    }

    /**
     * 生成指定范围内的随机浮点数（包含边界）
     *
     * @param min 最小值（包含）
     * @param max 最大值（包含）
     * @return 随机浮点数
     */
    public static double randomDoubleInclusive(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("Min must be less than or equal to max");
        }
        if (min == max) {
            return min;
        }
        return ThreadLocalRandom.current().nextDouble(min, Math.nextUp(max));
    }

    /**
     * 从字符串列表中随机选择指定数量的元素（不重复）
     *
     * @param words     字符串列表
     * @param wordCount 选择数量
     * @return 随机选择的字符串列表
     */
    public static List<String> randomEles(List<String> words, int wordCount) {
        if (words == null || words.isEmpty() || wordCount <= 0) {
            return Collections.emptyList();
        }

        List<String> list = new ArrayList<>(words);
        int size = list.size();
        wordCount = Math.min(wordCount, size);

        Collections.shuffle(list, new Random(ThreadLocalRandom.current().nextLong()));
        return list.subList(0, wordCount);
    }

    /**
     * 生成随机整数数组（全范围）
     *
     * @param size 数组大小
     * @return 随机整数数组
     */
    public static int[] randomInts(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        int[] result = new int[size];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < size; i++) {
            result[i] = random.nextInt();
        }
        return result;
    }

    /**
     * 获得一个随机的字符串（只包含数字和大写字符）
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomStringUpper(int length) {
        return randomString(length, NUMBER_UPPER_CHARSET);
    }
}
