package com.ysmjjsy.goya.component.kafka;

import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;
import com.ysmjjsy.goya.component.framework.bus.binder.BusBinding;
import com.ysmjjsy.goya.component.framework.bus.message.DefaultBusMessageProducer;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>Kafka Binder：使用 Spring Integration Kafka 官方适配器接入 Kafka</p>
 * <p>
 * Outbound：KafkaProducerMessageHandler
 * Inbound：KafkaMessageDrivenChannelAdapter + ConcurrentMessageListenerContainer
 * <p>
 * 额外：inbound 容器层异常发布到 bus 全局 error 通道（不替代官方重试/DLQ等机制）。
 *
 * @author goya
 * @since 2026/1/27 00:08
 */
@Slf4j
@RequiredArgsConstructor
public final class KafkaIntegrationBusBinder implements BusBinder, DisposableBean {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ConcurrentKafkaListenerContainerFactory<Object, Object> factory;
    private final BusChannels channels;
    private final BeanFactory beanFactory;

    private final List<KafkaMessageDrivenChannelAdapter<Object, Object>> inboundAdapters = new CopyOnWriteArrayList<>();

    @Override
    public String name() {
        return "kafka";
    }

    @Override
    public void bindOutbound(BusBinding binding, SubscribableChannel outboundChannel) {
        KafkaProducerMessageHandler<Object, Object> handler = new KafkaProducerMessageHandler<>(kafkaTemplate);
        handler.setBeanFactory(beanFactory);
        handler.setTopicExpression(new LiteralExpression(binding.destination()));
        handler.afterPropertiesSet();

        outboundChannel.subscribe((Message<?> msg) -> {
            try {
                handler.handleMessage(msg);
            } catch (Exception ex) {
                log.error("Kafka outbound 发送失败: binding='{}', destination='{}'",
                        binding.name(), binding.destination(), ex);
                throw ex;
            }
        });

        log.info("Kafka outbound 已绑定: binding='{}', destination='{}'",
                binding.name(), binding.destination());
    }

    @Override
    public void bindInbound(BusBinding binding, SubscribableChannel inboundChannel) {
        ConcurrentMessageListenerContainer<Object, Object> container = factory.createContainer(binding.destination());

        if (StringUtils.hasText(binding.group())) {
            container.getContainerProperties().setGroupId(binding.group());
        }

        CommonErrorHandler existing = container.getCommonErrorHandler();
        container.setCommonErrorHandler(new PublishingCommonErrorHandler(existing, channels, binding));

        KafkaMessageDrivenChannelAdapter<Object, Object> adapter = new KafkaMessageDrivenChannelAdapter<>(container);

        adapter.setBeanFactory(beanFactory);

        adapter.setOutputChannel(inboundChannel);
        adapter.afterPropertiesSet();
        adapter.start();

        inboundAdapters.add(adapter);

        log.info("Kafka inbound 已绑定: binding='{}', destination='{}', group='{}'",
                binding.name(), binding.destination(), binding.group());
    }

    @Override
    public void destroy() {
        for (KafkaMessageDrivenChannelAdapter<Object, Object> adapter : inboundAdapters) {
            try {
                adapter.stop();
            } catch (Exception ex) {
                log.warn("Kafka inbound adapter stop 失败（忽略）", ex);
            }
        }
        inboundAdapters.clear();
    }

    /**
     * CommonErrorHandler 装饰器：
     * - 先发布到 bus error 通道（便于统一观测）
     * - 再委托给原有 error handler（保留官方重试/DLQ/seek 等语义）
     *
     * 注意：该实现严格对齐你贴出来的 CommonErrorHandler 源码签名。
     */
    private static final class PublishingCommonErrorHandler implements CommonErrorHandler {

        private final CommonErrorHandler delegate;
        private final BusChannels channels;
        private final BusBinding binding;

        private PublishingCommonErrorHandler(CommonErrorHandler delegate, BusChannels channels, BusBinding binding) {
            // 没有现成 handler 时，兜底用官方 DefaultErrorHandler（不造轮子）
            this.delegate = (delegate != null ? delegate : new DefaultErrorHandler());
            this.channels = channels;
            this.binding = binding;
        }

        private void publish(Throwable ex, Object data) {
            HashMap<String, Object> headers = new HashMap<>();
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
        @NullMarked
        public void handleOtherException(Exception thrownException, Consumer<?, ?> consumer,
                                         MessageListenerContainer container, boolean batchListener) {
            publish(thrownException, null);
            delegate.handleOtherException(thrownException, consumer, container, batchListener);
        }

        @Override
        @NullMarked
        public boolean handleOne(Exception thrownException, ConsumerRecord<?, ?> record, Consumer<?, ?> consumer,
                                 MessageListenerContainer container) {
            publish(thrownException, record);
            return delegate.handleOne(thrownException, record, consumer, container);
        }

        @Override
        @NullMarked
        public void handleRemaining(Exception thrownException, List<ConsumerRecord<?, ?>> records,
                                    Consumer<?, ?> consumer, MessageListenerContainer container) {
            publish(thrownException, records);
            delegate.handleRemaining(thrownException, records, consumer, container);
        }

        @Override
        @NullMarked
        public void handleBatch(Exception thrownException, ConsumerRecords<?, ?> data,
                                Consumer<?, ?> consumer, MessageListenerContainer container, Runnable invokeListener) {
            publish(thrownException, data);
            delegate.handleBatch(thrownException, data, consumer, container, invokeListener);
        }

        @Override
        @NullMarked
        public <K, V> ConsumerRecords<K, V> handleBatchAndReturnRemaining(Exception thrownException,
                                                                          ConsumerRecords<?, ?> data,
                                                                          Consumer<?, ?> consumer,
                                                                          MessageListenerContainer container,
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
        public void setAckAfterHandle(boolean ack) {
            // delegate 可能不支持设置，保持其行为（接口默认是抛 UnsupportedOperationException）
            delegate.setAckAfterHandle(ack);
        }

        @Override
        @NullMarked
        public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions,
                                         Runnable publishPause) {
            delegate.onPartitionsAssigned(consumer, partitions, publishPause);
        }
    }
}