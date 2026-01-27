package com.ysmjjsy.goya.platform.monolith.controller;

import com.ysmjjsy.goya.component.framework.cache.api.CacheService;
import com.ysmjjsy.goya.component.framework.cache.api.MultiLevelCacheService;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.platform.monolith.controller.dto.TestCacheDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p></p>
 *
 * @author goya
 * @since 2026/1/27 14:55
 */
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/test")
@RestController
public class TestController {

    private final CacheService cacheService;
    private final MultiLevelCacheService multiLevelCacheService;

    @GetMapping("cache")
    public ApiRes<String> cache() {
        Object o = cacheService.get("test", "test1", String.class);
        if (o == null) {
            cacheService.put("test", "test1", "test1");
        }
        String o1 = cacheService.get("test", "test1", String.class);
        return ApiRes.ok(o1);
    }

    @GetMapping("cache1")
    public ApiRes<TestCacheDTO> cache1() {
        TestCacheDTO o = cacheService.get("test", "test2", TestCacheDTO.class);
        if (o == null) {
            cacheService.put("test", "test2", new TestCacheDTO("111", "1"));
        }
        TestCacheDTO o1 = cacheService.get("test", "test2", TestCacheDTO.class);
        return ApiRes.ok(o1);
    }

    @GetMapping("cache2")
    public ApiRes<TestCacheDTO> cache2() {
        TestCacheDTO o = multiLevelCacheService.get("test", "test3", TestCacheDTO.class);
        if (o == null) {
            multiLevelCacheService.put("test", "test3", new TestCacheDTO("111", "1"));
        }
        TestCacheDTO o1 = multiLevelCacheService.get("test", "test3", TestCacheDTO.class);
        return ApiRes.ok(o1);
    }
}
