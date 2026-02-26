package com.ysmjjsy.goya.component.admin.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>用户设备实体</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_user_device")
public class IamUserDeviceEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -1195735492587132905L;

    @TableField("user_id")
    private String userId;

    @TableField("device_id")
    private String deviceId;

    @TableField("device_name")
    private String deviceName;

    @TableField("device_type")
    private String deviceType;

    @TableField("trusted")
    private Boolean trusted;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;
}
