package com.ysmjjsy.goya.component.admin.dict.controller;

import com.ysmjjsy.goya.component.admin.dict.dto.DictItemSaveRequest;
import com.ysmjjsy.goya.component.admin.dict.dto.DictTypeSaveRequest;
import com.ysmjjsy.goya.component.admin.dict.entity.IamDictItemEntity;
import com.ysmjjsy.goya.component.admin.dict.entity.IamDictTypeEntity;
import com.ysmjjsy.goya.component.admin.dict.service.AdminDictDomainService;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.servlet.definition.IController;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>字典管理 API</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dict")
public class AdminDictController implements IController {

    private final AdminDictDomainService adminDictDomainService;

    @GetMapping("/types")
    public ApiRes<List<IamDictTypeEntity>> listTypes(@RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminDictDomainService.listTypes(tenantId));
    }

    @PostMapping("/types")
    public ApiRes<IamDictTypeEntity> createType(@RequestBody DictTypeSaveRequest request) {
        return response(adminDictDomainService.createType(request));
    }

    @PutMapping("/types/{id}")
    public ApiRes<IamDictTypeEntity> updateType(@PathVariable("id") String id,
                                                @RequestBody DictTypeSaveRequest request) {
        return response(adminDictDomainService.updateType(id, request));
    }

    @DeleteMapping("/types/{id}")
    public ApiRes<Boolean> deleteType(@PathVariable("id") String id,
                                      @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminDictDomainService.deleteType(tenantId, id));
    }

    @GetMapping("/types/{typeCode}/items")
    public ApiRes<List<IamDictItemEntity>> listItems(@PathVariable("typeCode") String typeCode,
                                                      @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminDictDomainService.listItems(tenantId, typeCode));
    }

    @PostMapping("/types/{typeCode}/items")
    public ApiRes<IamDictItemEntity> createItem(@PathVariable("typeCode") String typeCode,
                                                 @RequestBody DictItemSaveRequest request) {
        DictItemSaveRequest normalized = new DictItemSaveRequest(
                request.tenantId(),
                typeCode,
                request.itemCode(),
                request.itemLabel(),
                request.itemValue(),
                request.sort(),
                request.status(),
                request.remark()
        );
        return response(adminDictDomainService.createItem(normalized));
    }

    @PutMapping("/items/{id}")
    public ApiRes<IamDictItemEntity> updateItem(@PathVariable("id") String id,
                                                 @RequestBody DictItemSaveRequest request) {
        return response(adminDictDomainService.updateItem(id, request));
    }

    @DeleteMapping("/items/{id}")
    public ApiRes<Boolean> deleteItem(@PathVariable("id") String id,
                                      @RequestParam(value = "tenantId", required = false) String tenantId) {
        return response(adminDictDomainService.deleteItem(tenantId, id));
    }
}
