package com.ysmjjsy.goya.component.mybatisplus.permission.handler;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.ysmjjsy.goya.component.framework.security.event.PermissionChangeEvent;
import com.ysmjjsy.goya.component.framework.security.event.PermissionChangeType;
import com.ysmjjsy.goya.component.framework.security.spi.PermissionChangePublisher;
import com.ysmjjsy.goya.component.mybatisplus.permission.entity.DataResourceEntity;
import com.ysmjjsy.goya.component.mybatisplus.permission.entity.DataResourcePolicyEntity;
import com.ysmjjsy.goya.component.mybatisplus.permission.mapper.DataResourceMapper;
import com.ysmjjsy.goya.component.mybatisplus.permission.mapper.DataResourcePolicyMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>权限变更事件拦截器。</p>
 *
 * <p>捕获 data_resource / data_resource_policy 的写操作并发布变更事件。</p>
 *
 * @author goya
 * @since 2026/2/01
 */
@RequiredArgsConstructor
public class PermissionChangeInnerInterceptor implements InnerInterceptor {

    private static final String POLICY_MAPPER = DataResourcePolicyMapper.class.getName();
    private static final String RESOURCE_MAPPER = DataResourceMapper.class.getName();

    private final PermissionChangePublisher publisher;

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) {
        if (publisher == null || ms == null) {
            return;
        }
        String statementId = ms.getId();
        if (!StringUtils.hasText(statementId)) {
            return;
        }
        boolean policyChange = statementId.startsWith(POLICY_MAPPER);
        boolean resourceChange = statementId.startsWith(RESOURCE_MAPPER);
        if (!policyChange && !resourceChange) {
            return;
        }

        PermissionChangeType changeType = resolveChangeType(ms.getSqlCommandType());
        if (changeType == null) {
            return;
        }

        PermissionChangeEvent event = buildEvent(ms, parameter, changeType, policyChange, resourceChange);
        publisher.publish(event);
    }

    private PermissionChangeEvent buildEvent(MappedStatement ms,
                                             Object parameter,
                                             PermissionChangeType changeType,
                                             boolean policyChange,
                                             boolean resourceChange) {
        PermissionChangeEvent event = new PermissionChangeEvent();
        event.setChangeType(changeType);
        event.setChangedAt(LocalDateTime.now());

        Object entity = unwrapEntity(parameter);
        if (policyChange && entity instanceof DataResourcePolicyEntity policyEntity) {
            event.setTenantCode(policyEntity.getTenantCode());
            event.setPolicyId(policyEntity.getId());
            event.setResourceCode(policyEntity.getResourceCode());
        } else if (resourceChange && entity instanceof DataResourceEntity resourceEntity) {
            event.setTenantCode(resourceEntity.getTenantCode());
            event.setResourceCode(resourceEntity.getResourceCode());
        } else if (policyChange) {
            event.setPolicyId(extractId(parameter));
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("statementId", ms.getId());
        if (ms.getSqlCommandType() != null) {
            attributes.put("sqlCommand", ms.getSqlCommandType().name());
        }
        event.setAttributes(attributes);
        return event;
    }

    private Object unwrapEntity(Object parameter) {
        if (parameter == null) {
            return null;
        }
        if (parameter instanceof DataResourcePolicyEntity || parameter instanceof DataResourceEntity) {
            return parameter;
        }
        if (parameter instanceof Wrapper<?> wrapper) {
            return wrapper.getEntity();
        }
        if (parameter instanceof Map<?, ?> map) {
            Object entity = map.get("et");
            if (entity == null) {
                entity = map.get("entity");
            }
            if (entity == null) {
                entity = map.get("param1");
            }
            if (entity == null) {
                entity = map.get("record");
            }
            if (entity instanceof Wrapper<?> wrapper) {
                return wrapper.getEntity();
            }
            return entity;
        }
        return null;
    }

    private String extractId(Object parameter) {
        if (parameter == null) {
            return null;
        }
        if (parameter instanceof DataResourcePolicyEntity policyEntity) {
            return policyEntity.getId();
        }
        if (parameter instanceof Map<?, ?> map) {
            Object id = map.get("id");
            return id == null ? null : id.toString();
        }
        if (parameter instanceof Wrapper<?>) {
            return null;
        }
        return parameter.toString();
    }

    private PermissionChangeType resolveChangeType(SqlCommandType commandType) {
        if (commandType == null) {
            return null;
        }
        return switch (commandType) {
            case INSERT -> PermissionChangeType.CREATE;
            case UPDATE -> PermissionChangeType.UPDATE;
            case DELETE -> PermissionChangeType.DELETE;
            default -> null;
        };
    }
}
