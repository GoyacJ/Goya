package com.ysmjjsy.goya.component.mybatisplus.permission.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Sets;
import com.ysmjjsy.goya.component.framework.security.domain.*;
import com.ysmjjsy.goya.component.framework.security.spi.PolicyRepository;
import com.ysmjjsy.goya.component.mybatisplus.permission.converter.PolicyConverter;
import com.ysmjjsy.goya.component.mybatisplus.permission.entity.DataResourcePolicyEntity;
import com.ysmjjsy.goya.component.mybatisplus.permission.mapper.DataResourcePolicyMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>基于 MyBatis Plus 的策略仓储实现。</p>
 *
 * @author goya
 * @since 2026/1/31 11:20
 */
@RequiredArgsConstructor
public class MybatisPolicyRepository implements PolicyRepository {

    private final DataResourcePolicyMapper policyMapper;
    private final PolicyConverter policyConverter;

    /**
     * 查询生效的策略列表。
     *
     * @param query 查询条件
     * @return 策略列表
     */
    @Override
    public List<Policy> findEffectivePolicies(@NonNull PolicyQuery query) {

        Set<SubjectKey> subjectKeys = buildSubjectKeys(query.getSubject());
        if (subjectKeys.isEmpty()) {
            return Collections.emptyList();
        }

        Resource resource = query.getResource();
        Set<String> resourceCodes = buildResourceCodes(resource);
        if (resourceCodes.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<DataResourcePolicyEntity> queryWrapper = Wrappers.lambdaQuery(DataResourcePolicyEntity.class);

        if (StringUtils.hasText(query.getTenantCode())) {
            queryWrapper.eq(DataResourcePolicyEntity::getTenantCode, query.getTenantCode());
        }

        queryWrapper.in(DataResourcePolicyEntity::getResourceCode, resourceCodes);

        if (resource.getResourceType() != null) {
            queryWrapper.eq(DataResourcePolicyEntity::getResourceType, resource.getResourceType().getCode());
        }
        if (StringUtils.hasText(query.getAction().getCode())) {
            queryWrapper.eq(DataResourcePolicyEntity::getActionCode, query.getAction().getCode());
        }

        queryWrapper.and(group -> {
            boolean first = true;
            for (SubjectKey key : subjectKeys) {
                if (first) {
                    group.eq(DataResourcePolicyEntity::getSubjectType, key.typeCode).eq(DataResourcePolicyEntity::getSubjectId, key.subjectId);
                    first = false;
                } else {
                    group.or(sub -> sub.eq(DataResourcePolicyEntity::getSubjectType, key.typeCode).eq(DataResourcePolicyEntity::getSubjectId, key.subjectId));
                }
            }
        });

        LocalDateTime requestTime = query.getRequestTime();
        queryWrapper.and(group -> group.eq(DataResourcePolicyEntity::getNeverExpire, true)
                .or()
                .ge(DataResourcePolicyEntity::getExpireTime, requestTime)
                .or()
                .isNull(DataResourcePolicyEntity::getExpireTime));

        return policyMapper.selectList(queryWrapper).stream()
                .filter(Objects::nonNull)
                .map(policyConverter::toTarget)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 构建资源编码
     *
     * @param resource 资源信息
     * @return 资源编码
     */
    private Set<String> buildResourceCodes(Resource resource) {
        Set<String> codes = Sets.newHashSet();
        codes.add(resource.getResourceCode());
        if (CollectionUtils.isNotEmpty(resource.getParentCodes())) {
            codes.addAll(resource.getParentCodes());
        }
        if (StringUtils.hasText(resource.getParentCode())) {
            codes.add(resource.getParentCode());
        }
        return codes;
    }

    /**
     * 构建主体key列表
     *
     * @param subject 主体
     * @return key列表
     */
    private Set<SubjectKey> buildSubjectKeys(Subject subject) {
        Set<SubjectKey> keys = Sets.newHashSet();
        addSubjectKey(keys, subject.getSubjectType(), subject.getSubjectId());
        subject.getRoleIds().forEach(roleId -> addSubjectKey(keys, SubjectType.ROLE, roleId));
        subject.getTeamIds().forEach(teamId -> addSubjectKey(keys, SubjectType.TEAM, teamId));
        subject.getOrgIds().forEach(orgId -> addSubjectKey(keys, SubjectType.ORG, orgId));
        return keys;
    }

    private void addSubjectKey(Set<SubjectKey> keys, SubjectType type, String subjectId) {
        if (type == null || !StringUtils.hasText(subjectId)) {
            return;
        }
        keys.add(new SubjectKey(type.getCode(), subjectId));
    }

    private record SubjectKey(String typeCode, String subjectId) {
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof SubjectKey(String code, String id))) {
                return false;
            }
            return Objects.equals(typeCode(), code) && Objects.equals(subjectId(), id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(typeCode(), subjectId());
        }
    }
}
