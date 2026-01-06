package com.ysmjjsy.goya.component.auth.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.security.Principal;
import java.util.Set;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/31 11:47
 */
@Data
public class UserPrincipal implements Principal, Serializable {

    @Serial
    private static final long serialVersionUID = -8687474410061066362L;

    private String userId;
    private String username;
    private String openId;
    private String password;
    private  String tenantId;
    private  String nickname;
    private  String phoneNumber;
    private  String email;
    private  String avatar;

    private Set<String> roles;

    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    @Override
    public String getName() {
        return username;
    }
}

