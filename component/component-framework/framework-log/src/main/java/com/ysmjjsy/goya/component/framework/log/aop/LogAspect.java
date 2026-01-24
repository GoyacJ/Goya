package com.ysmjjsy.goya.component.framework.log.aop;

import com.ysmjjsy.goya.component.framework.common.utils.GoyaStringUtils;
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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    private final LogProperties props;
    private final Masker masker;
    private final MethodArgMasker argMasker;
    private final RequestInfoExtractor requestInfoExtractor;

    /**
     * 注解切点：方法或类上标注 @Loggable。
     */
    @Pointcut("@annotation(com.ysmjjsy.goya.component.framework.log.aop.Loggable) || @within(com.ysmjjsy.goya.component.framework.log.aop.Loggable)")
    public void loggablePointcut() {
    }

    /**
     * 包拦截切点：在 AutoConfiguration 中动态决定是否启用（此处只定义表达式由配置拼装）。
     *
     * <p>为了避免复杂表达式拼装，本实现采用“运行时判断”方式：
     * 统一拦截 public 方法，再根据配置 include/exclude 做快速过滤。</p>
     */
    @Pointcut("execution(public * *(..))")
    public void publicMethodPointcut() {
    }

    /**
     * 环绕增强：记录入参/出参/耗时/异常。
     *
     * @param pjp join point
     * @return 返回值
     * @throws Throwable 原异常透传
     */
    @Around("publicMethodPointcut()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        if (!props.enabled() || !props.aop().enabled()) {
            return pjp.proceed();
        }

        MethodSignature ms = (MethodSignature) pjp.getSignature();
        Method method = ms.getMethod();
        Class<?> declaring = ms.getDeclaringType();

        Loggable ann = findLoggable(method, declaring);

        // 规则：若未配置 includePackages，则必须有 @Loggable 才记录
        boolean hasIncludes = props.aop().includePackages() != null && !props.aop().includePackages().isEmpty();
        if (!hasIncludes && ann == null) {
            return pjp.proceed();
        }

        // 若配置了 includePackages，则按包过滤（exclude 优先）
        if (hasIncludes && !matchPackage(declaring.getName())) {
            return pjp.proceed();
        }

        boolean logArgs = (ann != null) ? ann.logArgs() : props.aop().logArgs();
        boolean logResult = (ann != null) ? ann.logResult() : props.aop().logResult();
        boolean mask = (ann == null) || ann.mask();

        String sig = declaring.getSimpleName() + "." + method.getName();
        long startNs = System.nanoTime();

        Object[] args = pjp.getArgs();
        if (logArgs) {
            Map<String, Object> safeArgs = argMasker.maskArgs(method, args, mask);
            log.info("调用开始 {} args={}", sig, safeArgs);
        } else {
            log.info("调用开始 {}", sig);
        }
        Object ret = null;
        Throwable error = null;
        try {
            ret = pjp.proceed();
            long costMs = (System.nanoTime() - startNs) / 1_000_000;
            if (logResult) {
                Object safeRet = mask ? masker.mask(ret) : ret;
                String out = GoyaStringUtils.truncate(String.valueOf(safeRet), props.aop().maxResultLength());
                logByCost(costMs, "调用结束 {} costMs={} result={}", sig, costMs, out);
            } else {
                logByCost(costMs, "调用结束 {} costMs={}", sig, costMs);
            }
            return ret;
        } catch (Throwable ex) {
            long costMs = (System.nanoTime() - startNs) / 1_000_000;
            log.error("调用异常 {} costMs={} ex={}", sig, costMs, ex, ex);
            error = ex;
            throw ex;
        } finally {
            long costMs = (System.nanoTime() - startNs) / 1_000_000;
            publishInvokeEvent(method, declaring, args, ret, error, costMs, mask);
        }
    }

    private void logByCost(long costMs, String template, Object... args) {
        if (costMs >= props.slowThresholdMs()) {
            log.warn(template, args);
        } else {
            log.info(template, args);
        }
    }

    private Loggable findLoggable(Method method, Class<?> declaring) {
        Loggable m = method.getAnnotation(Loggable.class);
        if (m != null) {
            return m;
        }
        return declaring.getAnnotation(Loggable.class);
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