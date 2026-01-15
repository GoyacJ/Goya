package com.ysmjjsy.goya.component.security.core.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 11:25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityGrantedAuthority implements GrantedAuthority {

    @Serial
    private static final long serialVersionUID = 6241216137398557378L;

    private String authority;
}
