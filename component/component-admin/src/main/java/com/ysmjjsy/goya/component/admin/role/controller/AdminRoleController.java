package com.ysmjjsy.goya.component.admin.role.controller;

import com.ysmjjsy.goya.component.admin.role.dto.RoleDataScopeRequest;
import com.ysmjjsy.goya.component.admin.role.dto.RoleMenuAssignRequest;
import com.ysmjjsy.goya.component.admin.role.dto.RolePermissionAssignRequest;
import com.ysmjjsy.goya.component.admin.role.dto.RoleSaveRequest;
import com.ysmjjsy.goya.component.admin.role.entity.IamRoleEntity;
import com.ysmjjsy.goya.component.admin.role.service.AdminRoleDomainService;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>角色管理 API</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/roles")
public class AdminRoleController implements IController {

    private final AdminRoleDomainService adminRoleDomainService;

    @GetMapping
    public ApiRes<List<IamRoleEntity>> list(@RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminRoleDomainService.list(tenantId));
    }

    @GetMapping("/{id}")
    public ApiRes<IamRoleEntity> detail(@PathVariable("id") String id,
                                        @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminRoleDomainService.detail(tenantId, id));
    }

    @PostMapping
    public ApiRes<IamRoleEntity> create(@RequestBody RoleSaveRequest request) {
        return response(adminRoleDomainService.create(request));
    }

    @PutMapping("/{id}")
    public ApiRes<IamRoleEntity> update(@PathVariable("id") String id,
                                        @RequestBody RoleSaveRequest request) {
        return response(adminRoleDomainService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiRes<Boolean> delete(@PathVariable("id") String id,
                                  @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminRoleDomainService.delete(tenantId, id));
    }

    @PutMapping("/{id}/permissions")
    public ApiRes<Boolean> assignPermissions(@PathVariable("id") String id,
                                             @RequestBody RolePermissionAssignRequest request) {
        return response(adminRoleDomainService.assignPermissions(id, request));
    }

    @PutMapping("/{id}/menus")
    public ApiRes<Boolean> assignMenus(@PathVariable("id") String id,
                                       @RequestBody RoleMenuAssignRequest request) {
        return response(adminRoleDomainService.assignMenus(id, request));
    }

    @PutMapping("/{id}/data-scope")
    public ApiRes<Boolean> updateDataScope(@PathVariable("id") String id,
                                           @RequestBody RoleDataScopeRequest request) {
        return response(adminRoleDomainService.updateDataScope(id, request));
    }
}
