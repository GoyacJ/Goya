package com.ysmjjsy.goya.component.security.oauth2.configuration.key;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.ysmjjsy.goya.component.security.oauth2.configuration.properties.SecurityOAuth2Properties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>基于 JDBC 的 JWK 持久化与轮换管理器。</p>
 *
 * @author goya
 * @since 2026/2/10
 */
public class JdbcOAuth2JwkManager {

    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_RETIRED = "RETIRED";

    private final JdbcTemplate jdbcTemplate;
    private final SecurityOAuth2Properties securityOAuth2Properties;
    private final Object monitor = new Object();

    private volatile boolean schemaReady;

    public JdbcOAuth2JwkManager(JdbcTemplate jdbcTemplate, SecurityOAuth2Properties securityOAuth2Properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.securityOAuth2Properties = securityOAuth2Properties;
    }

    public List<JWK> loadJwks() {
        synchronized (monitor) {
            ensureSchema();

            Instant now = Instant.now();
            rotateIfNeeded(now);
            KeyRow activeKey = ensureActiveKey(now);

            Map<String, JWK> jwkByKid = new LinkedHashMap<>();
            jwkByKid.put(activeKey.kid(), parseRsaKey(activeKey.privateJwk()));

            for (KeyRow retired : listRetiredKeys(now)) {
                if (jwkByKid.containsKey(retired.kid())) {
                    continue;
                }
                RSAKey publicKey = parseRsaKey(retired.publicJwk()).toPublicJWK();
                jwkByKid.put(retired.kid(), publicKey);
            }

            return new ArrayList<>(jwkByKid.values());
        }
    }

    private void ensureSchema() {
        if (schemaReady) {
            return;
        }
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS oauth2_jwk (
                    kid VARCHAR(128) PRIMARY KEY,
                    status VARCHAR(16) NOT NULL,
                    public_jwk TEXT NOT NULL,
                    private_jwk TEXT NULL,
                    valid_from TIMESTAMP NOT NULL,
                    valid_to TIMESTAMP NULL,
                    created_at TIMESTAMP NOT NULL,
                    updated_at TIMESTAMP NOT NULL
                )
                """);
        schemaReady = true;
    }

    private void rotateIfNeeded(Instant now) {
        KeyRow activeKey = findLatestActiveKey();
        if (activeKey == null) {
            return;
        }

        if (!shouldRotate(activeKey, now)) {
            return;
        }

        Instant retiredValidTo = now.plus(resolveOverlap());
        retireActiveKey(activeKey.kid(), retiredValidTo, now);
        insertActiveKey(generateRsa(), now);
    }

    private KeyRow ensureActiveKey(Instant now) {
        KeyRow activeKey = findLatestActiveKey();
        if (activeKey == null) {
            insertActiveKey(generateRsa(), now);
            return findLatestActiveKey();
        }

        if (StringUtils.isBlank(activeKey.privateJwk())) {
            retireActiveKey(activeKey.kid(), now.plus(resolveOverlap()), now);
            insertActiveKey(generateRsa(), now);
            return findLatestActiveKey();
        }

        return activeKey;
    }

    private boolean shouldRotate(KeyRow activeKey, Instant now) {
        Instant validFrom = activeKey.validFrom();
        if (validFrom == null) {
            return true;
        }
        Instant nextRotationAt = validFrom.plus(resolveRotationInterval());
        return !now.isBefore(nextRotationAt);
    }

    private Duration resolveRotationInterval() {
        SecurityOAuth2Properties.Keys keys = securityOAuth2Properties.keys();
        if (keys == null || keys.rotationInterval() == null || keys.rotationInterval().isNegative() || keys.rotationInterval().isZero()) {
            return Duration.ofDays(30);
        }
        return keys.rotationInterval();
    }

    private Duration resolveOverlap() {
        SecurityOAuth2Properties.Keys keys = securityOAuth2Properties.keys();
        if (keys == null || keys.overlap() == null || keys.overlap().isNegative() || keys.overlap().isZero()) {
            return Duration.ofDays(7);
        }
        return keys.overlap();
    }

    private KeyRow findLatestActiveKey() {
        List<KeyRow> rows = jdbcTemplate.query("""
                        SELECT kid, status, public_jwk, private_jwk, valid_from, valid_to
                        FROM oauth2_jwk
                        WHERE status = ?
                        ORDER BY valid_from DESC
                        """,
                (resultSet, rowNum) -> new KeyRow(
                        resultSet.getString("kid"),
                        resultSet.getString("status"),
                        resultSet.getString("public_jwk"),
                        resultSet.getString("private_jwk"),
                        toInstant(resultSet.getTimestamp("valid_from")),
                        toInstant(resultSet.getTimestamp("valid_to"))
                ),
                STATUS_ACTIVE
        );
        return rows.isEmpty() ? null : rows.get(0);
    }

    private List<KeyRow> listRetiredKeys(Instant now) {
        return jdbcTemplate.query("""
                        SELECT kid, status, public_jwk, private_jwk, valid_from, valid_to
                        FROM oauth2_jwk
                        WHERE status = ?
                          AND (valid_to IS NULL OR valid_to > ?)
                        ORDER BY valid_from DESC
                        """,
                (resultSet, rowNum) -> new KeyRow(
                        resultSet.getString("kid"),
                        resultSet.getString("status"),
                        resultSet.getString("public_jwk"),
                        resultSet.getString("private_jwk"),
                        toInstant(resultSet.getTimestamp("valid_from")),
                        toInstant(resultSet.getTimestamp("valid_to"))
                ),
                STATUS_RETIRED,
                Timestamp.from(now)
        );
    }

    private void retireActiveKey(String kid, Instant validTo, Instant now) {
        jdbcTemplate.update("""
                        UPDATE oauth2_jwk
                           SET status = ?,
                               valid_to = ?,
                               private_jwk = NULL,
                               updated_at = ?
                         WHERE kid = ?
                           AND status = ?
                        """,
                STATUS_RETIRED,
                Timestamp.from(validTo),
                Timestamp.from(now),
                kid,
                STATUS_ACTIVE
        );
    }

    private void insertActiveKey(RSAKey rsaKey, Instant now) {
        jdbcTemplate.update("""
                        INSERT INTO oauth2_jwk
                            (kid, status, public_jwk, private_jwk, valid_from, valid_to, created_at, updated_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                rsaKey.getKeyID(),
                STATUS_ACTIVE,
                rsaKey.toPublicJWK().toJSONString(),
                rsaKey.toJSONString(),
                Timestamp.from(now),
                null,
                Timestamp.from(now),
                Timestamp.from(now)
        );
    }

    private RSAKey parseRsaKey(String jwkJson) {
        if (StringUtils.isBlank(jwkJson)) {
            throw new IllegalStateException("JWK 内容为空");
        }
        try {
            return RSAKey.parse(jwkJson);
        } catch (Exception ex) {
            throw new IllegalStateException("JWK 解析失败", ex);
        }
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    private RSAKey generateRsa() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }

        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        return new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
    }

    private record KeyRow(String kid,
                          String status,
                          String publicJwk,
                          String privateJwk,
                          Instant validFrom,
                          Instant validTo) {
    }
}

