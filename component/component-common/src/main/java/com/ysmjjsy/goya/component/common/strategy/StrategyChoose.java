package com.ysmjjsy.goya.component.common.strategy;

import com.ysmjjsy.goya.component.common.context.ApplicationInitializingEvent;
import com.ysmjjsy.goya.component.common.context.SpringContext;
import com.ysmjjsy.goya.component.common.definition.exception.CommonException;
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

    private final Map<String, IStrategyExecute> strategyMap = new HashMap<>();

    public IStrategyExecute choose(String mark) {
        return Optional.ofNullable(strategyMap.get(mark))
                .orElseThrow(() -> new CommonException("Strategy [" + mark + "] not found."));
    }

    public <I> void chooseAndExecute(String mark, I request) {
        choose(mark).execute(request);
    }

    public <I, O> O chooseAndExecuteResp(String mark, I request) {
        return (O) choose(mark).executeResp(request);
    }

    @Override
    public void onApplicationEvent(ApplicationInitializingEvent event) {
        Map<String, IStrategyExecute> beans = SpringContext.getBeanMapsOfType(IStrategyExecute.class);

        beans.forEach((name, bean) -> {
            if (strategyMap.containsKey(bean.mark())) {
                throw new CommonException("Duplicate Strategy mark: " + bean.mark());
            }
            strategyMap.put(bean.mark(), bean);
        });
    }
}
