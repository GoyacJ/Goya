package com.ysmjjsy.goya.component.log.aspect;

import com.ysmjjsy.goya.component.bus.stream.service.IBusService;
import com.ysmjjsy.goya.component.core.constants.DefaultConst;
import com.ysmjjsy.goya.component.core.utils.GoyaCollectionUtils;
import com.ysmjjsy.goya.component.core.utils.GoyaIdUtils;
import com.ysmjjsy.goya.component.core.utils.GoyaMapUtils;
import com.ysmjjsy.goya.component.core.utils.GoyaStringUtils;
import com.ysmjjsy.goya.component.framework.context.GoyaContext;
import com.ysmjjsy.goya.component.framework.context.SpringContext;
import com.ysmjjsy.goya.component.framework.enums.StateEnum;
import com.ysmjjsy.goya.component.framework.json.GoyaJson;
import com.ysmjjsy.goya.component.log.annotation.GoyaLog;
import com.ysmjjsy.goya.component.log.event.OperatorLogEvent;
import com.ysmjjsy.goya.component.web.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/16 13:29
 */
@Aspect
@Slf4j
@Order(1)
@RequiredArgsConstructor
public class GoyaLogAspect {

    /**
     * 排除敏感属性字段
     */
    public static final String[] EXCLUDE_PROPERTIES = {"password", "oldPassword", "newPassword", "confirmPassword"};


    /**
     * 计时 key
     */
    private static final ThreadLocal<StopWatch> KEY_CACHE = new ThreadLocal<>();

    private final IBusService iBus;

    /**
     * 处理请求前执行
     */
    @Before(value = "@annotation(controllerLog)")
    public void doBefore(JoinPoint joinPoint, GoyaLog controllerLog) {
        StopWatch stopWatch = new StopWatch();
        KEY_CACHE.set(stopWatch);
        stopWatch.start();
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, GoyaLog controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, GoyaLog controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    protected void handleLog(final JoinPoint joinPoint, GoyaLog controllerLog, final Exception e, Object jsonResult) {
        try {

            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();

            // 设置消耗时间
            StopWatch stopWatch = KEY_CACHE.get();
            stopWatch.stop();

            String userId = DefaultConst.DEFAULT_USER;
            GoyaContext goyaContext = SpringContext.getBean(GoyaContext.class);
            if (Objects.nonNull(goyaContext)) {
                userId = goyaContext.currentUser().getUserId();
            }

            StateEnum stateEnum = StateEnum.ENABLED;
            String errorMsg = "";

            if (Objects.nonNull(e)) {
                stateEnum = StateEnum.DISABLED;
                errorMsg = StringUtils.substring(e.getMessage(), 0, 255);
            }

            // *========数据库日志=========*//
            OperatorLogEvent operLog = new OperatorLogEvent(
                    GoyaIdUtils.getSeataSnowflakeNextIdStr(),
                    "",
                    controllerLog.title(),
                    className + "." + methodName + "()",
                    WebUtils.getRequest().getMethod(),
                    controllerLog.operatorType(),
                    userId,
                    StringUtils.substring(WebUtils.getRequest().getRequestURI(), 0, 255),
                    WebUtils.getClientIp(),
                    getOperParam(joinPoint, WebUtils.getRequest().getMethod(), controllerLog),
                    getJsonResult(controllerLog, jsonResult),
                    stateEnum,
                    errorMsg,
                    LocalDateTime.now(),
                    stopWatch.getDuration().toMillis()
            );
            // 发布事件保存数据库
            iBus.publishLocal(operLog);
            iBus.publishRemote(operLog);
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("异常信息:{}", exp.getMessage());
        } finally {
            KEY_CACHE.remove();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log 日志
     * @throws Exception
     */
    public String getJsonResult(GoyaLog log, Object jsonResult) {
        // 是否需要保存response，参数和值
        if (log.isSaveResponseData() && Objects.nonNull(jsonResult)) {
            return StringUtils.substring(GoyaJson.toJson(jsonResult), 0, 3800);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @throws Exception 异常
     */
    private String getOperParam(JoinPoint joinPoint, String requestMethod, GoyaLog log) {
        if (log.isSaveRequestData()) {
            Map<String, String> paramsMap = WebUtils.getParamMap(WebUtils.getRequest());
            if (GoyaMapUtils.isEmpty(paramsMap) && GoyaStringUtils.equalsAnyIgnoreCase(requestMethod, HttpMethod.PUT.name(), HttpMethod.POST.name(), HttpMethod.DELETE.name())) {
                String params = argsArrayToString(joinPoint.getArgs(), log.excludeParamNames());
                return StringUtils.substring(params, 0, 3800);
            } else {
                GoyaMapUtils.removeAny(paramsMap, EXCLUDE_PROPERTIES);
                GoyaMapUtils.removeAny(paramsMap, log.excludeParamNames());
                return StringUtils.substring(GoyaJson.toJson(paramsMap), 0, 3800);
            }
        }
        return StringUtils.EMPTY;
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        StringJoiner params = new StringJoiner(" ");

        if (GoyaCollectionUtils.isEmpty(paramsArray)) {
            return params.toString();
        }

        String[] exclude = GoyaCollectionUtils.addAll(excludeParamNames, EXCLUDE_PROPERTIES);
        Set<String> excludeSet = Set.of(exclude);

        for (Object arg : paramsArray) {
            if (arg == null || isFilterObject(arg)) {
                continue;
            }

            String json = serializeAndFilter(arg, excludeSet);
            if (json != null) {
                params.add(json);
            }
        }

        return params.toString();
    }

    private String serializeAndFilter(Object value, Set<String> excludeSet) {
        if (value instanceof List<?> list) {
            List<Map<String, Object>> filteredList = new ArrayList<>();

            for (Object element : list) {
                Map<String, Object> map = GoyaJson.toMap(element);
                if (map != null && !map.isEmpty()) {
                    map.keySet().removeAll(excludeSet);
                    filteredList.add(map);
                }
            }

            return GoyaJson.toJson(filteredList);
        }

        Map<String, Object> map = GoyaJson.toMap(value);
        if (map != null && !map.isEmpty()) {
            map.keySet().removeAll(excludeSet);
            return GoyaJson.toJson(map);
        }

        return GoyaJson.toJson(value);
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    @SuppressWarnings("rawtypes")
    public boolean isFilterObject(final Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()) {
            return MultipartFile.class.isAssignableFrom(clazz.getComponentType());
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.values()) {
                return value instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse
                || o instanceof BindingResult;
    }
    
}
