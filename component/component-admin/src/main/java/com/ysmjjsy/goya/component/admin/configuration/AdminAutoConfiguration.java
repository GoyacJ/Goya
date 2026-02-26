package com.ysmjjsy.goya.component.admin.configuration;

import com.ysmjjsy.goya.component.admin.bootstrap.AdminBootstrapRunner;
import com.ysmjjsy.goya.component.admin.configuration.properties.AdminProperties;
import com.ysmjjsy.goya.component.admin.error.AdminErrorCodeCatalog;
import com.ysmjjsy.goya.component.admin.policy.service.AdminPolicyDomainService;
import com.ysmjjsy.goya.component.admin.security.AdminRolePermissionService;
import com.ysmjjsy.goya.component.admin.security.AdminTenantService;
import com.ysmjjsy.goya.component.admin.security.AdminUserService;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRolePermissionMapper;
import com.ysmjjsy.goya.component.admin.role.mapper.IamRoleMapper;
import com.ysmjjsy.goya.component.admin.role.mapper.IamUserRoleMapper;
import com.ysmjjsy.goya.component.admin.support.AdminPasswordSupport;
import com.ysmjjsy.goya.component.admin.support.AdminTenantSupport;
import com.ysmjjsy.goya.component.admin.support.AdminTreeSupport;
import com.ysmjjsy.goya.component.admin.support.SessionRevokeSupport;
import com.ysmjjsy.goya.component.admin.permission.mapper.IamPermissionMapper;
import com.ysmjjsy.goya.component.admin.user.mapper.IamUserAuthAuditLogMapper;
import com.ysmjjsy.goya.component.admin.user.mapper.IamUserDeviceMapper;
import com.ysmjjsy.goya.component.admin.user.mapper.IamUserMapper;
import com.ysmjjsy.goya.component.admin.user.mapper.IamUserPasswordHistoryMapper;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCodeCatalog;
import com.ysmjjsy.goya.component.mybatisplus.tenant.TenantProfileStore;
import com.ysmjjsy.goya.component.security.core.service.IRolePermissionService;
import com.ysmjjsy.goya.component.security.core.service.ITenantService;
import com.ysmjjsy.goya.component.security.core.service.IUserService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.ysmjjsy.goya.component.security.core.service.SecuritySessionLifecycleService;

