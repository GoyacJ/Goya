package com.ysmjjsy.goya.component.admin.bootstrap;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ysmjjsy.goya.component.admin.configuration.properties.AdminProperties;
import com.ysmjjsy.goya.component.admin.constants.AdminConst;
import com.ysmjjsy.goya.component.admin.enums.RoleDataScopeEnum;
import com.ysmjjsy.goya.component.admin.enums.UserStatusEnum;
import com.ysmjjsy.goya.component.admin.policy.service.AdminPolicyDomainService;
import com.ysmjjsy.goya.component.admin.role.entity.IamRoleEntity;
import com.ysmjjsy.goya.component.admin.role.entity.IamUserRoleEntity;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRoleMapper;
import com.ysmjjsy.goya.component.admin.role.mapper.IamUserRoleMapper;
import com.ysmjjsy.goya.component.admin.support.AdminPasswordSupport;
import com.ysmjjsy.goya.component.admin.support.AdminTenantSupport;
import com.ysmjjsy.goya.component.admin.user.entity.IamUserEntity;
import com.ysmjjsy.goya.component.admin.user.mapper.IamUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * <p>初始化超级管理员</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Slf4j
public class AdminBootstrapRunner implements ApplicationRunner {

    private final ObjectProvider<AdminProperties> adminPropertiesProvider;
    private final ObjectProvider<AdminTenantSupport> adminTenantSupportProvider;
    private final ObjectProvider<AdminPasswordSupport> adminPasswordSupportProvider;
    private final ObjectProvider<IamRoleMapper> iamRoleMapperProvider;
    private final ObjectProvider<IamUserMapper> iamUserMapperProvider;
    private final ObjectProvider<IamUserRoleMapper> iamUserRoleMapperProvider;
    private final ObjectProvider<AdminPolicyDomainService> adminPolicyDomainServiceProvider;

    public AdminBootstrapRunner(ObjectProvider<AdminProperties> adminPropertiesProvider,
                                ObjectProvider<AdminTenantSupport> adminTenantSupportProvider,
                                ObjectProvider<AdminPasswordSupport> adminPasswordSupportProvider,
                                ObjectProvider<IamRoleMapper> iamRoleMapperProvider,
                                ObjectProvider<IamUserMapper> iamUserMapperProvider,
                                ObjectProvider<IamUserRoleMapper> iamUserRoleMapperProvider,
                                ObjectProvider<AdminPolicyDomainService> adminPolicyDomainServiceProvider) {
        this.adminPropertiesProvider = adminPropertiesProvider;
        this.adminTenantSupportProvider = adminTenantSupportProvider;
        this.adminPasswordSupportProvider = adminPasswordSupportProvider;
        this.iamRoleMapperProvider = iamRoleMapperProvider;
        this.iamUserMapperProvider = iamUserMapperProvider;
        this.iamUserRoleMapperProvider = iamUserRoleMapperProvider;
        this.adminPolicyDomainServiceProvider = adminPolicyDomainServiceProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        AdminProperties adminProperties = adminPropertiesProvider == null ? null : adminPropertiesProvider.getIfAvailable();
        if (adminProperties == null || !adminProperties.bootstrap().enabled()) {
            return;
        }

        String tenantId = resolveTenantId(adminProperties);
        String superRoleCode = StringUtils.defaultIfBlank(adminProperties.bootstrap().superRoleCode(), AdminConst.SUPER_ADMIN_ROLE_CODE);

        IamRoleMapper roleMapper = getBean(iamRoleMapperProvider);
        IamUserMapper userMapper = getBean(iamUserMapperProvider);
        IamUserRoleMapper userRoleMapper = getBean(iamUserRoleMapperProvider);
        AdminPasswordSupport passwordSupport = getBean(adminPasswordSupportProvider);

        if (roleMapper == null || userMapper == null || userRoleMapper == null || passwordSupport == null) {
            return;
        }

        IamRoleEntity role = roleMapper.selectOne(new LambdaQueryWrapper<IamRoleEntity>()
                .eq(IamRoleEntity::getTenantId, tenantId)
                .eq(IamRoleEntity::getRoleCode, superRoleCode));
        if (role == null) {
            role = new IamRoleEntity();
            role.setTenantId(tenantId);
            role.setRoleCode(superRoleCode);
            role.setRoleName("超级管理员");
            role.setDataScope(RoleDataScopeEnum.ALL.getCode());
            role.setStatus(UserStatusEnum.ENABLED.getCode());
            roleMapper.insert(role);
        }

        String adminUsername = StringUtils.defaultIfBlank(adminProperties.bootstrap().adminUsername(), "admin");
        IamUserEntity adminUser = userMapper.selectOne(new LambdaQueryWrapper<IamUserEntity>()
                .eq(IamUserEntity::getTenantId, tenantId)
                .eq(IamUserEntity::getUsername, adminUsername));
        if (adminUser == null) {
            String bootstrapPassword = StringUtils.defaultIfBlank(adminProperties.bootstrap().adminPassword(), System.getenv("GOYA_ADMIN_BOOTSTRAP_PASSWORD"));
            if (StringUtils.isBlank(bootstrapPassword)) {
                log.warn("[Goya] |- component [admin] bootstrap admin skipped because password is empty.");
                return;
            }
            adminUser = new IamUserEntity();
            adminUser.setTenantId(tenantId);
            adminUser.setUsername(adminUsername);
            adminUser.setNickname("超级管理员");
            adminUser.setPassword(passwordSupport.encode(bootstrapPassword));
            adminUser.setStatus(UserStatusEnum.ENABLED.getCode());
            userMapper.insert(adminUser);
        } else {
            userMapper.update(null, new LambdaUpdateWrapper<IamUserEntity>()
                    .eq(IamUserEntity::getId, adminUser.getId())
                    .set(IamUserEntity::getStatus, UserStatusEnum.ENABLED.getCode()));
        }

        Long relationCount = userRoleMapper.selectCount(new LambdaQueryWrapper<IamUserRoleEntity>()
                .eq(IamUserRoleEntity::getTenantId, tenantId)
                .eq(IamUserRoleEntity::getUserId, adminUser.getId())
                .eq(IamUserRoleEntity::getRoleId, role.getId()));
        if (relationCount == null || relationCount <= 0) {
            IamUserRoleEntity userRoleEntity = new IamUserRoleEntity();
            userRoleEntity.setTenantId(tenantId);
            userRoleEntity.setUserId(adminUser.getId());
            userRoleEntity.setRoleId(role.getId());
            userRoleMapper.insert(userRoleEntity);
        }

        AdminPolicyDomainService policyDomainService = adminPolicyDomainServiceProvider == null
                ? null
                : adminPolicyDomainServiceProvider.getIfAvailable();
        if (policyDomainService != null) {
            policyDomainService.bootstrapSuperAdminPolicies(tenantId, role.getId(), role.getRoleCode());
        }

        log.info("[Goya] |- component [admin] bootstrap super admin done. tenantId={}, username={}", tenantId, adminUsername);
    }

    private String resolveTenantId(AdminProperties adminProperties) {
        AdminTenantSupport tenantSupport = adminTenantSupportProvider == null ? null : adminTenantSupportProvider.getIfAvailable();
        if (tenantSupport != null) {
            return tenantSupport.requiredTenantId(null);
        }
        return adminProperties.defaultTenantId();
    }

    private <T> T getBean(ObjectProvider<T> provider) {
        return provider == null ? null : provider.getIfAvailable();
    }
}
