package com.ysmjjsy.goya.component.mybatisplus.permission.rule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;

/**
 * <p>权限规则治理服务</p>
 * <p>
 * 所有对规则集的新增/修改/删除都应通过该服务完成，以保证 subject 版本号递增，从而让缓存快速生效。
 *
 * @author goya
 * @since 2026/1/29 00:22
 */
@Service
public class PermissionRuleAdminService extends ServiceImpl<PermissionRuleSetMapper, PermissionRuleSetEntity> {

    private final PermissionSubjectVersionMapper versionMapper;

    public PermissionRuleAdminService(PermissionSubjectVersionMapper versionMapper) {
        this.versionMapper = Objects.requireNonNull(versionMapper, "versionMapper 不能为空");
    }

    /**
     * 创建或更新规则集（subject+resource 幂等）。
     * <p>
     * 操作完成后将递增 (tenantId, subjectId) 对应的 subject version。
     *
     * @param tenantId 租户ID
     * @param subjectId 主体ID
     * @param resource 资源
     * @param ruleJson 规则集JSON
     * @param enabled 是否启用
     */
    @Transactional
    public void upsert(String tenantId, String subjectId, String resource, String ruleJson, boolean enabled) {
        Objects.requireNonNull(tenantId);
        Objects.requireNonNull(subjectId);
        Objects.requireNonNull(resource);
        Objects.requireNonNull(ruleJson);

        LambdaQueryWrapper<PermissionRuleSetEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(PermissionRuleSetEntity::getTenantId, tenantId)
                .eq(PermissionRuleSetEntity::getSubjectId, subjectId)
                .eq(PermissionRuleSetEntity::getResource, resource);

        PermissionRuleSetEntity e = getOne(qw, false);
        if (e == null) {
            e = new PermissionRuleSetEntity();
            e.setTenantId(tenantId);
            e.setSubjectId(subjectId);
            e.setResource(resource);
        }
        e.setRuleJson(ruleJson);
        e.setEnabled(enabled);

        if (e.getId() == null) {
            save(e);
        } else {
            updateById(e);
        }

        bumpSubjectVersion(tenantId, subjectId);
    }

    /**
     * 删除规则集，并递增 subject version。
     *
     * @param tenantId 租户ID
     * @param subjectId 主体ID
     * @param resource 资源
     */
    @Transactional
    public void delete(String tenantId, String subjectId, String resource) {
        LambdaQueryWrapper<PermissionRuleSetEntity> qw = new LambdaQueryWrapper<>();
        qw.eq(PermissionRuleSetEntity::getTenantId, tenantId)
                .eq(PermissionRuleSetEntity::getSubjectId, subjectId)
                .eq(PermissionRuleSetEntity::getResource, resource);

        remove(qw);
        bumpSubjectVersion(tenantId, subjectId);
    }

    /**
     * 递增主体版本号。
     * <p>
     * 不依赖数据库触发器，纯应用内维护。
     */
    private void bumpSubjectVersion(String tenantId, String subjectId) {
        // 1) 先尝试 update version=version+1
        LambdaUpdateWrapper<PermissionSubjectVersionEntity> uw = new LambdaUpdateWrapper<>();
        uw.eq(PermissionSubjectVersionEntity::getTenantId, tenantId)
                .eq(PermissionSubjectVersionEntity::getSubjectId, subjectId)
                .setSql("version = version + 1")
                .set(PermissionSubjectVersionEntity::getUpdatedAt, Instant.now());

        int updated = versionMapper.update(null, uw);

        // 2) 如果不存在则插入初始版本 1
        if (updated == 0) {
            PermissionSubjectVersionEntity v = new PermissionSubjectVersionEntity();
            v.setTenantId(tenantId);
            v.setSubjectId(subjectId);
            v.setVersion(1L);
            v.setUpdatedAt(Instant.now());
            versionMapper.insert(v);
        }
    }
}