package com.ysmjjsy.goya.component.mybatisplus.audit;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContext;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;

import java.time.Instant;

/**
 * <p>审计字段自动填充处理器</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@RequiredArgsConstructor
public class GoyaMetaObjectHandler implements MetaObjectHandler {

    private static final String FIELD_CREATED_AT = "created_at";
    private static final String FIELD_CREATED_BY = "created_by";
    private static final String FIELD_UPDATED_AT = "updated_at";
    private static final String FIELD_UPDATED_BY = "updated_by";
    private static final String FIELD_TENANT_ID = "tenant_id";

    private final AuditorProvider auditorProvider;

    /**
     * 插入时填充审计字段。
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Instant now = Instant.now();
        String auditor = auditorProvider.currentAuditor();
        String tenantId = TenantContext.get().tenantId();

        strictInsertFill(metaObject, FIELD_CREATED_AT, Instant.class, now);
        strictInsertFill(metaObject, FIELD_UPDATED_AT, Instant.class, now);
        strictInsertFill(metaObject, FIELD_CREATED_BY, String.class, auditor);
        strictInsertFill(metaObject, FIELD_UPDATED_BY, String.class, auditor);
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = DefaultConst.DEFAULT_TENANT_ID;
        }
        strictInsertFill(metaObject, FIELD_TENANT_ID, String.class, tenantId);
    }

    /**
     * 更新时填充审计字段。
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        Instant now = Instant.now();
        String auditor = auditorProvider.currentAuditor();
        strictUpdateFill(metaObject, FIELD_UPDATED_AT, Instant.class, now);
        strictUpdateFill(metaObject, FIELD_UPDATED_BY, String.class, auditor);
    }
}
