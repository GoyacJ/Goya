package com.ysmjjsy.goya.component.rabbitmq.autoconfigure;

import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import com.ysmjjsy.goya.component.rabbitmq.RabbitIntegrationBusBinder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * <p>Registers RabbitMQ binder when Spring Integration AMQP</p>
 *
 * @author goya
 * @since 2026/1/27 00:14
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint")
public class RabbitBusBinderAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |- component [rabbitmq] RabbitBusBinderAutoConfiguration auto configure.");
    }

    @Bean
    public BusBinder rabbitBusBinder(RabbitTemplate rabbitTemplate,
                                     ConnectionFactory connectionFactory,
                                     BusChannels channels) {
        return new RabbitIntegrationBusBinder(rabbitTemplate, connectionFactory, channels);
    }
}
