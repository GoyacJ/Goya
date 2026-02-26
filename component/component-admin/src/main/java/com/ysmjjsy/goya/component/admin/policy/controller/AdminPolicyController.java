package com.ysmjjsy.goya.component.admin.policy.controller;

import com.ysmjjsy.goya.component.admin.policy.dto.PolicySaveRequest;
import com.ysmjjsy.goya.component.admin.policy.dto.ResourceSaveRequest;
import com.ysmjjsy.goya.component.admin.policy.service.AdminPolicyDomainService;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import com.ysmjjsy.goya.component.mybatisplus.permission.entity.DataResourceEntity;
import com.ysmjjsy.goya.component.mybatisplus.permission.entity.DataResourcePolicyEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>策略与资源管理 API</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminPolicyController implements IController {

    private final AdminPolicyDomainService adminPolicyDomainService;

    @GetMapping("/policies")
    public ApiRes<List<DataResourcePolicyEntity>> listPolicies(@RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminPolicyDomainService.listPolicies(tenantId));
    }

    @PostMapping("/policies")
    public ApiRes<DataResourcePolicyEntity> createPolicy(@RequestBody PolicySaveRequest request) {
        return response(adminPolicyDomainService.createPolicy(request));
    }

    @PutMapping("/policies/{id}")
    public ApiRes<DataResourcePolicyEntity> updatePolicy(@PathVariable("id") String id,
                                                          @RequestBody PolicySaveRequest request) {
        return response(adminPolicyDomainService.updatePolicy(id, request));
    }

    @DeleteMapping("/policies/{id}")
    public ApiRes<Boolean> deletePolicy(@PathVariable("id") String id,
                                        @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminPolicyDomainService.deletePolicy(tenantId, id));
    }

    @GetMapping("/resources")
    public ApiRes<List<DataResourceEntity>> listResources(@RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminPolicyDomainService.listResources(tenantId));
    }

    @PostMapping("/resources")
    public ApiRes<DataResourceEntity> createResource(@RequestBody ResourceSaveRequest request) {
        return response(adminPolicyDomainService.createResource(request));
    }

    @PutMapping("/resources/{id}")
    public ApiRes<DataResourceEntity> updateResource(@PathVariable("id") String id,
                                                      @RequestBody ResourceSaveRequest request) {
        return response(adminPolicyDomainService.updateResource(id, request));
    }

    @DeleteMapping("/resources/{id}")
    public ApiRes<Boolean> deleteResource(@PathVariable("id") String id,
                                          @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminPolicyDomainService.deleteResource(tenantId, id));
    }
}
