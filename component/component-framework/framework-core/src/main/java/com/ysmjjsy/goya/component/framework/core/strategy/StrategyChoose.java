package com.ysmjjsy.goya.component.framework.core.strategy;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.Exceptions;
import com.ysmjjsy.goya.component.framework.common.stragegy.StrategyExecute;
import com.ysmjjsy.goya.component.framework.core.context.SpringContext;
import com.ysmjjsy.goya.component.framework.core.processor.ApplicationInitializingEvent;
import org.springframework.context.ApplicationListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * <p>策略选择器</p>
 *
 * @author goya
 * @since 2025/12/19 23:47
 */
@SuppressWarnings("all")
public class StrategyChoose implements ApplicationListener<ApplicationInitializingEvent> {

    private final Map<String, StrategyExecute> strategyMap = new HashMap<>();

    public StrategyExecute choose(String mark) {
        return Optional.ofNullable(strategyMap.get(mark))
                .orElseThrow(() -> Exceptions.system(CommonErrorCode.SYSTEM_ERROR).userMessage("Strategy [" + mark + "] not found.").build());
    }

    public <I> void chooseAndExecute(String mark, I request) {
        choose(mark).execute(request);
    }

    public <I, O> O chooseAndExecuteResp(String mark, I request) {
        return (O) choose(mark).executeResp(request);
    }

    @Override
    public void onApplicationEvent(ApplicationInitializingEvent event) {
        Map<String, StrategyExecute> beans = SpringContext.getBeanMapsOfType(StrategyExecute.class);

        beans.forEach((name, bean) -> {
            if (strategyMap.containsKey(bean.mark())) {
                throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).userMessage("Duplicate Strategy mark: " + bean.mark()).build();
            }
            strategyMap.put(bean.mark(), bean);
        });
    }
}