/**
 * <p>admin 自动配置</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(AdminProperties.class)
@MapperScan("com.ysmjjsy.goya.component.admin")
@ComponentScan("com.ysmjjsy.goya.component.admin")
@ConditionalOnProperty(prefix = "goya.admin", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AdminAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [admin] AdminAutoConfiguration auto configure.");
    }

    @Bean
    @ConditionalOnMissingBean(name = "adminErrorCodeCatalog")
    public ErrorCodeCatalog adminErrorCodeCatalog() {
        AdminErrorCodeCatalog adminErrorCodeCatalog = new AdminErrorCodeCatalog();
        log.trace("[Goya] |- component [admin] |- bean [adminErrorCodeCatalog] register.");
        return adminErrorCodeCatalog;
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminTenantSupport adminTenantSupport(AdminProperties adminProperties) {
        AdminTenantSupport adminTenantSupport = new AdminTenantSupport(adminProperties);
        log.trace("[Goya] |- component [admin] |- bean [adminTenantSupport] register.");
        return adminTenantSupport;
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminTreeSupport adminTreeSupport() {
        AdminTreeSupport adminTreeSupport = new AdminTreeSupport();
        log.trace("[Goya] |- component [admin] |- bean [adminTreeSupport] register.");
        return adminTreeSupport;
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminPasswordSupport adminPasswordSupport(ObjectProvider<PasswordEncoder> passwordEncoderProvider) {
        AdminPasswordSupport adminPasswordSupport = new AdminPasswordSupport(passwordEncoderProvider);
        log.trace("[Goya] |- component [admin] |- bean [adminPasswordSupport] register.");
        return adminPasswordSupport;
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionRevokeSupport sessionRevokeSupport(ObjectProvider<SecuritySessionLifecycleService> lifecycleServiceProvider,
                                                     AdminProperties adminProperties) {
        SessionRevokeSupport sessionRevokeSupport = new SessionRevokeSupport(lifecycleServiceProvider, adminProperties);
        log.trace("[Goya] |- component [admin] |- bean [sessionRevokeSupport] register.");
        return sessionRevokeSupport;
    }

    @Bean
    @ConditionalOnMissingBean(IUserService.class)
    public IUserService adminUserService(IamUserMapper iamUserMapper,
                                         IamUserDeviceMapper iamUserDeviceMapper,
                                         IamUserAuthAuditLogMapper iamUserAuthAuditLogMapper,
                                         IamUserPasswordHistoryMapper iamUserPasswordHistoryMapper,
                                         IamUserRoleMapper iamUserRoleMapper,
                                         IamRoleMapper iamRoleMapper,
                                         IRolePermissionService rolePermissionService,
                                         AdminTenantSupport adminTenantSupport) {
        IUserService userService = new AdminUserService(
                iamUserMapper,
                iamUserDeviceMapper,
                iamUserAuthAuditLogMapper,
                iamUserPasswordHistoryMapper,
                iamUserRoleMapper,
                iamRoleMapper,
                rolePermissionService,
                adminTenantSupport
        );
        log.trace("[Goya] |- component [admin] |- bean [adminUserService] register.");
        return userService;
    }

    @Bean
    @ConditionalOnMissingBean(IRolePermissionService.class)
    public IRolePermissionService adminRolePermissionService(IamUserRoleMapper iamUserRoleMapper,
                                                             IamRoleMapper iamRoleMapper,
                                                             IamRolePermissionMapper iamRolePermissionMapper,
                                                             IamPermissionMapper iamPermissionMapper,
                                                             AdminTenantSupport adminTenantSupport) {
        IRolePermissionService rolePermissionService = new AdminRolePermissionService(
                iamUserRoleMapper,
                iamRoleMapper,
                iamRolePermissionMapper,
                iamPermissionMapper,
                adminTenantSupport
        );
        log.trace("[Goya] |- component [admin] |- bean [adminRolePermissionService] register.");
        return rolePermissionService;
    }

    @Bean
    @ConditionalOnMissingBean(ITenantService.class)
    public ITenantService adminTenantService(AdminProperties adminProperties,
                                             AdminTenantSupport adminTenantSupport,
                                             TenantProfileStore tenantProfileStore) {
        ITenantService tenantService = new AdminTenantService(
                adminProperties,
                adminTenantSupport,
                tenantProfileStore
        );
        log.trace("[Goya] |- component [admin] |- bean [adminTenantService] register.");
        return tenantService;
    }

    @Bean
    @ConditionalOnMissingBean
    public AdminBootstrapRunner adminBootstrapRunner(ObjectProvider<AdminProperties> adminPropertiesProvider,
                                                     ObjectProvider<AdminTenantSupport> adminTenantSupportProvider,
                                                     ObjectProvider<AdminPasswordSupport> adminPasswordSupportProvider,
                                                     ObjectProvider<IamRoleMapper> iamRoleMapperProvider,
                                                     ObjectProvider<IamUserMapper> iamUserMapperProvider,
                                                     ObjectProvider<IamUserRoleMapper> iamUserRoleMapperProvider,
                                                     ObjectProvider<AdminPolicyDomainService> adminPolicyDomainServiceProvider) {
        AdminBootstrapRunner adminBootstrapRunner = new AdminBootstrapRunner(
                adminPropertiesProvider,
                adminTenantSupportProvider,
                adminPasswordSupportProvider,
                iamRoleMapperProvider,
                iamUserMapperProvider,
                iamUserRoleMapperProvider,
                adminPolicyDomainServiceProvider
        );
        log.trace("[Goya] |- component [admin] |- bean [adminBootstrapRunner] register.");
        return adminBootstrapRunner;
    }
}
