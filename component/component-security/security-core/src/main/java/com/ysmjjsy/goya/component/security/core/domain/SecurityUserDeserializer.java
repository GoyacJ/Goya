package com.ysmjjsy.goya.component.security.core.domain;

import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import java.util.Set;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 11:24
 */
public class SecurityUserDeserializer extends ValueDeserializer<SecurityUser> {

    private static final TypeReference<Set<SecurityGrantedAuthority>> SECURITY_GRANTED_AUTHORITY_SET = new TypeReference<>() {
    };
    private static final TypeReference<Set<String>> SECURITY_ROLE_SET = new TypeReference<>() {
    };

    /**
     * This method will create {@link User} object. It will ensure successful object
     * creation even if password key is null in serialized json, because credentials may
     * be removed from the {@link User} by invoking {@link User#eraseCredentials()}. In
     * that case there won't be any password key in serialized json.
     *
     * @param jp   the JsonParser
     * @param ctxt the DeserializationContext
     * @return the user
     * @throws JacksonException if an error during JSON processing occurs
     */
    @Override
    public SecurityUser deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
        JsonNode jsonNode = ctxt.readTree(jp);
        String userId = JsonUtils.findStringValue(jsonNode, "userId");
        String username = JsonUtils.findStringValue(jsonNode, "username");
        String password = readPassword(jsonNode);

        Set<GrantedAuthority> authorities = ctxt.readTreeAsValue(JsonUtils.readJsonNode(jsonNode, "authorities"),
                ctxt.getTypeFactory().constructType(SECURITY_GRANTED_AUTHORITY_SET));

        Set<String> roles = ctxt.readTreeAsValue(JsonUtils.readJsonNode(jsonNode, "authorities"),
                ctxt.getTypeFactory().constructType(SECURITY_ROLE_SET));

        SecurityUser.Builder b = SecurityUser.builder()
                .userId(userId)
                .username(username)
                .password(password)
                .openId(JsonUtils.findStringValue(jsonNode, "openId"))
                .tenantId(JsonUtils.findStringValue(jsonNode, "tenantId"))
                .nickname(JsonUtils.findStringValue(jsonNode, "nickname"))
                .phoneNumber(JsonUtils.findStringValue(jsonNode, "phoneNumber"))
                .email(JsonUtils.findStringValue(jsonNode, "email"))
                .avatar(JsonUtils.findStringValue(jsonNode, "avatar"))
                .authorities(authorities)
                .roles(roles)
                .enabled(JsonUtils.findBooleanValue(jsonNode, "enabled"))
                .accountNonExpired(JsonUtils.findBooleanValue(jsonNode, "accountNonExpired"))
                .accountNonLocked(JsonUtils.findBooleanValue(jsonNode, "accountNonLocked"))
                .credentialsNonExpired(JsonUtils.findBooleanValue(jsonNode, "credentialsNonExpired"));

        SecurityUser user = b.build();

        if (passwordNodeIsMissing(jsonNode)) {
            user.eraseCredentials();
        }

        return user;
    }

    private String readPassword(JsonNode root) {
        JsonNode pwdNode = root.get("password");

        // 字段不存在 → 返回空字符串
        if (pwdNode == null || pwdNode.isNull()) {
            return "";
        }

        // 字段存在但 null → Jackson 会解析为 null → 返回 ""
        String pwd = pwdNode.asString("");
        return (pwd == null ? "" : pwd);
    }

    private boolean passwordNodeIsMissing(JsonNode root) {
        JsonNode pwdNode = root.get("password");

        // 只有字段“完全不存在”时才触发 eraseCredentials()
        return pwdNode == null;
    }
}

