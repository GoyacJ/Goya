package com.ysmjjsy.goya.component.framework.servlet.converter;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 字符串到集合转换器工厂
 * <p>
 * 支持将逗号分隔的字符串自动转换为 List 或 Set
 * <p>
 * 使用场景：
 * <ul>
 *   <li>@RequestParam List<Long> ids - 自动将 "1,2,3" 转换为 [1, 2, 3]</li>
 *   <li>@RequestParam Set<String> tags - 自动将 "tag1,tag2" 转换为 {tag1, tag2}</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>{@code
 * @GetMapping("/users")
 * public List<User> getUsers(@RequestParam List<Long> ids) {
 *     // ids 参数会自动从 "1,2,3" 转换为 [1L, 2L, 3L]
 * }
 * }</pre>
 *
 * @author goya
 * @since 2025/12/20
 */
@Slf4j
public class StringToCollectionConverterFactory implements ConverterFactory<String, Collection<?>> {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    @NullMarked
    public <T extends Collection<?>> Converter<String, T> getConverter(Class<T> targetType) {
        return new StringToCollectionConverter(targetType);
    }

    /**
     * 字符串到集合转换器
     */
    private record StringToCollectionConverter<T extends Collection<Object>>(Class<T> collectionType)
            implements Converter<String, T> {

        // 简化处理：假设元素类型为 String，实际使用时可以通过泛型获取

        @Override
        public T convert(String source) {
            if (!StringUtils.hasText(source)) {
                return createEmptyCollection();
            }

            // 分割字符串（支持逗号、分号、空格分隔）
            String[] parts = source.split("[,;\\s]+");

            // 过滤空字符串
            List<String> elements = Arrays.stream(parts)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .toList();

            // 创建集合并添加元素
            T collection = createEmptyCollection();
            collection.addAll(elements);

            return collection;
        }

        @SuppressWarnings("unchecked")
        private T createEmptyCollection() {
            if (List.class.isAssignableFrom(collectionType)) {
                return (T) new ArrayList<>();
            } else if (Set.class.isAssignableFrom(collectionType)) {
                return (T) new LinkedHashSet<>(); // 保持顺序
            } else {
                try {
                    return collectionType.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    log.warn("[Goya] |- 无法创建集合实例: {}, 使用 ArrayList", collectionType.getName());
                    return (T) new ArrayList<>();
                }
            }
        }
    }
}