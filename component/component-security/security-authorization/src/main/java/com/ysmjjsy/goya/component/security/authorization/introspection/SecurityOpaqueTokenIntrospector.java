package com.ysmjjsy.goya.component.security.authorization.introspection;

import com.ysmjjsy.goya.component.security.core.constants.StandardClaimNamesConst;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;

import java.util.*;

/**
 * <p>Opaque Token 权限增强 Introspector</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class SecurityOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final OpaqueTokenIntrospector delegate;

    public SecurityOpaqueTokenIntrospector(OpaqueTokenIntrospector delegate) {
        this.delegate = delegate;
    }

    @Override
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        OAuth2AuthenticatedPrincipal principal = delegate.introspect(token);

        Map<String, Object> attributes = new LinkedHashMap<>(principal.getAttributes());
        Set<GrantedAuthority> authorities = new LinkedHashSet<>(principal.getAuthorities());

        readStringSet(attributes.get(StandardClaimNamesConst.AUTHORITIES))
                .forEach(authority -> authorities.add(new SimpleGrantedAuthority(authority)));

        readStringSet(attributes.get(StandardClaimNamesConst.ROLES))
                .stream()
                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                .forEach(roleAuthority -> authorities.add(new SimpleGrantedAuthority(roleAuthority)));

        return new DefaultOAuth2AuthenticatedPrincipal(principal.getName(), attributes, authorities);
    }

    private Set<String> readStringSet(@Nullable Object value) {
        if (value == null) {
            return Collections.emptySet();
        }

        if (value instanceof Collection<?> collection) {
            Set<String> values = new LinkedHashSet<>();
            for (Object item : collection) {
                if (item != null) {
                    values.add(String.valueOf(item));
                }
            }
            return values;
        }

        if (value instanceof String text) {
            if (StringUtils.isBlank(text)) {
                return Collections.emptySet();
            }
            Set<String> values = new LinkedHashSet<>();
            Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .forEach(values::add);
            return values;
        }

        return Set.of(String.valueOf(value));
    }
}
