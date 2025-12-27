package com.ysmjjsy.goya.component.bus.service;

import com.ysmjjsy.goya.component.bus.capabilities.Capabilities;
import com.ysmjjsy.goya.component.bus.configuration.properties.BusProperties;
import com.ysmjjsy.goya.component.bus.definition.EventScope;
import com.ysmjjsy.goya.component.bus.definition.IEvent;
import com.ysmjjsy.goya.component.bus.metrics.EventMetrics;
import com.ysmjjsy.goya.component.bus.publish.IRemoteEventPublisher;
import com.ysmjjsy.goya.component.bus.publish.MetadataAccessor;
import com.ysmjjsy.goya.component.bus.transaction.EventTransactionSynchronization;
import com.ysmjjsy.goya.component.common.context.SpringContext;
import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
import com.ysmjjsy.goya.component.common.strategy.IStrategyExecute;
import com.ysmjjsy.goya.component.common.strategy.StrategyChoose;
import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * <p>事件总线服务实现</p>
 * <p>通过 StrategyChoose 选择发布策略（LOCAL 或 DISTRIBUTED）</p>
 *
 * @author goya
 * @since 2025/12/21
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultBusService implements IBusService {

    private final StrategyChoose strategyChoose;
    private final BusProperties busProperties;

    @Override
    public <E extends IEvent> void publishLocal(E event) {
        if (event == null) {
            throw new CommonException("Event cannot be null");
        }

        log.debug("[Goya] |- component [bus] BusServiceImpl |- publish local event [{}]", event.eventName());
        long startTime = System.currentTimeMillis();
        try {
            IStrategyExecute publisher = strategyChoose.choose("LOCAL");
            publisher.execute(event);
            EventMetrics.recordPublish(event.eventName(), EventScope.LOCAL);
        } catch (Exception e) {
            EventMetrics.recordFailure(event.eventName(), e.getMessage());
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            EventMetrics.recordSuccess(event.eventName(), duration);
        }
    }

    @Override
    public <E extends IEvent> void publishRemote(E event) {
        if (event == null) {
            throw new CommonException("Event cannot be null");
        }

        long startTime = System.currentTimeMillis();
        try {
            log.debug("[Goya] |- component [bus] BusServiceImpl |- publish remote event [{}]", event.eventName());
            IRemoteEventPublisher publisher = chooseRemotePublisher(busProperties.defaultRemoteBus());
            
            if (publisher == null) {
                log.warn("[Goya] |- component [bus] BusServiceImpl |- remote event publishing is not available. " +
                        "Please introduce a corresponding starter (e.g., kafka-boot-starter). Event [{}] will not be published remotely.",
                        event.eventName());
                EventMetrics.recordFailure(event.eventName(), "Publisher not available");
                return;
            }
            
            // 构建 Message Headers
            MessageHeaders headers = buildMessageHeaders(event, null, null);
            
            // 创建 Message
            Message<E> message = MessageBuilder.withPayload(event)
                    .copyHeaders(headers)
                    .build();
            
            // 构建 destination
            String destination = buildDestination(event);
            publisher.publish(destination, message);
            EventMetrics.recordPublish(event.eventName(), EventScope.REMOTE);
        } catch (Exception e) {
            log.warn("[Goya] |- component [bus] BusServiceImpl |- remote event publishing is not available. " +
                    "Please introduce a corresponding starter (e.g., kafka-boot-starter). Event [{}] will not be published remotely. Error: {}",
                    event.eventName(), e.getMessage());
            EventMetrics.recordFailure(event.eventName(), e.getMessage());
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            EventMetrics.recordSuccess(event.eventName(), duration);
        }
    }

    @Override
    public <E extends IEvent> void publishDelayed(E event, Duration delay) {
        if (event == null) {
            throw new CommonException("Event cannot be null");
        }
        if (delay == null || delay.isNegative()) {
            throw new CommonException("Delay must be non-negative");
        }

        log.debug("[Goya] |- component [bus] BusServiceImpl |- publish delayed event [{}] with delay [{}ms]",
                event.eventName(), delay.toMillis());

        try {
            IRemoteEventPublisher publisher = chooseRemotePublisher("DISTRIBUTED");
            
            if (publisher == null) {
                log.warn("[Goya] |- component [bus] BusServiceImpl |- remote event publishing is not available. " +
                        "Please introduce a corresponding starter (e.g., kafka-boot-starter). Delayed event [{}] will not be published remotely.",
                        event.eventName());
                return;
            }

            // 检查能力并处理降级
            Capabilities capabilities = publisher.getCapabilities();
            long delayMillis = delay.toMillis();

            if (!capabilities.supportsDelayedMessages()) {
                // 检查是否允许降级
                if (!checkAndHandleDegradation(capabilities, "delayed messages", event.eventName(),
                        "Please use a MQ that supports delayed messages (e.g., RabbitMQ, RocketMQ)")) {
                    // 降级：立即发布（不设置延迟）
                    publishRemote(event);
                    return;
                }
            }

            // 检查延迟时间是否在支持范围内
            if (!capabilities.isDelaySupported(delayMillis)) {
                log.warn("[Goya] |- component [bus] BusServiceImpl |- delay [{}ms] exceeds the maximum supported delay [{}ms]. " +
                        "The event [{}] will be published with the maximum delay.",
                        delayMillis, capabilities.maxDelayMillis(), event.eventName());
                // 降级：使用最大支持的延迟时间
                delay = Duration.ofMillis(capabilities.maxDelayMillis());
            }
            
            // 构建 Message Headers，包含延迟时间
            MessageHeaders headers = buildMessageHeaders(event, delay, null);
            
            // 创建 Message
            Message<E> message = MessageBuilder.withPayload(event)
                    .copyHeaders(headers)
                    .build();
            
            // 构建 destination
            String destination = buildDestination(event);
            publisher.publish(destination, message);
        } catch (CommonException e) {
            log.warn("[Goya] |- component [bus] BusServiceImpl |- remote event publishing is not available. " +
                    "Please introduce a corresponding starter (e.g., kafka-boot-starter). Delayed event [{}] will not be published remotely. Error: {}",
                    event.eventName(), e.getMessage());
        }
    }

    @Override
    public <E extends IEvent> void publishOrdered(E event, String partitionKey) {
        if (event == null) {
            throw new CommonException("Event cannot be null");
        }
        if (partitionKey == null || partitionKey.isBlank()) {
            throw new CommonException("Partition key cannot be null or blank");
        }

        log.debug("[Goya] |- component [bus] BusServiceImpl |- publish ordered event [{}] with partition key [{}]",
                event.eventName(), partitionKey);

        try {
            IRemoteEventPublisher publisher = chooseRemotePublisher("DISTRIBUTED");
            
            if (publisher == null) {
                log.warn("[Goya] |- component [bus] BusServiceImpl |- remote event publishing is not available. " +
                        "Please introduce a corresponding starter (e.g., kafka-boot-starter). Ordered event [{}] will not be published remotely.",
                        event.eventName());
                return;
            }

            // 检查能力并处理降级
            Capabilities capabilities = publisher.getCapabilities();

            if (!capabilities.supportsOrderedMessages()) {
                // 检查是否允许降级
                if (!checkAndHandleDegradation(capabilities, "ordered messages", event.eventName(),
                        "Please use a MQ that supports ordered messages (e.g., Kafka, RabbitMQ, RocketMQ)")) {
                    // 降级：发布但不保证顺序
                    publishRemote(event);
                    return;
                }
            }

            if (!capabilities.supportsPartitioning()) {
                log.debug("[Goya] |- component [bus] BusServiceImpl |- partitioning is not supported by the MQ, " +
                        "but partition key [{}] will still be set in headers for potential use by the MQ.",
                        partitionKey);
            }
            
            // 构建 Message Headers，包含分区键
            MessageHeaders headers = buildMessageHeaders(event, null, partitionKey);
            
            // 创建 Message
            Message<E> message = MessageBuilder.withPayload(event)
                    .copyHeaders(headers)
                    .build();
            
            // 构建 destination
            String destination = buildDestination(event);
            publisher.publish(destination, message);
        } catch (CommonException e) {
            log.warn("[Goya] |- component [bus] BusServiceImpl |- remote event publishing is not available. " +
                    "Please introduce a corresponding starter (e.g., kafka-boot-starter). Ordered event [{}] will not be published remotely. Error: {}",
                    event.eventName(), e.getMessage());
        }
    }

    /**
     * 检查能力并处理降级
     * <p>如果能力不支持且允许降级，则降级为普通发布</p>
     * <p>如果不允许降级，则抛出异常</p>
     *
     * @param capabilities    MQ 能力
     * @param capabilityName 能力名称（用于日志）
     * @param eventName      事件名称
     * @param suggestion     建议信息
     * @return true 如果继续处理，false 如果已降级
     * @throws CommonException 如果不允许降级
     */
    private <E extends IEvent> boolean checkAndHandleDegradation(
            Capabilities capabilities,
            String capabilityName,
            String eventName,
            String suggestion) {
        // 检查是否允许降级（优先使用全局配置）
        boolean allowDegradation = busProperties.capabilities().allowDegradation() || capabilities.allowDegradation();
        if (!allowDegradation) {
            // 不允许降级，抛出异常
            throw new CommonException(
                    String.format("%s are not supported by the MQ and degradation is not allowed. " +
                            "Event [%s] cannot be published. %s " +
                            "or enable degradation in configuration (goya.bus.capabilities.allow-degradation=true).",
                            capabilityName, eventName, suggestion)
            );
        }
        log.warn("[Goya] |- component [bus] BusServiceImpl |- {} are not supported by the MQ. " +
                        "The event [{}] will be published with degradation enabled. Consider {}.", 
                capabilityName, eventName, suggestion.toLowerCase());
        // 降级：使用普通发布
        return false;
    }

    /**
     * 选择远程事件发布器
     * <p>根据 mark 从 Spring 容器中选择对应的 IRemoteEventPublisher 实现</p>
     *
     * @param mark 发布器标记
     * @return IRemoteEventPublisher 实例，如果不存在则返回 null
     */
    private IRemoteEventPublisher chooseRemotePublisher(String mark) {
        try {
            Map<String, IRemoteEventPublisher> publishers = SpringContext.getBeanMapsOfType(IRemoteEventPublisher.class);
            for (IRemoteEventPublisher publisher : publishers.values()) {
                if (mark.equals(publisher.mark())) {
                    return publisher;
                }
            }
            return null;
        } catch (Exception e) {
            log.debug("[Goya] |- component [bus] BusServiceImpl |- failed to choose remote publisher with mark [{}]: {}",
                    mark, e.getMessage());
            return null;
        }
    }

    /**
     * 构建 destination
     * <p>优先使用配置映射，否则使用默认模板</p>
     *
     * @param event 事件
     * @return destination
     */
    private String buildDestination(IEvent event) {
        String eventName = event.eventName();
        
        // 1. 检查是否有配置映射
        Map<String, String> mappings = busProperties.destination().mappings();
        if (mappings != null && mappings.containsKey(eventName)) {
            return mappings.get(eventName);
        }
        
        // 2. 使用默认模板
        String template = busProperties.destination().defaultTemplate();
        if (StringUtils.isNotBlank(template)) {
            // 替换占位符 {eventName}
            String normalizedEventName = eventName.replace(".", "-");
            return template.replace("{eventName}", normalizedEventName);
        }
        
        // 3. 默认行为：bus.{eventName}
        return "bus." + eventName.replace(".", "-");
    }

    /**
     * 构建 Message Headers
     * <p>统一处理 traceId、幂等键、延迟时间、分区键等</p>
     *
     * @param event       事件
     * @param delay       延迟时间（可选）
     * @param partitionKey 分区键（可选）
     * @return MessageHeaders
     */
    private <E extends IEvent> MessageHeaders buildMessageHeaders(E event, Duration delay, String partitionKey) {
        // 获取或生成 traceId
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }
        
        // 构建 Header Builder
        MetadataAccessor.HeaderBuilder headerBuilder = MetadataAccessor.buildHeaders(event, EventScope.REMOTE)
                .withTraceId(traceId);
        
        // 添加延迟时间
        if (delay != null) {
            headerBuilder.withDelay(delay);
        }
        
        // 添加分区键
        if (partitionKey != null && !partitionKey.isBlank()) {
            headerBuilder.withPartitionKey(partitionKey);
        }

        String idempotencyKey = generateIdempotencyKey(event);
        headerBuilder.withIdempotencyKey(idempotencyKey);
        return headerBuilder.toMessageHeaders();
    }

    /**
     * 生成幂等键
     * <p>基于事件内容生成 MD5 哈希值作为幂等键</p>
     * <p>格式：{eventName}:{hash}</p>
     *
     * @param event 事件
     * @return 幂等键
     */
    private String generateIdempotencyKey(IEvent event) {
        try {
            // 序列化事件对象为 JSON
            String json = JsonUtils.toJson(event);
            if (json == null) {
                log.warn("[Goya] |- component [bus] BusServiceImpl |- failed to serialize event to JSON, fallback to UUID");
                return event.eventName() + ":" + UUID.randomUUID().toString();
            }
            
            // 计算 MD5 哈希值
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(json.getBytes(StandardCharsets.UTF_8));
            
            // 转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b & 0xff));
            }
            String hash = sb.toString();
            
            // 格式：{eventName}:{hash}
            return event.eventName() + ":" + hash;
        } catch (Exception e) {
            log.warn("[Goya] |- component [bus] BusServiceImpl |- failed to generate idempotency key, fallback to UUID: {}",
                    e.getMessage());
            // 如果生成失败，使用 UUID 作为后备方案
            return event.eventName() + ":" + UUID.randomUUID().toString();
        }
    }

    @Override
    public <E extends IEvent> void publishInTransaction(E event, Runnable transactionCallback) {
        if (event == null) {
            throw new CommonException("Event cannot be null");
        }
        if (transactionCallback == null) {
            throw new CommonException("Transaction callback cannot be null");
        }

        log.debug("[Goya] |- component [bus] BusServiceImpl |- publish in transaction event [{}]", event.eventName());

        // 检查是否在事务中
        boolean isTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (!isTransactionActive) {
            log.debug("[Goya] |- component [bus] BusServiceImpl |- not in transaction, executing callback and publishing events directly");
            // 如果不在事务中，直接执行回调和发布事件
            transactionCallback.run();
            publishLocal(event);
            publishRemote(event);
            return;
        }

        // 1. 在事务内执行回调
        transactionCallback.run();

        // 2. 在事务内发布本地事件（同步执行）
        publishLocal(event);

        // 3. 在事务提交后发布远程事件（异步执行）
        // 使用 EventTransactionSynchronization 包装类，支持完整的生命周期管理
        try {
            EventTransactionSynchronization synchronization = new EventTransactionSynchronization(
                    event,
                    () -> {
                        // 事务提交后发布远程事件
                        log.debug("[Goya] |- component [bus] BusServiceImpl |- transaction committed, publishing remote event [{}]",
                                event.eventName());
                        publishRemote(event);
                    }
            );
            TransactionSynchronizationManager.registerSynchronization(synchronization);
            log.trace("[Goya] |- component [bus] BusServiceImpl |- registered transaction synchronization for event [{}]",
                    event.eventName());
        } catch (IllegalStateException e) {
            // 如果注册失败（理论上不应该发生，因为已经检查了事务状态），记录错误并直接发布
            log.error("[Goya] |- component [bus] BusServiceImpl |- failed to register transaction synchronization for event [{}]: {}. " +
                            "Publishing remote event directly.",
                    event.eventName(), e.getMessage());
            publishRemote(event);
        }
    }
}

