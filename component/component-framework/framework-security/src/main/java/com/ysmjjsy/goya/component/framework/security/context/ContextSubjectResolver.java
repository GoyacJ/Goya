package com.ysmjjsy.goya.component.framework.security.context;

import com.ysmjjsy.goya.component.framework.security.domain.Subject;
import org.jspecify.annotations.NonNull;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        Map<String, Object> attributes = context.getAttributes();
        subject.setAttributes(attributes);
        subject.setRoleIds(extractIds(attributes, "roleIds"));
        subject.setTeamIds(extractIds(attributes, "teamIds"));
        subject.setOrgIds(extractIds(attributes, "orgIds"));
        return subject;
    }

    private List<String> extractIds(Map<String, Object> attributes, String key) {
        if (attributes == null || !StringUtils.hasText(key)) {
            return Collections.emptyList();
        }
        Object value = attributes.get(key);
        if (value instanceof Collection<?> collection) {
            List<String> result = new ArrayList<>();
            for (Object item : collection) {
                if (item == null) {
                    continue;
                }
                String text = item.toString().trim();
                if (StringUtils.hasText(text)) {
                    result.add(text);
                }
            }
            return result;
        }
        if (value instanceof String text) {
            if (!StringUtils.hasText(text)) {
                return Collections.emptyList();
            }
            String[] parts = text.split(",");
            List<String> result = new ArrayList<>();
            for (String part : parts) {
                if (StringUtils.hasText(part)) {
                    result.add(part.trim());
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
}
