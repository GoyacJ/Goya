package com.ysmjjsy.goya.security.core.domain;

import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import tools.jackson.databind.annotation.JsonDeserialize;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 11:22
 */
@Getter
@ToString
@JsonDeserialize(using = SecurityUserDeserializer.class)
public class SecurityUser implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 4719371763229244364L;

    private final String userId;
    private final String username;
    private final String openId;
    private String password;
    private final String tenantId;
    private final String nickname;
    private final String phoneNumber;
    private final String email;
    private final String avatar;

    private final Set<GrantedAuthority> authorities;
    private final Set<String> roles;

    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;

    public static Builder builder() {
        return new Builder();
    }

    private SecurityUser(Builder b) {
        Assert.isTrue(b.username != null && !b.username.isEmpty(), "username cannot be empty");
        Assert.notNull(b.password, "password cannot be null");

        this.userId = b.userId;
        this.username = b.username;
        this.password = b.password;
        this.openId = b.openId;
        this.tenantId = b.tenantId;
        this.nickname = b.nickname;
        this.phoneNumber = b.phoneNumber;
        this.email = b.email;
        this.avatar = b.avatar;

        this.enabled = b.enabled;
        this.accountNonExpired = b.accountNonExpired;
        this.accountNonLocked = b.accountNonLocked;
        this.credentialsNonExpired = b.credentialsNonExpired;

        this.authorities = Collections.unmodifiableSet(sortAuthorities(b.authorities));
        this.roles = (b.roles != null ? b.roles : new HashSet<>());
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    private static SortedSet<GrantedAuthority> sortAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Assert.notNull(authorities, "Cannot pass a null GrantedAuthority collection");

        SortedSet<GrantedAuthority> sorted = new TreeSet<>(new AuthorityComparator());
        for (GrantedAuthority authority : authorities) {
            Assert.notNull(authority, "GrantedAuthority list cannot contain null elements");
            sorted.add(authority);
        }
        return sorted;
    }

    private static class AuthorityComparator implements Comparator<GrantedAuthority>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Override
        public int compare(GrantedAuthority g1, GrantedAuthority g2) {
            if (g2.getAuthority() == null) {
                return -1;
            }
            if (g1.getAuthority() == null) {
                return 1;
            }
            return g1.getAuthority().compareTo(g2.getAuthority());
        }
    }

    // ======= Builder =======
    public static class Builder {

        private String userId;
        private String username;
        private String openId;
        private String password;
        private String tenantId;
        private String nickname;
        private String phoneNumber;
        private String email;
        private String avatar;

        private final Set<GrantedAuthority> authorities = new HashSet<>();
        private Set<String> roles = new HashSet<>();

        private boolean accountNonExpired = true;
        private boolean accountNonLocked = true;
        private boolean credentialsNonExpired = true;
        private boolean enabled = true;

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder openId(String openId) {
            this.openId = openId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public Builder phoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder authorities(Collection<? extends GrantedAuthority> authorities) {
            if (authorities != null) {
                this.authorities.addAll(authorities);
            }
            return this;
        }

        public Builder addAuthority(GrantedAuthority authority) {
            this.authorities.add(authority);
            return this;
        }

        public Builder roles(Set<String> roles) {
            if (roles != null) {
                this.roles = roles;
            }
            return this;
        }

        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder accountNonExpired(boolean v) {
            this.accountNonExpired = v;
            return this;
        }

        public Builder accountNonLocked(boolean v) {
            this.accountNonLocked = v;
            return this;
        }

        public Builder credentialsNonExpired(boolean v) {
            this.credentialsNonExpired = v;
            return this;
        }

        public SecurityUser build() {
            return new SecurityUser(this);
        }
    }
}
