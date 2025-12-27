# Component Bus - 事务总线

## 📋 目录

- [简介](#简介)
- [核心特性](#核心特性)
- [架构设计](#架构设计)
- [快速开始](#快速开始)
- [使用指南](#使用指南)
- [配置说明](#配置说明)
- [高级特性](#高级特性)
- [最佳实践](#最佳实践)
- [常见问题](#常见问题)
- [参考文档](#参考文档)

---

## 简介

`component-bus` 是 Goya 框架的事件总线组件，提供统一的事件发布与订阅机制，支持本地事件和分布式远程事件。基于 Spring ApplicationEventPublisher 和 Spring Cloud Stream 实现，提供声明式的事件监听、幂等性保证、链路追踪等企业级特性。

### 设计理念

- **统一抽象**：提供与中间件无关的事件总线抽象，支持多种消息中间件（Kafka、RabbitMQ 等）
- **声明式编程**：通过 `@BusEventListener` 注解声明式订阅事件，简化开发
- **幂等性保证**：内置幂等性检查机制，防止重复处理事件
- **链路追踪**：自动透传 traceId，支持分布式链路追踪
- **灵活路由**：支持事件名称匹配、类型匹配、SpEL 条件表达式等多种路由方式

---

## 核心特性

### ✅ 事件发布

- **本地事件**：基于 Spring ApplicationEventPublisher，仅在当前 JVM 内发布
- **远程事件**：基于 Spring Cloud Stream，支持跨服务发布
- **延迟消息**：支持延迟发布事件（通过 Header 传递延迟时间），自动检查 MQ 能力并降级
- **有序消息**：通过分区键保证同一分区键的消息有序处理，自动检查 MQ 能力并降级
- **事务性发布**：支持 `publishInTransaction()` 方法，确保事务提交后才发布远程事件

### ✅ 事件订阅

- **声明式监听**：通过 `@BusEventListener` 注解声明事件监听器
- **多种匹配方式**：
  - 事件名称匹配（`eventNames`）
  - 参数类型匹配（基于 `getSimpleName()`）
  - SpEL 条件表达式（`condition`）
- **作用域控制**：支持 `LOCAL`、`REMOTE`、`ALL` 三种作用域
- **异步执行**：支持异步处理事件（`async = true`）

### ✅ 幂等性保证

- **全局幂等性**：基于事件内容自动生成幂等键，防止重复处理
- **监听器级幂等性**：每个监听器独立幂等性检查
- **原子操作**：支持 `checkAndSetAtomic()` 方法，使用本地锁保证原子性
- **可配置 TTL**：支持配置幂等键的过期时间

### ✅ 链路追踪

- **自动透传**：自动从 MDC 获取 traceId，并在远程事件中透传
- **MDC 管理**：自动设置和清理 MDC，支持分布式链路追踪

### ✅ 消息确认

- **统一抽象**：提供 `IEventAcknowledgment` 接口，统一不同 MQ 的 ACK 机制
- **自动确认**：默认自动确认消息（`AckMode.AUTO`）
- **手动确认**：支持手动确认消息（`AckMode.MANUAL`），精确控制确认时机
- **适配器模式**：自动适配 Kafka、RabbitMQ 等不同 MQ 的 Acknowledgment

### ✅ 反序列化

- **自动类型发现**：通过 Header 中的事件类型信息自动反序列化
- **降级方案**：反序列化失败时，支持 String 类型的监听器作为降级方案

### ✅ 事件模型增强

- **事件 ID**：每个事件自动生成唯一 ID，用于追踪和回放
- **事件版本**：支持事件版本管理，便于事件演进
- **关联 ID**：支持关联相关事件，用于事件链路追踪
- **时间戳**：自动记录事件发生时间

### ✅ Pipeline 模式

- **拦截器链**：使用 Pipeline 模式处理事件，支持自定义拦截器
- **职责分离**：反序列化、幂等性检查、路由、调用等逻辑分离
- **可扩展性**：支持自定义拦截器，灵活扩展处理逻辑

### ✅ 能力声明与降级

- **能力声明**：MQ 发布器声明支持的能力（延迟消息、顺序消息、分区等）
- **自动降级**：当 MQ 不支持某项能力时，自动降级或给出警告
- **诚实 API**：API 对开发者诚实，不会造成"能力错觉"

### ✅ 可观测性

- **指标记录**：自动记录事件发布、消费、成功、失败等指标
- **处理时间**：记录平均、最大、最小处理时间
- **成功率统计**：自动计算成功率

---

## 架构设计

### 模块结构

```
component-bus/
├── annotation/              # 注解定义
│   └── BusEventListener.java
├── configuration/           # 自动配置
│   ├── BusAutoConfiguration.java
│   └── properties/
│       └── BusProperties.java
├── constants/              # 常量定义
│   └── IBusConstants.java
├── definition/             # 核心定义
│   ├── BaseEvent.java
│   ├── BusHeaders.java
│   ├── EventScope.java
│   └── IEvent.java
├── deserializer/           # 反序列化
│   ├── DeserializationResult.java
│   └── EventDeserializer.java
├── enums/                  # 枚举定义
│   └── AckMode.java
├── exception/             # 异常定义
│   └── BusException.java
├── handler/                 # 处理器
│   ├── CacheIdempotencyHandler.java
│   └── IIdempotencyHandler.java
├── processor/             # 处理器
│   ├── BusEventListenerHandler.java
│   └── BusEventListenerScanner.java
├── publish/               # 发布器
│   ├── IRemoteEventPublisher.java
│   ├── LocalEventPublisher.java
│   ├── MetadataAccessor.java
│   └── StreamEventPublisher.java
└── service/              # 服务接口
    ├── DefaultBusService.java
    └── IBusService.java
```

### 核心组件

#### 1. IBusService（事件总线服务）

统一的事件发布接口，提供以下方法：

- `publishLocal(E event)`：发布本地事件
- `publishRemote(E event)`：发布远程事件
- `publishAll(E event)`：发布到本地和远程
- `publishDelayed(E event, Duration delay)`：延迟发布事件
- `publishOrdered(E event, String partitionKey)`：有序发布事件

#### 2. BusEventListenerHandler（事件监听器处理器）

负责事件路由、幂等性检查、监听器调用等核心逻辑：

- 处理本地事件和远程事件
- 根据作用域、事件名称、参数类型、SpEL 条件匹配监听器
- 执行全局和监听器级别的幂等性检查
- 支持手动 ACK 注入

#### 3. BusEventListenerScanner（事件监听器扫描器）

扫描 `@BusEventListener` 注解的方法，建立索引：

- 事件名称索引：按 `eventNames` 建立索引
- 参数类型索引：按参数类型的 `getSimpleName()` 建立索引

#### 4. EventDeserializer（事件反序列化器）

从 `Message<?>` 中反序列化事件：

- 优先使用 Header 中的事件类型信息
- 支持多种 payload 类型（IEvent、String、byte[]、Map）
- 反序列化失败时返回 JSON 字符串和事件名称

#### 5. CacheIdempotencyHandler（幂等性处理器）

基于 `ICacheService` 实现幂等性检查：

- 使用缓存存储已处理的事件标识
- 支持配置 TTL（默认 24 小时）

### 数据流

#### 本地事件流程

```
发布者 → IBusService.publishLocal()
  → LocalEventPublisher.execute()
    → BusEventListenerHandler.handleLocalEvent()
      → 匹配监听器（作用域、事件名称、类型、条件）
        → 调用监听器方法
```

#### 远程事件流程

```
发布者 → IBusService.publishRemote()
  → DefaultBusService.buildMessageHeaders()（构建 Headers）
    → StreamEventPublisher.publish()
      → StreamBridge.send()（发送到消息中间件）
        → 消息中间件（Kafka/RabbitMQ）
          → Consumer<Message<?>>（接收消息）
            → BusEventListenerHandler.handleRemoteMessage()
              → EventDeserializer.deserialize()（反序列化）
                → 全局幂等性检查
                  → 匹配监听器
                    → 监听器级幂等性检查
                      → 调用监听器方法
```

---

## 快速开始

### 1. 添加依赖

在 `pom.xml` 中添加 `component-bus` 依赖：

```xml
<dependency>
    <groupId>com.ysmjjsy.goya</groupId>
    <artifactId>component-bus</artifactId>
</dependency>
```

### 2. 定义事件

实现 `IEvent` 接口或继承 `BaseEvent`：

```java
/**
 * 订单创建事件
 */
public record OrderCreatedEvent(
    String orderId,
    BigDecimal amount,
    String userId
) implements IEvent {
    @Override
    public String eventName() {
        return "order.created";
    }
}
```

### 3. 发布事件

注入 `IBusService` 并发布事件：

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final IBusService busService;

    public void createOrder(Order order) {
        // 业务逻辑...
        
        // 发布本地事件
        busService.publishLocal(new OrderCreatedEvent(
            order.getId(),
            order.getAmount(),
            order.getUserId()
        ));
    }
}
```

### 4. 订阅事件

使用 `@BusEventListener` 注解订阅事件：

```java
@Service
public class OrderEventListener {
    
    @BusEventListener(scope = {EventScope.LOCAL})
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("订单创建事件: {}", event.orderId());
        // 处理订单创建逻辑...
    }
}
```

---

## 使用指南

### 事件定义

#### 方式一：实现 IEvent 接口

```java
public record UserUpdatedEvent(
    String userId,
    String userName
) implements IEvent {
    @Override
    public String eventName() {
        return "user.updated";
    }
}
```

#### 方式二：继承 BaseEvent

```java
public class OrderPaidEvent extends BaseEvent {
    private final String orderId;
    private final BigDecimal amount;

    public OrderPaidEvent(String orderId, BigDecimal amount) {
        this.orderId = orderId;
        this.amount = amount;
    }

    @Override
    public String eventName() {
        return "order.paid";
    }

    // getters...
}
```

### 事件发布

#### 本地事件

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final IBusService busService;

    public void createOrder(Order order) {
        // 发布本地事件（仅在当前 JVM 内）
        busService.publishLocal(new OrderCreatedEvent(order.getId()));
    }
}
```

#### 远程事件

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final IBusService busService;

    public void createOrder(Order order) {
        // 发布远程事件（跨服务）
        busService.publishRemote(new OrderCreatedEvent(order.getId()));
        
        // 注意：需要引入对应的 starter（如 kafka-boot-starter）
    }
}
```

#### 本地和远程

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final IBusService busService;

    public void createOrder(Order order) {
        // 同时发布本地和远程事件
        busService.publishAll(new OrderCreatedEvent(order.getId()));
    }
}
```

#### 延迟发布

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final IBusService busService;

    public void createOrder(Order order) {
        // 延迟 10 秒发布
        busService.publishDelayed(
            new OrderCreatedEvent(order.getId()),
            Duration.ofSeconds(10)
        );
    }
}
```

#### 有序发布

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final IBusService busService;

    public void createOrder(Order order) {
        // 通过用户 ID 作为分区键，保证同一用户的消息有序处理
        busService.publishOrdered(
            new OrderCreatedEvent(order.getId()),
            order.getUserId()
        );
    }
}
```

### 事件订阅

#### 基础用法

```java
@Service
public class OrderEventListener {
    
    @BusEventListener(scope = {EventScope.LOCAL})
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("订单创建: {}", event.orderId());
    }
}
```

#### 指定事件名称

```java
@Service
public class OrderEventListener {
    
    @BusEventListener(
        scope = {EventScope.REMOTE},
        eventNames = {"order.created", "order.updated"}
    )
    public void handleOrderEvents(OrderCreatedEvent event) {
        log.info("订单事件: {}", event.orderId());
    }
}
```

#### SpEL 条件表达式

```java
@Service
public class OrderEventListener {
    
    @BusEventListener(
        scope = {EventScope.REMOTE},
        condition = "#event.amount > 1000"
    )
    public void handleLargeOrder(OrderCreatedEvent event) {
        log.info("大额订单: {}", event.orderId());
    }
}
```

#### 异步执行

```java
@Service
public class OrderEventListener {
    
    @BusEventListener(
        scope = {EventScope.LOCAL},
        async = true
    )
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 异步执行，不阻塞主线程
        log.info("订单创建: {}", event.orderId());
    }
}
```

#### 手动确认（仅远程事件）

```java
@Service
public class OrderEventListener {
    
    @BusEventListener(
        scope = {EventScope.REMOTE},
        ackMode = AckMode.MANUAL
    )
    public void handleOrderCreated(OrderCreatedEvent event, Acknowledgment ack) {
        try {
            // 处理事件
            processOrder(event);
            // 手动确认
            ack.acknowledge();
        } catch (Exception e) {
            // 不确认，触发重试
            log.error("处理失败", e);
        }
    }
}
```

#### String 类型监听器（降级方案）

当事件类型在本地不存在时，可以使用 String 类型的监听器：

```java
@Service
public class OrderEventListener {
    
    @BusEventListener(
        scope = {EventScope.REMOTE},
        eventNames = {"order.created"}
    )
    public void handleOrderCreated(String jsonEvent) {
        // jsonEvent 是事件的 JSON 字符串
        log.info("订单创建（String）: {}", jsonEvent);
        // 可以手动解析 JSON
    }
}
```

### 事件作用域

#### LOCAL（本地事件）

仅在当前 JVM 内发布和订阅，基于 Spring ApplicationEventPublisher：

```java
@BusEventListener(scope = {EventScope.LOCAL})
public void handleLocalEvent(OrderCreatedEvent event) {
    // 仅处理本地事件
}
```

#### REMOTE（远程事件）

跨服务发布和订阅，基于 Spring Cloud Stream：

```java
@BusEventListener(scope = {EventScope.REMOTE})
public void handleRemoteEvent(OrderCreatedEvent event) {
    // 仅处理远程事件
}
```

#### ALL（全部）

同时处理本地和远程事件：

```java
@BusEventListener(scope = {EventScope.ALL})
public void handleAllEvents(OrderCreatedEvent event) {
    // 处理本地和远程事件
}
```

---

## 配置说明

### 基础配置

在 `application.yml` 中配置：

```yaml
goya:
  bus:
    # 默认远程总线标记（用于策略选择）
    defaultRemoteBus: "REMOTE"
    
    # 幂等性配置
    idempotency:
      enabled: true                    # 是否启用幂等性检查
      cacheName: "bus:idempotency"    # 缓存名称
      ttl: PT24H                       # TTL（ISO-8601 格式，默认 24 小时）
    
    # Destination 配置
    destination:
      # 默认模板（支持占位符 {eventName}）
      defaultTemplate: "bus.{eventName}"
      # 事件名称到 destination 的映射
      mappings:
        "order.created": "bus.order-created"
        "user.updated": "bus.user-updated"
```

### 配置说明

#### defaultRemoteBus

默认远程总线标记，用于策略选择。如果引入了多个 starter（如 kafka-boot-starter、rabbitmq-boot-starter），可以通过此配置选择默认使用的总线。

#### idempotency

幂等性配置：

- `enabled`：是否启用幂等性检查（默认 `true`）
- `cacheName`：用于存储幂等键的缓存名称（默认 `"bus:idempotency"`）
- `ttl`：幂等键的过期时间（ISO-8601 格式，默认 `PT24H`）

#### destination

Destination 配置（用于远程事件）：

- `defaultTemplate`：默认模板，支持 `{eventName}` 占位符
  - 示例：`"bus.{eventName}"` → `"bus.order-created"`
- `mappings`：事件名称到 destination 的映射表
  - 优先级：`mappings` > `defaultTemplate` > 默认行为（`"bus.{eventName}"`）

---

## 高级特性

### 幂等性保证

#### 工作原理

1. **全局幂等性**：基于事件内容自动生成 MD5 哈希值作为幂等键
   - 格式：`{eventName}:{hash}`
   - 在事件处理入口处检查，如果已存在则跳过处理

2. **监听器级幂等性**：每个监听器独立幂等性检查
   - 格式：`{globalIdempotencyKey}:{listenerIdentifier}`
   - 监听器标识符：`{className}#{methodName}`

#### 配置示例

```yaml
goya:
  bus:
    idempotency:
      enabled: true
      cacheName: "bus:idempotency"
      ttl: PT24H  # 24 小时
```

#### 自定义幂等性处理器

实现 `IIdempotencyHandler` 接口：

```java
@Component
public class CustomIdempotencyHandler implements IIdempotencyHandler {
    
    @Override
    public boolean checkAndSet(String idempotencyKey) {
        // 自定义幂等性检查逻辑
        // 返回 true 表示未处理（已设置），false 表示已处理
    }
}
```

### 链路追踪

#### 自动透传

事件总线会自动从 MDC 获取 `traceId`，并在远程事件中透传：

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final IBusService busService;

    public void createOrder(Order order) {
        // MDC 中已有 traceId
        MDC.put("traceId", "trace-123");
        
        // 发布远程事件，traceId 会自动透传到 Header
        busService.publishRemote(new OrderCreatedEvent(order.getId()));
    }
}
```

#### 接收端自动设置

在事件监听器中，`traceId` 会自动设置到 MDC：

```java
@Service
public class OrderEventListener {
    
    @BusEventListener(scope = {EventScope.REMOTE})
    public void handleOrderCreated(OrderCreatedEvent event) {
        // traceId 已自动设置到 MDC
        String traceId = MDC.get("traceId");
        log.info("处理订单创建事件，traceId: {}", traceId);
    }
}
```

### 延迟消息

#### 发布延迟消息

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final IBusService busService;

    public void createOrder(Order order) {
        // 延迟 10 秒发布
        busService.publishDelayed(
            new OrderCreatedEvent(order.getId()),
            Duration.ofSeconds(10)
        );
    }
}
```

#### 注意事项

- **Kafka**：Kafka 不支持原生延迟消息，通过 `ScheduledExecutorService` 实现延迟发送
- **RabbitMQ**：RabbitMQ 支持原生延迟消息（通过 `x-delay` Header）

### 有序消息

#### 发布有序消息

通过分区键保证同一分区键的消息有序处理：

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final IBusService busService;

    public void updateOrder(Order order) {
        // 使用用户 ID 作为分区键，保证同一用户的消息有序处理
        busService.publishOrdered(
            new OrderUpdatedEvent(order.getId()),
            order.getUserId()
        );
    }
}
```

#### Spring Cloud Stream 配置

需要在 Spring Cloud Stream 配置中设置分区键表达式：

```yaml
spring:
  cloud:
    stream:
      bindings:
        busEventConsumer-in-0:
          producer:
            partition-key-expression: headers['x-goya-partition-key']
            partition-count: 3
```

### 事件匹配规则

#### 匹配优先级

1. **作用域匹配**（必须）
   - 监听器的 `scope` 必须包含事件的作用域或 `ALL`

2. **事件匹配**（满足任一即可）
   - `@BusEventListener.eventNames` 包含事件的 `eventName()`
   - 参数类型的 `getSimpleName()` 等于事件的 `eventName()`

3. **SpEL 条件匹配**（如果指定，必须满足）
   - `@BusEventListener.condition` 表达式计算结果为 `true`

#### 示例

```java
// 事件定义
public record OrderCreatedEvent(String orderId) implements IEvent {
    @Override
    public String eventName() {
        return "order.created";
    }
}

// 监听器 1：通过 eventNames 匹配
@BusEventListener(
    scope = {EventScope.REMOTE},
    eventNames = {"order.created"}
)
public void handleOrderCreated(OrderCreatedEvent event) {
    // 匹配成功
}

// 监听器 2：通过参数类型 SimpleName 匹配
@BusEventListener(scope = {EventScope.REMOTE})
public void handleOrderCreated(OrderCreatedEvent event) {
    // 匹配成功（参数类型 SimpleName = "OrderCreatedEvent"）
    // 但事件的 eventName() = "order.created"，不匹配
    // 注意：此示例不会匹配，因为 eventName() 不等于 SimpleName
}

// 监听器 3：通过 SpEL 条件匹配
@BusEventListener(
    scope = {EventScope.REMOTE},
    condition = "#event.orderId.startsWith('ORD')"
)
public void handleOrderCreated(OrderCreatedEvent event) {
    // 匹配成功（满足条件）
}
```

---

## 最佳实践

### 1. 事件命名规范

建议使用点分隔的命名方式：

```java
// ✅ 推荐
public record OrderCreatedEvent(String orderId) implements IEvent {
    @Override
    public String eventName() {
        return "order.created";
    }
}

// ❌ 不推荐
public record OrderCreatedEvent(String orderId) implements IEvent {
    @Override
    public String eventName() {
        return "OrderCreatedEvent";  // 使用类名
    }
}
```

### 2. 事件设计原则

- **不可变性**：事件应该是不可变的（使用 `record` 或 `final` 类）
- **最小化数据**：只包含必要的数据，避免传递大量数据
- **版本兼容**：考虑事件版本兼容性，避免破坏性变更

### 3. 监听器设计

- **单一职责**：每个监听器只处理一种业务逻辑
- **幂等性**：确保监听器逻辑是幂等的
- **异常处理**：合理处理异常，避免影响其他监听器

```java
@Service
public class OrderEventListener {
    
    @BusEventListener(scope = {EventScope.REMOTE})
    public void handleOrderCreated(OrderCreatedEvent event) {
        try {
            // 处理逻辑
            processOrder(event);
        } catch (Exception e) {
            // 记录日志，但不抛出异常（避免触发重试）
            log.error("处理订单创建事件失败", e);
        }
    }
}
```

### 4. 性能优化

- **异步处理**：对于耗时操作，使用 `async = true`
- **批量处理**：考虑批量处理事件，减少数据库操作
- **缓存优化**：合理使用缓存，减少重复计算

### 5. 监控和日志

- **结构化日志**：使用结构化日志，便于追踪
- **指标监控**：监控事件发布和消费的指标
- **异常告警**：设置异常告警，及时发现问题

---

## 常见问题

### Q1: 远程事件发布失败，提示 "remote event publishing is not available"

**A**: 需要引入对应的 starter（如 `kafka-boot-starter`）。如果不需要远程事件，可以忽略此警告。

### Q2: 监听器没有被调用

**A**: 检查以下几点：

1. 监听器的作用域是否匹配（`scope`）
2. 事件名称是否匹配（`eventNames` 或参数类型 `SimpleName`）
3. SpEL 条件是否满足（`condition`）
4. 监听器是否被 Spring 管理（`@Service`、`@Component` 等）

### Q3: 如何实现跨服务事件订阅？

**A**: 

1. 引入对应的 starter（如 `kafka-boot-starter`）
2. 配置 Spring Cloud Stream（参考 `kafka-boot-starter/README.md`）
3. 使用 `@BusEventListener(scope = {EventScope.REMOTE})` 订阅远程事件

### Q4: 如何自定义幂等性处理器？

**A**: 实现 `IIdempotencyHandler` 接口并注册为 Bean：

```java
@Component
public class CustomIdempotencyHandler implements IIdempotencyHandler {
    @Override
    public boolean checkAndSet(String idempotencyKey) {
        // 自定义逻辑
    }
}
```

### Q5: 延迟消息在 Kafka 中如何实现？

**A**: Kafka 不支持原生延迟消息，`kafka-boot-starter` 通过 `ScheduledExecutorService` 实现延迟发送。如果需要更精确的延迟控制，建议使用 RabbitMQ 或 RocketMQ。

### Q6: 如何保证消息顺序？

**A**: 使用 `publishOrdered()` 方法，并传入分区键。同一分区键的消息会被发送到同一个分区，保证有序处理。

### Q7: 手动 ACK 如何使用？

**A**: 

1. 设置 `ackMode = AckMode.MANUAL`
2. 在监听器方法中添加 `Acknowledgment` 参数
3. 处理成功后调用 `ack.acknowledge()`

```java
@BusEventListener(
    scope = {EventScope.REMOTE},
    ackMode = AckMode.MANUAL
)
public void handleOrderCreated(OrderCreatedEvent event, Acknowledgment ack) {
    try {
        processOrder(event);
        ack.acknowledge();
    } catch (Exception e) {
        // 不确认，触发重试
    }
}
```

---

## 参考文档

### 相关模块

- [kafka-boot-starter](../starter/kafka-boot-starter/README.md)：Kafka 事件总线启动器
- [component-cache](../component-cache/README.md)：缓存组件（用于幂等性检查）

### 官方文档

- [Spring Events](https://docs.spring.io/spring-framework/reference/core/events.html)
- [Spring Cloud Stream](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream.html)
- [Spring Cloud Stream Kafka Binder](https://docs.spring.io/spring-cloud-stream-binder-kafka/current/reference/html/)

### 设计参考

- [Event-Driven Architecture](https://martinfowler.com/articles/201701-event-driven.html)
- [Idempotency Patterns](https://www.baeldung.com/idempotent-operations)

---

## 版本历史

- **1.1.0** (2025/12/21)
  - ✨ 新增事件模型增强：eventId、eventVersion、correlationId、timestamp
  - ✨ 新增事务性发布：`publishInTransaction()` 方法
  - ✨ 新增幂等性原子操作：`checkAndSetAtomic()` 方法
  - ✨ 新增 Pipeline 模式：支持拦截器链，职责分离
  - ✨ 新增统一 ACK 抽象：`IEventAcknowledgment` 接口
  - ✨ 新增能力声明与降级：MQ 能力声明，自动降级策略
  - ✨ 新增可观测性：EventMetrics 指标记录
  - ⚠️ 废弃 `publishAll()` 方法（存在事务语义混淆风险）
  - 🔧 优化事件处理流程，提升可维护性

- **1.0.0** (2025/12/21)
  - 初始版本
  - 支持本地和远程事件发布
  - 支持声明式事件订阅
  - 支持幂等性保证
  - 支持链路追踪
  - 支持延迟消息和有序消息

---

## 迁移指南

### 从 1.0.0 迁移到 1.1.0

#### 1. 事件模型增强

**变更**：`IEvent` 接口新增了 `eventId()`、`eventVersion()`、`correlationId()`、`timestamp()` 方法。

**影响**：
- 如果使用 `BaseEvent`，无需修改
- 如果直接实现 `IEvent`，需要实现这些方法（或使用默认实现）

**示例**：
```java
// 1.0.0
public record OrderCreatedEvent(String orderId) implements IEvent {
    @Override
    public String eventName() {
        return "order.created";
    }
}

// 1.1.0（无需修改，使用默认实现）
public record OrderCreatedEvent(String orderId) implements IEvent {
    @Override
    public String eventName() {
        return "order.created";
    }
    // eventId()、eventVersion() 等使用默认实现
}
```

#### 2. 事务语义明确

**变更**：`publishAll()` 方法被标记为 `@Deprecated`，新增 `publishInTransaction()` 方法。

**迁移步骤**：

1. **替换 `publishAll()` 调用**：
```java
// 1.0.0
@Transactional
public void createOrder(Order order) {
    orderRepository.save(order);
    busService.publishAll(new OrderCreatedEvent(order.getId()));
}

// 1.1.0（推荐）
@Transactional
public void createOrder(Order order) {
    orderRepository.save(order);
    busService.publishInTransaction(
        new OrderCreatedEvent(order.getId()),
        () -> {
            // 事务内的其他操作
        }
    );
}

// 或分别调用
@Transactional
public void createOrder(Order order) {
    orderRepository.save(order);
    busService.publishLocal(new OrderCreatedEvent(order.getId()));
    // 远程事件在事务提交后发布
    busService.publishRemote(new OrderCreatedEvent(order.getId()));
}
```

2. **理解事务语义**：
   - `publishLocal()`：在事务内同步执行
   - `publishRemote()`：在事务外异步执行（可能失败）
   - `publishInTransaction()`：本地事件在事务内，远程事件在事务提交后

#### 3. 幂等性原子操作

**变更**：新增 `checkAndSetAtomic()` 方法，用于高并发场景。

**迁移步骤**：

如果需要在 high-concurrency 场景下使用原子操作：

```java
// 1.0.0
if (idempotencyHandler.checkAndSet(idempotencyKey)) {
    processEvent(event);
}

// 1.1.0（高并发场景）
if (idempotencyHandler.checkAndSetAtomic(idempotencyKey)) {
    processEvent(event);
}
```

**注意**：`checkAndSet()` 方法仍然可用，适用于低并发场景。

#### 4. 统一 ACK 抽象

**变更**：新增 `IEventAcknowledgment` 接口，统一不同 MQ 的 ACK 机制。

**迁移步骤**：

如果使用手动 ACK：

```java
// 1.0.0（Kafka 特定）
@BusEventListener(scope = {EventScope.REMOTE}, ackMode = AckMode.MANUAL)
public void handleEvent(OrderCreatedEvent event, org.springframework.kafka.support.Acknowledgment ack) {
    processEvent(event);
    ack.acknowledge();
}

// 1.1.0（统一接口）
@BusEventListener(scope = {EventScope.REMOTE}, ackMode = AckMode.MANUAL)
public void handleEvent(OrderCreatedEvent event, IEventAcknowledgment ack) {
    processEvent(event);
    ack.acknowledge();
}
```

**注意**：仍然支持原始 Acknowledgment 类型（向后兼容）。

#### 5. 能力声明与降级

**变更**：新增 `Capabilities` 和 `getCapabilities()` 方法。

**影响**：
- `publishDelayed()` 和 `publishOrdered()` 会自动检查 MQ 能力
- 如果不支持，会自动降级或给出警告
- 无需修改代码，但建议了解 MQ 的能力限制

**示例**：
```java
// 1.1.0（自动检查能力）
busService.publishDelayed(event, Duration.ofSeconds(10));
// 如果 MQ 不支持延迟消息，会自动降级为立即发布，并记录警告日志
```

#### 6. Pipeline 模式

**变更**：事件处理使用 Pipeline 模式，支持自定义拦截器。

**影响**：
- 默认行为不变
- 可以自定义拦截器扩展处理逻辑

**示例**：
```java
// 1.1.0（自定义拦截器）
@Component
public class CustomInterceptor implements IEventInterceptor {
    @Override
    public void intercept(EventContext context) {
        // 自定义处理逻辑
    }

    @Override
    public int getOrder() {
        return 50; // 执行顺序
    }
}
```

#### 7. 可观测性

**变更**：新增 `EventMetrics` 工具类，自动记录指标。

**使用**：
```java
// 1.1.0（查看指标）
EventMetrics.MetricsSnapshot snapshot = EventMetrics.getSnapshot();
System.out.println(snapshot);
// 输出：EventMetrics{ publish=100, consume=95, success=90, failure=5, ... }
```

---

## 兼容性说明

### 向后兼容

- ✅ `publishAll()` 方法仍然可用（但已废弃）
- ✅ `checkAndSet()` 方法仍然可用
- ✅ 原始 Acknowledgment 类型仍然支持
- ✅ 所有现有 API 保持不变

### 不兼容变更

- ⚠️ 无（所有变更都是向后兼容的）

---

## 升级建议

1. **立即升级**：如果使用 `publishAll()`，建议尽快迁移到 `publishInTransaction()`
2. **逐步迁移**：其他功能可以逐步迁移，不影响现有功能
3. **测试验证**：升级后建议进行充分测试，特别是事务相关场景

---

## 贡献指南

欢迎提交 Issue 和 Pull Request。

---

**作者**: goya  
**最后更新**: 2025/12/21

