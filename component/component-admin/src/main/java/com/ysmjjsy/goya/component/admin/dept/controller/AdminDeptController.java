package com.ysmjjsy.goya.component.admin.dept.controller;

import com.ysmjjsy.goya.component.admin.dept.dto.DeptSaveRequest;
import com.ysmjjsy.goya.component.admin.dept.entity.IamDeptEntity;
import com.ysmjjsy.goya.component.admin.dept.service.AdminDeptDomainService;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>部门管理 API</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/depts")
public class AdminDeptController implements IController {

    private final AdminDeptDomainService adminDeptDomainService;

    @GetMapping("/tree")
    public ApiRes<List<IamDeptEntity>> tree(@RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminDeptDomainService.tree(tenantId));
    }

    @GetMapping("/{id}")
    public ApiRes<IamDeptEntity> detail(@PathVariable("id") String id,
                                        @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminDeptDomainService.detail(tenantId, id));
    }

    @PostMapping
    public ApiRes<IamDeptEntity> create(@RequestBody DeptSaveRequest request) {
        return response(adminDeptDomainService.create(request));
    }

    @PutMapping("/{id}")
    public ApiRes<IamDeptEntity> update(@PathVariable("id") String id,
                                        @RequestBody DeptSaveRequest request) {
        return response(adminDeptDomainService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiRes<Boolean> delete(@PathVariable("id") String id,
                                  @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminDeptDomainService.delete(tenantId, id));
    }
}
