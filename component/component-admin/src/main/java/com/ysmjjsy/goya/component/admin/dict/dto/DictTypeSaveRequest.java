package com.ysmjjsy.goya.component.admin.dict.dto;

/**
 * <p>字典类型创建/更新请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record DictTypeSaveRequest(
        String tenantId,
        String dictTypeCode,
        String dictTypeName,
        String status,
        String remark
) {
}
