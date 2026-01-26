package com.ysmjjsy.goya.component.framework.bus.runtime;

import com.ysmjjsy.goya.component.framework.bus.message.BusMessageListener;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * <p>Scans beans for @BusMessageListener methods and registers them.</p>
 *
 * @author goya
 * @since 2026/1/26 23:51
 */
public final class BusMessageListenerRegistrar implements BeanPostProcessor {

    private final BusMessageDispatcher dispatcher;

    public BusMessageListenerRegistrar(BusMessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    @NullMarked
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        ReflectionUtils.doWithMethods(bean.getClass(), m -> {
            BusMessageListener ann = m.getAnnotation(BusMessageListener.class);
            if (ann != null) {
                dispatcher.register(ann.binding(), bean, m);
            }
        }, this::isCandidate);
        return bean;
    }

    private boolean isCandidate(Method m) {
        return m.isAnnotationPresent(BusMessageListener.class);
    }
}