package com.ysmjjsy.goya.component.common.context;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:40
 */
public class ApplicationContentPostProcessor implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

    private ApplicationContext applicationContext;
    /**
     * 执行标识，确保Spring事件 {@link ApplicationReadyEvent} 有且执行一次
     */
    private boolean executeOnlyOnce = true;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        synchronized (ApplicationContentPostProcessor.class) {
            if (executeOnlyOnce) {
                applicationContext.publishEvent(new ApplicationInitializingEvent(this, event.getApplicationContext()));
                executeOnlyOnce = false;
            }
        }
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
