# Kafka Boot Starter

Kafka 事件总线启动器，基于 Spring Cloud Stream 实现。

## 功能特性

- ✅ 基于 Spring Cloud Stream 的 `StreamBridge` 实现消息发送
- ✅ 基于 Spring Cloud Stream 函数式编程实现消息接收
- ✅ 支持重试机制（通过 SCS 配置）
- ✅ 支持死信队列（DLQ，通过 SCS 配置）
- ✅ 支持分区（通过 SCS 配置）
- ✅ 支持延迟消息（通过 Header 传递延迟时间）

## 配置示例

### 基础配置

```yaml
spring:
  cloud:
    function:
      definition: busEventConsumer  # 注册函数式 Consumer
    stream:
      bindings:
        busEventConsumer-in-0:  # Consumer binding
          destination: bus.events  # Kafka topic
          group: ${spring.application.name}  # Consumer group
          consumer:
            max-attempts: 3  # 最大重试次数（SCS 提供）
            dlq-name: bus.events.dlq  # 死信队列（SCS 提供）
          producer:
            partition-key-expression: headers['x-goya-partition-key']  # 分区键表达式（SCS 提供）
```

### 重试配置

```yaml
spring:
  cloud:
    stream:
      bindings:
        busEventConsumer-in-0:
          consumer:
            max-attempts: 3  # 最大重试次数
            back-off-initial-interval: 1000  # 初始退避间隔（毫秒）
            back-off-max-interval: 10000  # 最大退避间隔（毫秒）
            back-off-multiplier: 2.0  # 退避乘数
```

### 分区配置

```yaml
spring:
  cloud:
    stream:
      bindings:
        busEventConsumer-in-0:
          producer:
            partition-key-expression: headers['x-goya-partition-key']  # 基于 Header 的分区键
            partition-count: 3  # 分区数量
```

### 延迟消息配置

某些 binder（如 RabbitMQ）原生支持延迟消息。对于 Kafka，可以通过以下方式实现：

1. 使用延迟插件（如 `kafka-delayed-message-plugin`）
2. 通过业务逻辑实现延迟处理

延迟时间通过 Header `x-goya-delay` 传递（毫秒数）。

## 使用示例

### 发布事件

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final IBusService busService;

    public void createOrder(Order order) {
        // 发布本地事件
        busService.publishLocal(new OrderCreatedEvent(order.getId()));

        // 发布远程事件
        busService.publishRemote(new OrderCreatedEvent(order.getId()));

        // 延迟发布（10秒）
        busService.publishDelayed(new OrderCreatedEvent(order.getId()), Duration.ofSeconds(10));

        // 有序发布（通过分区键）
        busService.publishOrdered(new OrderCreatedEvent(order.getId()), order.getUserId());
    }
}
```

### 订阅事件

```java
@Service
public class OrderEventListener {
    @BusEventListener(scope = {EventScope.REMOTE})
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 处理订单创建事件
    }

    @BusEventListener(
        scope = {EventScope.REMOTE},
        condition = "#event.amount > 1000",
        maxRetries = 3
    )
    public void handleLargeOrder(OrderCreatedEvent event) {
        // 处理大额订单
    }
}
```

## 注意事项

1. **重试和 DLQ**：通过 Spring Cloud Stream 的配置实现，不需要自己实现错误处理逻辑
2. **分区**：通过 `x-goya-partition-key` Header 传递分区键，SCS 自动处理分区逻辑
3. **延迟消息**：延迟时间通过 `x-goya-delay` Header 传递（毫秒数）
4. **幂等性**：通过 `x-goya-idempotency-key` Header 传递幂等键，自动进行幂等性检查

## 参考文档

- [Spring Cloud Stream 官方文档](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream.html)
- [Spring Cloud Stream Kafka Binder](https://docs.spring.io/spring-cloud-stream-binder-kafka/current/reference/html/)

