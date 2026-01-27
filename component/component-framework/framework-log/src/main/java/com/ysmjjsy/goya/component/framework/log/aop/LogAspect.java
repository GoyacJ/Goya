package com.ysmjjsy.goya.component.framework.log.aop;

import com.ysmjjsy.goya.component.framework.core.context.SpringContext;
import com.ysmjjsy.goya.component.framework.core.json.GoyaJson;
import com.ysmjjsy.goya.component.framework.core.web.RequestInfo;
import com.ysmjjsy.goya.component.framework.core.web.RequestInfoExtractor;
import com.ysmjjsy.goya.component.framework.log.autoconfigure.properties.LogProperties;
import com.ysmjjsy.goya.component.framework.log.event.LoggableMethodInvokedEvent;
import com.ysmjjsy.goya.component.framework.log.event.MethodInvokeEventPayload;
import com.ysmjjsy.goya.component.framework.log.mask.MethodArgMasker;
import com.ysmjjsy.goya.component.framework.masker.core.Masker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Map;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/24 22:05
 */
@Aspect
@RequiredArgsConstructor
@Slf4j
public class LogAspect {

    private final LogProperties props;
    private final Masker masker;
    private final MethodArgMasker argMasker;
    private final RequestInfoExtractor requestInfoExtractor;

    /**
     * 计时 key
     */
    private static final ThreadLocal<StopWatch> KEY_CACHE = new ThreadLocal<>();

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(loggable)")
    public void doBefore(JoinPoint joinPoint, Loggable loggable) {
        StopWatch stopWatch = new StopWatch();
        KEY_CACHE.set(stopWatch);
        stopWatch.start();
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(loggable)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Loggable loggable, Object jsonResult) {
        around(joinPoint, loggable, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(loggable)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Loggable loggable, Exception e) {
        around(joinPoint, loggable, e, null);
    }


    /**
     * 环绕增强：记录入参/出参/耗时/异常。
     *
     * @param joinPoint join point
     * @param ann       loggable
     * @param e         e
     */
    public void around(JoinPoint joinPoint, Loggable ann, Exception e, Object jsonResult) {
        if (!props.enabled() || !props.aop().enabled()) {
            return;
        }

        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        Method method = ms.getMethod();
        Class<?> declaring = ms.getDeclaringType();

        // 规则：若未配置 includePackages，则必须有 @Loggable 才记录
        boolean hasIncludes = props.aop().includePackages() != null && !props.aop().includePackages().isEmpty();
        if (!hasIncludes && ann == null) {
            return;
        }

        // 若配置了 includePackages，则按包过滤（exclude 优先）
        if (hasIncludes && !matchPackage(declaring.getName())) {
            return;
        }

        boolean logArgs = (ann != null) ? ann.logArgs() : props.aop().logArgs();
        boolean mask = (ann == null) || ann.mask();

        String sig = declaring.getSimpleName() + "." + method.getName();

        Object[] args = joinPoint.getArgs();
        if (logArgs) {
            Map<String, Object> safeArgs = argMasker.maskArgs(method, args, mask);
            log.info("调用开始 {} args={}", sig, safeArgs);
        } else {
            log.info("调用开始 {}", sig);
        }

        // 设置消耗时间
        StopWatch stopWatch = KEY_CACHE.get();
        stopWatch.stop();
        long costMs = stopWatch.getTotalTimeMillis();
        logByCost(costMs, "调用结束 {} costMs={} result={}", sig, stopWatch.getTotalTimeMillis(), jsonResult);
        publishInvokeEvent(method, declaring, args, jsonResult, e, costMs, mask);
    }

    private void logByCost(long costMs, String template, Object... args) {
        if (costMs >= props.slowThresholdMs()) {
            log.warn(template, args);
        } else {
            log.info(template, args);
        }
    }

    private boolean matchPackage(String className) {
        // exclude 优先
        if (props.aop().excludePackages() != null) {
            for (String ex : props.aop().excludePackages()) {
                if (ex != null && !ex.isBlank() && className.startsWith(ex)) {
                    return false;
                }
            }
        }
        if (props.aop().includePackages() == null || props.aop().includePackages().isEmpty()) {
            return true;
        }
        for (String in : props.aop().includePackages()) {
            if (in != null && !in.isBlank() && className.startsWith(in)) {
                return true;
            }
        }
        return false;
    }

    private void publishInvokeEvent(Method method,
                                    Class<?> declaring,
                                    Object[] args,
                                    Object result,
                                    Throwable error,
                                    long costMs,
                                    boolean maskEnabled) {

        boolean success = (error == null);
        String className = declaring.getName();
        String methodName = method.getName();
        String signature = declaring.getSimpleName() + "." + methodName + "()";

        Object eventArgs;
        // 参数级 @Sensitive 优先（更精准）
        eventArgs = argMasker.maskArgs(method, args, maskEnabled);

        Object eventResult = null;
        if (success) {
            eventResult = maskEnabled ? masker.mask(result) : result;
        }

        String exType = null;
        String exMsg = null;
        String exStack = null;
        if (!success) {
            exType = error.getClass().getName();
            exMsg = safe(error.getMessage());
            exStack = stacktraceToString(error, props.eventErrorMaxLen());
        }

        Map<String, String> mdc = MDC.getCopyOfContextMap();
        if (mdc == null) {
            mdc = Map.of();
        }

        RequestInfo req = requestInfoExtractor.extract();

        MethodInvokeEventPayload payload = new MethodInvokeEventPayload(
                Instant.now(),
                SpringContext.getDisplayName(),
                SpringContext.getApplicationName(),
                req == null ? null : req.requestUri(),
                req == null ? null : req.httpMethod(),
                req == null ? null : GoyaJson.toJson(req.userAgent()),
                req == null ? null : req.clientIp(),
                className,
                method.getName(),
                signature,
                costMs,
                success,
                eventArgs,
                eventResult,
                exType,
                exMsg,
                exStack,
                mdc
        );

        SpringContext.publishEvent(new LoggableMethodInvokedEvent(this, payload));
    }

    private String safe(String s) {
        return StringUtils.hasText(s) ? s : null;
    }

    /**
     * 将异常堆栈转换为字符串，并限制最大长度，避免事件体过大。
     *
     * @param t      异常
     * @param maxLen 最大长度
     * @return 堆栈字符串
     */
    private String stacktraceToString(Throwable t, int maxLen) {
        try {
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            t.printStackTrace(pw);
            pw.flush();
            String s = sw.toString();
            if (s.length() <= maxLen) {
                return s;
            }
            return s.substring(0, maxLen) + "...(truncated)";
        } catch (Exception e) {
            return t.toString();
        }
    }
}