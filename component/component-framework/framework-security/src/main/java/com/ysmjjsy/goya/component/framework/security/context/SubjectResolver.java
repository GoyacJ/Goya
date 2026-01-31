package com.ysmjjsy.goya.component.framework.security.context;

import com.ysmjjsy.goya.component.framework.security.domain.Subject;
import org.jspecify.annotations.NonNull;
import org.springframework.validation.annotation.Validated;

/**
 * <p>主体解析器。</p>
 *
 * @author goya
 * @since 2026/1/31 10:00
 */
public interface SubjectResolver {

    /**
     * 解析主体信息。
     *
     * @param context 主体上下文
     * @return 解析后的主体
     */
    Subject resolve(@Validated @NonNull SubjectContext context);
}
