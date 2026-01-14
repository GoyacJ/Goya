package com.ysmjjsy.goya.component.framework.strategy.chain;

import com.ysmjjsy.goya.component.core.exception.CommonException;
import com.ysmjjsy.goya.component.framework.context.SpringContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/19 23:48
 */
public class ChainContext<T> implements CommandLineRunner {

    private final Map<String, List<ChainHandler<T>>> chainContainer = new HashMap<>();

    /**
     * 执行责任链
     */
    public void executeChain(String chainKey, T requestParam) {
        List<ChainHandler<T>> handlers = chainContainer.get(chainKey);

        if (CollectionUtils.isEmpty(handlers)) {
            throw new CommonException(String.format("Chain [%s] is not defined.", chainKey));
        }

        for (ChainHandler<T> handler : handlers) {
            boolean shouldContinue = handler.handle(requestParam);
            if (!shouldContinue) {
                break;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(String... args) {
        Map<String, ChainHandler> beanMap = SpringContext.getBeanMapsOfType(ChainHandler.class);

        beanMap.forEach((name, handler) -> {
            String key = handler.chainKey();
            chainContainer.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(handler);
        });

        // 对责任链内部按 Ordered 排序
        chainContainer.replaceAll((key, list) ->
                list.stream()
                        .sorted(Comparator.comparing(Ordered::getOrder))
                        .toList()
        );
    }
}
