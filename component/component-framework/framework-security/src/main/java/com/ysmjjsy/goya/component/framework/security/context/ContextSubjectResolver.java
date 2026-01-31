package com.ysmjjsy.goya.component.framework.security.context;

import com.ysmjjsy.goya.component.framework.security.domain.Subject;
import org.jspecify.annotations.NonNull;

/**
 * <p>基于上下文的主体解析器。</p>
 *
 * @author goya
 * @since 2026/1/31 11:10
 */
public class ContextSubjectResolver implements SubjectResolver {

    /**
     * 解析主体信息。
     *
     * @param context 主体上下文
     * @return 解析后的主体
     */
    @Override
    public Subject resolve(@NonNull SubjectContext context) {
        Subject subject = new Subject();
        subject.setSubjectId(context.getSubjectId());
        subject.setSubjectType(context.getSubjectType());
        subject.setAttributes(context.getAttributes());
        return subject;
    }
}
