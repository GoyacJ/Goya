package com.ysmjjsy.goya.component.security.core.domain;

import com.ysmjjsy.goya.component.framework.core.web.UserAgent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * <p>用户设备实体</p>
 * <p>用于设备注册、信任设备、设备撤销等功能</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户设备实体")
public class SecurityUserDevice {

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "设备ID（设备指纹）")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型（MOBILE, TABLET, DESKTOP, UNKNOWN）")
    private String deviceType;

    @Schema(description = "是否信任设备")
    private Boolean trusted;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "IP地址")
    private String ipAddress;

    @Schema(description = "User-Agent")
    private UserAgent userAgent;
}

