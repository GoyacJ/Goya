package com.ysmjjsy.goya.component.mybatisplus.context;

import com.ysmjjsy.goya.component.framework.security.domain.SubjectType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * <p>访问上下文值对象</p>
 *
 * <p>用于构建 framework-security 的 SubjectContext。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
public record AccessContextValue(
        String subjectId,
        SubjectType subjectType,
        String userId,
        Map<String, Object> attributes
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1880428991606937690L;

    /**
     * 空上下文。
     *
     * @return 空对象
     */
    public static AccessContextValue empty() {
        return new AccessContextValue(null, null, null, Collections.emptyMap());
    }
}
