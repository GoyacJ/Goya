package com.ysmjjsy.goya.component.admin.user.controller;

import com.ysmjjsy.goya.component.admin.user.dto.*;
import com.ysmjjsy.goya.component.admin.user.entity.IamUserDeviceEntity;
import com.ysmjjsy.goya.component.admin.user.entity.IamUserEntity;
import com.ysmjjsy.goya.component.admin.user.service.AdminUserDomainService;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>用户管理 API</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController implements IController {

    private final AdminUserDomainService adminUserDomainService;

    @GetMapping
    public ApiRes<List<IamUserEntity>> list(@RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminUserDomainService.list(tenantId));
    }

    @GetMapping("/{id}")
    public ApiRes<IamUserEntity> detail(@PathVariable("id") String id,
                                        @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminUserDomainService.detail(tenantId, id));
    }

    @PostMapping
    public ApiRes<IamUserEntity> create(@RequestBody UserSaveRequest request) {
        return response(adminUserDomainService.create(request));
    }

    @PutMapping("/{id}")
    public ApiRes<IamUserEntity> update(@PathVariable("id") String id,
                                        @RequestBody UserSaveRequest request) {
        return response(adminUserDomainService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiRes<Boolean> delete(@PathVariable("id") String id,
                                  @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminUserDomainService.delete(tenantId, id));
    }

    @PutMapping("/{id}/status")
    public ApiRes<Boolean> updateStatus(@PathVariable("id") String id,
                                        @RequestBody UserStatusRequest request) {
        return response(adminUserDomainService.updateStatus(id, request));
    }

    @PutMapping("/{id}/password")
    public ApiRes<Boolean> updatePassword(@PathVariable("id") String id,
                                          @RequestBody UserPasswordRequest request) {
        return response(adminUserDomainService.updatePassword(id, request));
    }

    @PutMapping("/{id}/roles")
    public ApiRes<Boolean> assignRoles(@PathVariable("id") String id,
                                       @RequestBody UserRoleAssignRequest request) {
        return response(adminUserDomainService.assignRoles(id, request));
    }

    @PutMapping("/{id}/depts")
    public ApiRes<Boolean> assignDepts(@PathVariable("id") String id,
                                       @RequestBody UserDeptAssignRequest request) {
        return response(adminUserDomainService.assignDepts(id, request));
    }

    @GetMapping("/{id}/devices")
    public ApiRes<List<IamUserDeviceEntity>> devices(@PathVariable("id") String id,
                                                      @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminUserDomainService.devices(tenantId, id));
    }

    @DeleteMapping("/{id}/devices/{deviceId}")
    public ApiRes<Boolean> removeDevice(@PathVariable("id") String id,
                                        @PathVariable("deviceId") String deviceId,
                                        @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminUserDomainService.removeDevice(tenantId, id, deviceId));
    }
}
