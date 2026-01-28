package com.ysmjjsy.goya.component.mybatisplus.permission.rule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ysmjjsy.goya.component.framework.core.json.GoyaJson;
import com.ysmjjsy.goya.component.mybatisplus.permission.model.RuleSet;
import com.ysmjjsy.goya.component.mybatisplus.permission.store.PermissionRuleStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <p></p>
 * <p>
 * <b>读取语义：</b>
 * <ul>
 *   <li>load：按 (tenantId, subjectId, resource) 读取规则集 JSON 并反序列化为 RuleSet</li>
 *   <li>version：按 (tenantId, subjectId) 读取主体版本号</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/29 00:22
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MybatisPlusPermissionRuleStore implements PermissionRuleStore {

    private final PermissionRuleSetMapper ruleSetMapper;
    private final PermissionSubjectVersionMapper versionMapper;

    @Override
    public RuleSet load(String tenantId, String subjectId, String resource) {
        if (isBlank(tenantId) || isBlank(subjectId) || isBlank(resource)) {
            return null;
        }

        LambdaQueryWrapper<PermissionRuleSetEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(PermissionRuleSetEntity::getTenantId, tenantId)
                .eq(PermissionRuleSetEntity::getSubjectId, subjectId)
                .eq(PermissionRuleSetEntity::getResource, resource)
                .eq(PermissionRuleSetEntity::getEnabled, true);

        PermissionRuleSetEntity entity = ruleSetMapper.selectOne(qw);
        if (entity == null) {
            return null;
        }

        RuleSet ruleSet = GoyaJson.fromJson(entity.getRuleJson(), RuleSet.class);
        if (ruleSet == null) {
            log.warn(
                    "权限规则 JSON 反序列化失败，将忽略该规则集: tenantId={}, subjectId={}, resource={}",
                    tenantId, subjectId, resource
            );
            return null;
        }

        return ruleSet;
    }

    @Override
    public long version(String tenantId, String subjectId) {
        if (isBlank(tenantId) || isBlank(subjectId)) {
            return 0L;
        }

        LambdaQueryWrapper<PermissionSubjectVersionEntity> qw = new LambdaQueryWrapper<>();
        qw.select(PermissionSubjectVersionEntity::getVersion)
                .eq(PermissionSubjectVersionEntity::getTenantId, tenantId)
                .eq(PermissionSubjectVersionEntity::getSubjectId, subjectId);

        PermissionSubjectVersionEntity v = versionMapper.selectOne(qw);
        if (v == null || v.getVersion() == null) {
            return 0L;
        }
        return v.getVersion();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}