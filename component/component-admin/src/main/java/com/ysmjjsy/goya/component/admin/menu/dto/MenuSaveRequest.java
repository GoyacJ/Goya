package com.ysmjjsy.goya.component.admin.menu.dto;

/**
 * <p>菜单创建/更新请求</p>
 *
 * @author goya
 * @since 2026/2/26
 */
public record MenuSaveRequest(
        String tenantId,
        String menuCode,
        String menuName,
        String menuType,
        String path,
        String component,
        String permissionCode,
        String icon,
        String parentId,
        Integer sort,
        String status,
        Boolean hidden
) {
}
