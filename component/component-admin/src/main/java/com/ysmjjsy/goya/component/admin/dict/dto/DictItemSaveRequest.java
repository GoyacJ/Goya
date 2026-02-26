package com.ysmjjsy.goya.component.admin.dict.dto;

/**
 * <p>字典条目创建/更新请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record DictItemSaveRequest(
        String tenantId,
        String dictTypeCode,
        String itemCode,
        String itemLabel,
        String itemValue,
        Integer sort,
        String status,
        String remark
) {
}
