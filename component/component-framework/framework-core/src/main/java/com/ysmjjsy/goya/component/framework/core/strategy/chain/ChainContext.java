package com.ysmjjsy.goya.component.framework.core.strategy.chain;

import com.ysmjjsy.goya.component.framework.common.error.CommonErrorCode;
import com.ysmjjsy.goya.component.framework.common.exception.Exceptions;
import com.ysmjjsy.goya.component.framework.core.context.SpringContext;
import org.jspecify.annotations.NullMarked;
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
            throw Exceptions.system(CommonErrorCode.SYSTEM_ERROR).userMessage(String.format("Chain [%s] is not defined.", chainKey)).build();
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
    @NullMarked
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
