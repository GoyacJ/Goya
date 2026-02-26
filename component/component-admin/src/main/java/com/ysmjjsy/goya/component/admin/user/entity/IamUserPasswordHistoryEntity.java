package com.ysmjjsy.goya.component.admin.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ysmjjsy.goya.component.mybatisplus.definition.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * <p>用户密码历史</p>
 *
 * @author goya
 * @since 2026/2/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("iam_user_password_history")
public class IamUserPasswordHistoryEntity extends BaseEntity {

    @Serial
    private static final long serialVersionUID = -4704884012349948574L;

    @TableField("user_id")
    private String userId;

    @TableField("password")
    private String password;
}
