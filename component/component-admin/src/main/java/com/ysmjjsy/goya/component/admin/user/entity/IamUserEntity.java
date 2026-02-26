package com.ysmjjsy.goya.component.admin.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>用户实体</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_user")
public class IamUserEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 6131710404668221930L;

    @TableField("username")
    private String username;

    @TableField("password")
    private String password;

    @TableField("nickname")
    private String nickname;

    @TableField("phone_number")
    private String phoneNumber;

    @TableField("email")
    private String email;

    @TableField("avatar")
    private String avatar;

    @TableField("open_id")
    private String openId;

    @TableField("status")
    private String status;

    @TableField("password_changed_at")
    private LocalDateTime passwordChangedAt;

    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;
}
