package com.ysmjjsy.goya.security.authentication.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ysmjjsy.goya.security.core.domain.SecurityRegisteredClient;
import com.ysmjjsy.goya.security.core.repository.IRegisteredClientMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>注册客户端管理服务</p>
 * <p>提供客户端CRUD操作接口，用于管理界面</p>
 * <p>注意：此服务需要DatabaseRegisteredClientRepository实现，如果使用其他实现可能无法正常工作</p>
 *
 * @author goya
 * @since 2026/1/5
 */
@Slf4j
@RequiredArgsConstructor
public class SecurityRegisteredClientService {

    private final RegisteredClientRepository registeredClientRepository;
    private final IRegisteredClientMapper registeredClientMapper;

    /**
     * 保存或更新客户端
     *
     * @param registeredClient 注册客户端
     */
    public void save(RegisteredClient registeredClient) {
        registeredClientRepository.save(registeredClient);
        log.debug("[Goya] |- security [core] RegisteredClient saved: {}", registeredClient.getClientId());
    }

    /**
     * 根据ID查找客户端
     *
     * @param id 客户端ID
     * @return 注册客户端
     */
    public RegisteredClient findById(String id) {
        return registeredClientRepository.findById(id);
    }

    /**
     * 根据客户端ID查找客户端
     *
     * @param clientId 客户端ID
     * @return 注册客户端
     */
    public RegisteredClient findByClientId(String clientId) {
        return registeredClientRepository.findByClientId(clientId);
    }

    /**
     * 查询所有客户端
     * <p>注意：此方法需要DatabaseRegisteredClientRepository实现</p>
     *
     * @return 客户端列表
     */
    public List<RegisteredClient> findAll() {
        List<SecurityRegisteredClient> entities = registeredClientMapper.selectList(
                new LambdaQueryWrapper<SecurityRegisteredClient>()
        );
        // 通过RegisteredClientRepository转换（需要先保存到Repository才能查询）
        return entities.stream()
                .map(entity -> {
                    // 先通过ID查找，如果不存在则通过clientId查找
                    RegisteredClient client = registeredClientRepository.findById(entity.getId());
                    if (client == null) {
                        client = registeredClientRepository.findByClientId(entity.getClientId());
                    }
                    return client;
                })
                .filter(client -> client != null)
                .collect(Collectors.toList());
    }

    /**
     * 删除客户端
     *
     * @param id 客户端ID
     */
    public void deleteById(String id) {
        RegisteredClient client = registeredClientRepository.findById(id);
        if (client != null) {
            registeredClientMapper.deleteById(id);
            log.debug("[Goya] |- security [core] RegisteredClient deleted: {}", id);
        }
    }

    /**
     * 根据客户端ID删除客户端
     *
     * @param clientId 客户端ID
     */
    public void deleteByClientId(String clientId) {
        RegisteredClient client = registeredClientRepository.findByClientId(clientId);
        if (client != null) {
            deleteById(client.getId());
        }
    }
}

