package com.ysmjjsy.goya.component.framework.bus.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>默认监听器注册表</p>
 * - 启动后扫描容器中所有 Bean 的方法，找出 @BusMessageListener
 * - 将 (binding -> 监听方法列表) 建立映射
 * - dispatch 时按 binding 找到所有监听方法并调用
 * <p>
 * 设计原则：
 * - 尽量使用 Spring 现有工具（MethodIntrospector / AnnotatedElementUtils）
 * - 不搞复杂的“类型路由/反序列化”，先保证最小可运行
 * - 业务监听方法参数支持两种：
 * 1) MessageEnvelope<?>（推荐）
 * 2) Message<?>（高级用法，业务自行处理 header/payload）
 *
 * @author goya
 * @since 2026/1/27 00:41
 */
@Slf4j
public class DefaultBusListenerRegistry implements BusListenerRegistry, SmartInitializingSingleton {

    private final ApplicationContext applicationContext;

    private final Map<String, List<ListenerInvoker>> invokers = new ConcurrentHashMap<>();

    public DefaultBusListenerRegistry(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext);
    }

    @Override
    public void afterSingletonsInstantiated() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean;
            try {
                bean = applicationContext.getBean(beanName);
            } catch (Exception ex) {
                continue;
            }

            Class<?> targetClass = AopUtils.getTargetClass(bean);
            Map<Method, BusMessageListener> methods = MethodIntrospector.selectMethods(
                    targetClass,
                    (MethodIntrospector.MetadataLookup<BusMessageListener>) method ->
                            AnnotatedElementUtils.findMergedAnnotation(method, BusMessageListener.class)
            );

            if (methods.isEmpty()) continue;

            for (Map.Entry<Method, BusMessageListener> e : methods.entrySet()) {
                Method m = e.getKey();
                BusMessageListener ann = e.getValue();

                if (!StringUtils.hasText(ann.binding())) {
                    throw new IllegalStateException("@BusMessageListener(binding) 不能为空: " + targetClass.getName() + "#" + m.getName());
                }

                Class<?>[] params = m.getParameterTypes();
                if (params.length != 1) {
                    throw new IllegalStateException("@BusMessageListener 方法必须且只能有 1 个参数: " + targetClass.getName() + "#" + m.getName());
                }
                Class<?> p0 = params[0];
                boolean ok = MessageEnvelope.class.isAssignableFrom(p0) || Message.class.isAssignableFrom(p0);
                if (!ok) {
                    throw new IllegalStateException("@BusMessageListener 方法参数必须是 MessageEnvelope 或 Message: " + targetClass.getName() + "#" + m.getName());
                }

                m.setAccessible(true);
                invokers.computeIfAbsent(ann.binding(), k -> new CopyOnWriteArrayList<>())
                        .add(new ListenerInvoker(bean, m, p0));

                log.info("注册 Bus 监听器: binding='{}', bean='{}', method='{}#{}'",
                        ann.binding(), beanName, targetClass.getName(), m.getName());
            }
        }
    }

    @Override
    public void dispatch(String binding, Message<?> message) {
        if (!StringUtils.hasText(binding)) {
            throw new MessagingException(message, "消息缺少 binding header: " + DefaultBusMessageProducer.HDR_BINDING);
        }

        List<ListenerInvoker> list = invokers.get(binding);
        if (list == null || list.isEmpty()) {
            log.debug("未找到 Bus 监听器: binding='{}'", binding);
            return;
        }

        Object payload = message.getPayload();
        for (ListenerInvoker invoker : list) {
            invoker.invoke(message, payload);
        }
    }

    private static final class ListenerInvoker {
        private final Object bean;
        private final Method method;
        private final Class<?> paramType;

        private ListenerInvoker(Object bean, Method method, Class<?> paramType) {
            this.bean = bean;
            this.method = method;
            this.paramType = paramType;
        }

        private void invoke(Message<?> message, Object payload) {
            try {
                if (Message.class.isAssignableFrom(paramType)) {
                    method.invoke(bean, message);
                    return;
                }
                if (payload instanceof MessageEnvelope<?> env) {
                    method.invoke(bean, env);
                    return;
                }
                throw new MessagingException(message, "监听器参数为 MessageEnvelope，但 payload 不是 MessageEnvelope: " + payload);
            } catch (MessagingException me) {
                throw me;
            } catch (Exception ex) {
                throw new MessagingException(message, ex);
            }
        }
    }
}