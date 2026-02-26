package com.ysmjjsy.goya.component.admin.permission.controller;

import com.ysmjjsy.goya.component.admin.permission.dto.PermissionSaveRequest;
import com.ysmjjsy.goya.component.admin.permission.entity.IamPermissionEntity;
import com.ysmjjsy.goya.component.admin.permission.service.AdminPermissionDomainService;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>权限管理 API</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/permissions")
public class AdminPermissionController implements IController {

    private final AdminPermissionDomainService adminPermissionDomainService;

    @GetMapping
    public ApiRes<List<IamPermissionEntity>> list(@RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminPermissionDomainService.list(tenantId));
    }

    @GetMapping("/{id}")
    public ApiRes<IamPermissionEntity> detail(@PathVariable("id") String id,
                                              @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminPermissionDomainService.detail(tenantId, id));
    }

    @PostMapping
    public ApiRes<IamPermissionEntity> create(@RequestBody PermissionSaveRequest request) {
        return response(adminPermissionDomainService.create(request));
    }

    @PutMapping("/{id}")
    public ApiRes<IamPermissionEntity> update(@PathVariable("id") String id,
                                              @RequestBody PermissionSaveRequest request) {
        return response(adminPermissionDomainService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiRes<Boolean> delete(@PathVariable("id") String id,
                                  @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminPermissionDomainService.delete(tenantId, id));
    }
}
