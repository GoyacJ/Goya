package com.ysmjjsy.goya.security.authentication.service;

import com.ysmjjsy.goya.security.authentication.domain.SecurityRegisteredClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.time.Instant;

/**
 * <p>客户端密钥轮换服务</p>
 * <p>支持客户端密钥的安全轮换，包括过渡期（新旧密钥都可用）</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Slf4j
@RequiredArgsConstructor
public class ClientSecretRotationService {

    private final RegisteredClientRepository registeredClientRepository;
    private final IRegisteredClientMapper registeredClientMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 轮换客户端密钥
     * <p>将当前密钥保存为旧密钥，设置新密钥</p>
     * <p>在过渡期内，新旧密钥都可以使用</p>
     *
     * @param clientId 客户端ID
     * @param newSecret 新密钥
     * @param transitionPeriodDays 过渡期天数（在此期间新旧密钥都可用）
     */
    public void rotateSecret(String clientId, String newSecret, int transitionPeriodDays) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if (client == null) {
            throw new IllegalArgumentException("Client not found: " + clientId);
        }

        // 查找实体
        SecurityRegisteredClient entity = registeredClientMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecurityRegisteredClient>()
                        .eq(SecurityRegisteredClient::getClientId, clientId)
        );

        if (entity == null) {
            throw new IllegalArgumentException("Client entity not found: " + clientId);
        }

        // 将当前密钥保存为旧密钥
        if (entity.getClientSecret() != null) {
            entity.setPreviousClientSecret(entity.getClientSecret());
        }

        // 设置新密钥
        entity.setClientSecret(newSecret);
        entity.setSecretRotationTime(Instant.now());

        // 更新实体
        registeredClientMapper.updateById(entity);

        log.info("[Goya] |- security [core] Client secret rotated for client: {} | transition period: {} days", 
                clientId, transitionPeriodDays);
    }

    /**
     * 验证客户端密钥（支持新旧密钥）
     *
     * @param clientId 客户端ID
     * @param providedSecret 提供的密钥
     * @return true如果密钥有效（新密钥或旧密钥），false如果无效
     */
    public boolean validateSecret(String clientId, String providedSecret) {
        SecurityRegisteredClient entity = registeredClientMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecurityRegisteredClient>()
                        .eq(SecurityRegisteredClient::getClientId, clientId)
        );

        if (entity == null) {
            return false;
        }

        // 验证新密钥
        if (entity.getClientSecret() != null) {
            if (passwordEncoder.matches(providedSecret, entity.getClientSecret())) {
                return true;
            }
        }

        // 验证旧密钥（如果存在且在过渡期内）
        if (entity.getPreviousClientSecret() != null && entity.getSecretRotationTime() != null) {
            // 检查是否在过渡期内（例如30天）
            Instant transitionEnd = entity.getSecretRotationTime().plusSeconds(30 * 24 * 60 * 60);
            if (Instant.now().isBefore(transitionEnd)) {
                if (passwordEncoder.matches(providedSecret, entity.getPreviousClientSecret())) {
                    log.debug("[Goya] |- security [core] Client authenticated with previous secret (transition period): {}", clientId);
                    return true;
                }
            } else {
                // 过渡期已过，清除旧密钥
                entity.setPreviousClientSecret(null);
                registeredClientMapper.updateById(entity);
                log.debug("[Goya] |- security [core] Transition period expired, previous secret cleared for client: {}", clientId);
            }
        }

        return false;
    }

    /**
     * 完成密钥轮换（清除旧密钥）
     * <p>在过渡期结束后调用，清除旧密钥</p>
     *
     * @param clientId 客户端ID
     */
    public void completeRotation(String clientId) {
        SecurityRegisteredClient entity = registeredClientMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SecurityRegisteredClient>()
                        .eq(SecurityRegisteredClient::getClientId, clientId)
        );

        if (entity != null && entity.getPreviousClientSecret() != null) {
            entity.setPreviousClientSecret(null);
            registeredClientMapper.updateById(entity);
            log.info("[Goya] |- security [core] Secret rotation completed for client: {}", clientId);
        }
    }
}

