package com.ysmjjsy.goya.component.kafka.autoconfigure;

import com.ysmjjsy.goya.component.framework.bus.binder.BusBinder;
import com.ysmjjsy.goya.component.framework.bus.runtime.BusChannels;
import com.ysmjjsy.goya.component.kafka.KafkaIntegrationBusBinder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/27 00:09
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler")
public class KafkaBusBinderAutoConfiguration {

    @PostConstruct
    public void init() {
        log.debug("[Goya] |-  [component] KafkaBusBinderAutoConfiguration auto configure.");
    }

    @Bean
    public BusBinder kafkaIntegrationBusBinder(KafkaTemplate<Object, Object> kafkaTemplate,
                                    ConcurrentKafkaListenerContainerFactory<Object, Object> factory,
                                    BusChannels channels,
                                    BeanFactory beanFactory) {
        KafkaIntegrationBusBinder kafkaIntegrationBusBinder = new KafkaIntegrationBusBinder(kafkaTemplate, factory, channels, beanFactory);
        log.trace("[Goya] |- component [kafka] KafkaBusBinderAutoConfiguration |- bean [kafkaIntegrationBusBinder] register.");
        return kafkaIntegrationBusBinder;
    }
}

