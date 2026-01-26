package com.ysmjjsy.goya.component.framework.masker.core;

import java.util.*;

/**
 * <p>基于字段名（key）的敏感类型分类器</p>
 *
 * <p>用于 Map 或 JSON 结构脱敏。</p>
 *
 * @author goya
 * @since 2026/1/24 22:56
 */
public class MaskingKeyClassifier {

    private final Set<String> extraKeysLower;

    /**
     * 构造分类器。
     *
     * @param extraKeys 额外敏感 key（可为空）
     */
    public MaskingKeyClassifier(List<String> extraKeys) {
        Set<String> set = new HashSet<>();
        if (extraKeys != null) {
            for (String k : extraKeys) {
                if (k != null && !k.isBlank()) {
                    set.add(k.toLowerCase(Locale.ROOT));
                }
            }
        }
        this.extraKeysLower = Collections.unmodifiableSet(set);
    }

    /**
     * 根据 key 推断敏感类型。
     *
     * @param key 字段名
     * @return SensitiveType，不敏感则返回 null
     */
    public SensitiveType classify(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        String k = key.toLowerCase(Locale.ROOT);

        if (extraKeysLower.contains(k)) {
            return SensitiveType.GENERIC;
        }
        if (k.contains("password") || k.contains("pwd")) {
            return SensitiveType.PASSWORD;
        }
        if (k.contains("token") || k.contains("secret") || k.contains("apikey") || k.contains("api_key")) {
            return SensitiveType.TOKEN;
        }
        if (k.equals("authorization") || k.contains("cookie")) {
            return SensitiveType.HEADER_CREDENTIAL;
        }
        if (k.contains("email")) {
            return SensitiveType.EMAIL;
        }
        if (k.contains("phone") || k.contains("mobile")) {
            return SensitiveType.PHONE;
        }
        if (k.contains("idcard") || k.contains("id_card") || k.contains("cert")) {
            return SensitiveType.ID_CARD;
        }
        if (k.contains("bank") || k.contains("card")) {
            return SensitiveType.BANK_CARD;
        }
        return null;
    }
}
