package com.ysmjjsy.goya.component.admin.menu.controller;

import com.ysmjjsy.goya.component.admin.menu.dto.MenuSaveRequest;
import com.ysmjjsy.goya.component.admin.menu.entity.IamMenuEntity;
import com.ysmjjsy.goya.component.admin.menu.service.AdminMenuDomainService;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>菜单管理 API</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/menus")
public class AdminMenuController implements IController {

    private final AdminMenuDomainService adminMenuDomainService;

    @GetMapping("/tree")
    public ApiRes<List<IamMenuEntity>> tree(@RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminMenuDomainService.tree(tenantId));
    }

    @GetMapping("/{id}")
    public ApiRes<IamMenuEntity> detail(@PathVariable("id") String id,
                                        @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminMenuDomainService.detail(tenantId, id));
    }

    @PostMapping
    public ApiRes<IamMenuEntity> create(@RequestBody MenuSaveRequest request) {
        return response(adminMenuDomainService.create(request));
    }

    @PutMapping("/{id}")
    public ApiRes<IamMenuEntity> update(@PathVariable("id") String id,
                                        @RequestBody MenuSaveRequest request) {
        return response(adminMenuDomainService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiRes<Boolean> delete(@PathVariable("id") String id,
                                  @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminMenuDomainService.delete(tenantId, id));
    }
}
