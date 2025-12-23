package com.ysmjjsy.goya.auth.server;

import com.ysmjjsy.goya.component.cache.exception.CacheException;
import com.ysmjjsy.goya.component.cache.service.CacheServiceFactory;
import com.ysmjjsy.goya.component.cache.service.ICacheService;
import com.ysmjjsy.goya.component.common.service.IPlatformService;
import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import com.ysmjjsy.goya.component.web.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/12/20 22:18
 */
@Slf4j
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final ICacheService iCacheService;
    private final CacheServiceFactory cacheServiceFactory;

    @GetMapping("test")
    public Response<Void> test() {
        throw new CacheException();
//        String s = I18nResolver.resolveEnum(ResponseCodeEnum.PARAMS_VALIDATION_ERROR);
//        log.warn("s------------>{}",s);
//        return Response.success();
    }

    @GetMapping("test1")
    public Response<LocalDateTime> test1(@RequestParam LocalDateTime time) {
        return Response.success(time);
    }

    @PostMapping("test2")
    public Response<String> test2(@RequestBody TestDTO time) {
        String json = JsonUtils.toJson(time);
        log.warn("json:{}", json);

        return Response.success(json);
    }

    @PostMapping("test3")
    public Response<Void> test3() {
        Set<String> packageNames = IPlatformService.getPackageNames();
        log.warn("json:{}", packageNames);

        return Response.success();
    }

    @PostMapping("test4")
    public Response<Void> test4() {

        String cache = iCacheService.get("test", "cache");

        if (StringUtils.isBlank(cache)) {
            iCacheService.put("test", "cache", "123");
        }

        String cache2 = iCacheService.get("test", "cache");

        log.warn("cache2:{}", cache2);

        return Response.success();
    }

    @PostMapping("test5")
    public Response<Void> test5() {

        TestDTO cache = iCacheService.get("test5", "cache");

        if (Objects.isNull(cache)) {
            TestDTO dto = new TestDTO(
                    "1",
                    LocalDateTime.now(),
                    "1,2,3,4"
            );
            iCacheService.put("test5", "cache", dto);
        }

        TestDTO cache2 = iCacheService.get("test5", "cache");

        log.warn("cache2:{}", cache2);

        return Response.success();
    }
}
