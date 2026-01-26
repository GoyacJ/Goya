package com.ysmjjsy.goya.component.kafka;

import com.ysmjjsy.goya.component.framework.bus.binder.BusBinding;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.messaging.support.ErrorMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Kafka 错误处理器装饰器</p>
 * - 不改变官方错误处理逻辑（委托给已有 CommonErrorHandler）
 * - 额外把异常发布到 bus 全局 error 通道，便于统一日志/告警/指标/链路
 * <p>
 * 注意：这里刻意不做重试/DLQ；这些交给 Spring Kafka 官方错误处理器 + 配置完成
 *
 * @author goya
 * @since 2026/1/27 00:28
 */
public final class KafkaBusPublishingCommonErrorHandler implements CommonErrorHandler {

    private final CommonErrorHandler delegate;
    private final BusChannels channels;
    private final BusBinding binding;

    public KafkaBusPublishingCommonErrorHandler(CommonErrorHandler delegate, BusChannels channels, BusBinding binding) {
        // 如果容器上没有设置错误处理器，兜底用 Spring Kafka 官方默认实现（避免改变整体语义）
        this.delegate = (delegate != null ? delegate : new DefaultErrorHandler());
        this.channels = channels;
        this.binding = binding;
    }

    private void publish(Throwable ex, Object data) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("stage", "kafka-inbound-container");
        headers.put("binder", "kafka");
        headers.put(DefaultBusMessageProducer.HDR_BINDING, binding.name());
        headers.put("destination", binding.destination());
        if (data != null) {
            headers.put("data", data);
        }
        channels.error().send(new ErrorMessage(ex, headers));
    }

    @Override
    public boolean seeksAfterHandling() {
        return delegate.seeksAfterHandling();
    }

    @Override
    public boolean deliveryAttemptHeader() {
        return delegate.deliveryAttemptHeader();
    }

    @Override
    public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer,
                                     MessageListenerContainer container, boolean batchListener) {
        publish(thrownException, null);
        delegate.handleOtherException(thrownException, consumer, container, batchListener);
    }

    @Override
    public void handleRemaining(Exception thrownException, List<ConsumerRecord<?, ?>> records,
                                Consumer<?, ?> consumer, MessageListenerContainer container) {
        publish(thrownException, records);
        delegate.handleRemaining(thrownException, records, consumer, container);
    }

    @Override
    public void handleBatch(Exception thrownException, ConsumerRecords<?, ?> data,
                            Consumer<?, ?> consumer, MessageListenerContainer container, Runnable invokeListener) {
        publish(thrownException, data);
        delegate.handleBatch(thrownException, data, consumer, container, invokeListener);
    }

    @Override
    public ConsumerRecords<?, ?> handleBatchAndReturnRemaining(Exception thrownException, ConsumerRecords<?, ?> data,
                                                               Consumer<?, ?> consumer, MessageListenerContainer container,
                                                               Runnable invokeListener) {
        publish(thrownException, data);
        return delegate.handleBatchAndReturnRemaining(thrownException, data, consumer, container, invokeListener);
    }

    @Override
    public void clearThreadState() {
        delegate.clearThreadState();
    }

    @Override
    public boolean isAckAfterHandle() {
        return delegate.isAckAfterHandle();
    }

    @Override
    public void setAckAfterHandle(boolean ackAfterHandle) {
        delegate.setAckAfterHandle(ackAfterHandle);
    }

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, List<TopicPartition> partitions,
                                     MessageListenerContainer container) {
        delegate.onPartitionsAssigned(consumer, partitions, container);
    }

    @Override
    public void onPartitionsRevoked(Consumer<?, ?> consumer, List<TopicPartition> partitions,
                                    MessageListenerContainer container) {
        delegate.onPartitionsRevoked(consumer, partitions, container);
    }

    @Override
    public void onPartitionsLost(Consumer<?, ?> consumer, List<TopicPartition> partitions,
                                 MessageListenerContainer container) {
        delegate.onPartitionsLost(consumer, partitions, container);
    }

    @Override
    public void onIdleContainer(Consumer<?, ?> consumer, MessageListenerContainer container) {
        delegate.onIdleContainer(consumer, container);
    }

    @Override
    public void handleOne(Exception thrownException, org.apache.kafka.clients.consumer.ConsumerRecord<?, ?> record,
                          Consumer<?, ?> consumer, MessageListenerContainer container) {
        publish(thrownException, record);
        delegate.handleOne(thrownException, record, consumer, container);
    }

    @Override
    public void handleConsumerException(Exception thrownException, Consumer<?, ?> consumer,
                                        MessageListenerContainer container) {
        publish(thrownException, null);
        delegate.handleConsumerException(thrownException, consumer, container);
    }

    @Override
    public void handleCommitFailure(Exception thrownException, List<Map<TopicPartition, OffsetAndMetadata>> commits,
                                    Consumer<?, ?> consumer, MessageListenerContainer container) {
        publish(thrownException, commits);
        delegate.handleCommitFailure(thrownException, commits, consumer, container);
    }
}