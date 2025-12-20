package com.ysmjjsy.goya.security.core.domain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import org.springframework.security.core.userdetails.User;

import java.io.IOException;
import java.util.Set;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/10 11:24
 */
public class SecurityUserDeserializer extends JsonDeserializer<SecurityUser> {

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
     * @throws IOException             if a exception during IO occurs
     * @throws JsonProcessingException if an error during JSON processing occurs
     */
    @Override
    public SecurityUser deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode root = mapper.readTree(jp);

        String userId = JsonUtils.findStringValue(root, "userId");
        String username = JsonUtils.findStringValue(root, "username");
        String password = readPassword(root);

        Set<SecurityGrantedAuthority> authorities =
                mapper.convertValue(root.get("authorities"), SECURITY_GRANTED_AUTHORITY_SET);

        Set<String> roles =
                mapper.convertValue(root.get("roles"), SECURITY_ROLE_SET);

        SecurityUser.Builder b = SecurityUser.builder()
                .userId(userId)
                .username(username)
                .password(password)
                .openId(JsonUtils.findStringValue(root, "openId"))
                .tenantId(JsonUtils.findStringValue(root, "tenantId"))
                .nickname(JsonUtils.findStringValue(root, "nickname"))
                .phoneNumber(JsonUtils.findStringValue(root, "phoneNumber"))
                .email(JsonUtils.findStringValue(root, "email"))
                .avatar(JsonUtils.findStringValue(root, "avatar"))
                .authorities(authorities)
                .roles(roles)
                .enabled(JsonUtils.findBooleanValue(root, "enabled"))
                .accountNonExpired(JsonUtils.findBooleanValue(root, "accountNonExpired"))
                .accountNonLocked(JsonUtils.findBooleanValue(root, "accountNonLocked"))
                .credentialsNonExpired(JsonUtils.findBooleanValue(root, "credentialsNonExpired"));

        SecurityUser user = b.build();

        if (passwordNodeIsMissing(root)) {
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
        String pwd = pwdNode.asText("");
        return (pwd == null ? "" : pwd);
    }

    private boolean passwordNodeIsMissing(JsonNode root) {
        JsonNode pwdNode = root.get("password");

        // 只有字段“完全不存在”时才触发 eraseCredentials()
        return pwdNode == null;
    }
}

