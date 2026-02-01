package com.ysmjjsy.goya.component.mybatisplus.permission.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.ysmjjsy.goya.component.framework.security.api.AuthorizeRequest;
import com.ysmjjsy.goya.component.framework.security.api.AuthorizationService;
import com.ysmjjsy.goya.component.framework.security.decision.Decision;
import com.ysmjjsy.goya.component.framework.security.decision.DecisionType;
import com.ysmjjsy.goya.component.framework.security.domain.Action;
import com.ysmjjsy.goya.component.framework.security.context.ResourceContext;
import com.ysmjjsy.goya.component.framework.security.domain.ResourceType;
import com.ysmjjsy.goya.component.framework.security.context.SubjectContext;
import com.ysmjjsy.goya.component.framework.security.domain.SubjectType;
import com.ysmjjsy.goya.component.framework.security.dsl.RangeFilter;
import com.ysmjjsy.goya.component.mybatisplus.configuration.properties.GoyaMybatisPlusProperties;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContext;
import com.ysmjjsy.goya.component.mybatisplus.context.AccessContextValue;
import com.ysmjjsy.goya.component.mybatisplus.context.TenantContext;
import com.ysmjjsy.goya.component.mybatisplus.permission.PermissionContextHolder;
import com.ysmjjsy.goya.component.mybatisplus.permission.SqlRangeFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>动态数据权限处理器</p>
 *
 * <p>仅对查询生效，写入权限由业务侧自行控制。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@Slf4j
@RequiredArgsConstructor
public class GoyaDataPermissionHandler implements MultiDataPermissionHandler {

    private static final String ACTION_QUERY = "QUERY";
    private static final String ACTION_CREATE = "CREATE";
    private static final String ACTION_UPDATE = "UPDATE";
    private static final String ACTION_DELETE = "DELETE";

    private final ObjectProvider<AuthorizationService> authorizationServiceProvider;
    private final GoyaMybatisPlusProperties.Permission options;

    /**
     * 生成数据权限 SQL 片段。
     *
     * @param table 表
     * @param where 原始条件
     * @param mappedStatementId 语句 ID
     * @return 条件表达式
     */
    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        AccessContextValue access = AccessContext.get();
        if (access == null || !StringUtils.hasText(access.subjectId())) {
            return handleNoContext();
        }
        AuthorizationService authorizationService = authorizationServiceProvider.getIfAvailable();
        if (authorizationService == null) {
            return handleNoContext();
        }
        String tableName = table == null ? null : table.getName();
        if (!StringUtils.hasText(tableName)) {
            return handleNoContext();
        }

        try {
            Decision decision = authorizationService.authorize(buildRequest(access, tableName, mappedStatementId));
            if (decision == null) {
                return handleNoContext();
            }
            PermissionContextHolder.putConstraint(tableName, decision.getColumnConstraint());
            if (decision.getDecisionType() == DecisionType.DENY) {
                return denyExpression();
            }
            RangeFilter rowFilter = decision.getRowFilter();
            if (rowFilter instanceof SqlRangeFilter(Expression expression)) {
                return expression;
            }
            return null;
        } catch (Exception ex) {
            log.warn("[Goya] |- component [mybatis-plus] |- permission evaluate failed: {}", ex.getMessage());
            return handleNoContext();
        }
    }

    private AuthorizeRequest buildRequest(AccessContextValue access, String tableName, String mappedStatementId) {
        AuthorizeRequest request = new AuthorizeRequest();
        request.setTenantCode(TenantContext.get().tenantId());
        request.setSubjectContext(buildSubjectContext(access));
        request.setResourceContext(buildResourceContext(tableName, mappedStatementId));
        request.setAction(buildAction());
        request.setRequestTime(LocalDateTime.now());
        return request;
    }

    private SubjectContext buildSubjectContext(AccessContextValue access) {
        SubjectContext context = new SubjectContext();
        context.setSubjectId(access.subjectId());
        SubjectType subjectType = access.subjectType() == null ? SubjectType.USER : access.subjectType();
        context.setSubjectType(subjectType);
        context.setAttributes(access.attributes());
        return context;
    }

    private ResourceContext buildResourceContext(String tableName, String mappedStatementId) {
        ResourceContext context = new ResourceContext();
        context.setResourceCode(tableName);
        context.setResourceType(ResourceType.TABLE);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("tableName", tableName);
        attributes.put("mappedStatementId", mappedStatementId);
        attributes.put("tenantCode", TenantContext.get().tenantId());
        context.setAttributes(attributes);
        return context;
    }

    private Action buildAction() {
        Action action = new Action();
        String actionCode = PermissionContextHolder.getAction();
        if (!StringUtils.hasText(actionCode)) {
            actionCode = ACTION_QUERY;
        }
        action.setCode(actionCode);
        action.setName(resolveActionName(actionCode));
        return action;
    }

    private String resolveActionName(String actionCode) {
        if (ACTION_QUERY.equalsIgnoreCase(actionCode)) {
            return "查询";
        }
        if (ACTION_CREATE.equalsIgnoreCase(actionCode)) {
            return "新增";
        }
        if (ACTION_UPDATE.equalsIgnoreCase(actionCode)) {
            return "更新";
        }
        if (ACTION_DELETE.equalsIgnoreCase(actionCode)) {
            return "删除";
        }
        return actionCode;
    }

    private Expression handleNoContext() {
        if (options.failClosed()) {
            return denyExpression();
        }
        return null;
    }

    private Expression denyExpression() {
        try {
            return CCJSqlParserUtil.parseCondExpression("1 = 0");
        } catch (Exception _) {
            return null;
        }
    }
}
