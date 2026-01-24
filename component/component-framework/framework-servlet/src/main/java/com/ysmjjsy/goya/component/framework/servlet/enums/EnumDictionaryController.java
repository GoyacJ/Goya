package com.ysmjjsy.goya.component.framework.servlet.enums;

import com.ysmjjsy.goya.component.framework.common.enums.EnumOption;
import com.ysmjjsy.goya.component.framework.core.api.ApiResponse;
import com.ysmjjsy.goya.component.framework.core.enums.dict.EnumDictionaryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * <p>枚举字典接口</p>
 *
 * <p>接口：</p>
 * <ul>
 *   <li>GET /goya/enums：列出可导出枚举（simpleName -> fullName）</li>
 *   <li>GET /goya/enums/conflicts：列出 simpleName 冲突（simpleName -> fullName 列表）</li>
 *   <li>GET /goya/enums/{enumName}：获取指定枚举 options</li>
 * </ul>
 *
 * <p>语言选择：</p>
 * <ul>
 *   <li>默认使用当前请求 Locale（由 servlet i18n 解析器决定）</li>
 *   <li>也可通过 ?locale=zh-CN 指定</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/24 16:04
 */
@RequestMapping("/goya/enums")
@RestController
public class EnumDictionaryController {

    private final EnumDictionaryService service;
    private final LocaleProvider localeProvider;

    /**
     * 构造控制器。
     *
     * @param service 字典服务
     * @param localeProvider LocaleProvider
     */
    public EnumDictionaryController(EnumDictionaryService service, LocaleProvider localeProvider) {
        this.service = Objects.requireNonNull(service, "service 不能为空");
        this.localeProvider = Objects.requireNonNull(localeProvider, "localeProvider 不能为空");
    }

    /**
     * 列出所有可导出枚举（simpleName -> fullName）。
     *
     * @return ApiResponse
     */
    @GetMapping
    public ApiResponse<Map<String, String>> listEnums() {
        return ApiResponse.ok(service.listEnums());
    }

    /**
     * 列出 simpleName 冲突信息。
     *
     * @return ApiResponse
     */
    @GetMapping("/conflicts")
    public ApiResponse<Map<String, List<String>>> conflicts() {
        return ApiResponse.ok(service.conflicts());
    }

    /**
     * 获取指定枚举的选项列表。
     *
     * @param enumName 枚举名（simpleName 或 fullName）
     * @param localeTag 语言标签（可为空）
     * @return ApiResponse
     */
    @GetMapping("{enumName}")
    public ApiResponse<List<EnumOption>> options(@PathVariable String enumName,
                                                 @RequestParam(value = "locale", required = false) String localeTag) {
        Locale locale = (localeTag == null || localeTag.isBlank())
                ? localeProvider.currentLocale()
                : Locale.forLanguageTag(localeTag);
        return ApiResponse.ok(service.options(enumName, locale));
    }

    /**
     * 导出所有可导出枚举的 options（simpleName 唯一者）。
     *
     * <p>适合前端启动时一次性拉取并缓存。</p>
     *
     * @param localeTag 语言标签（可为空）
     * @return ApiResponse
     */
    @GetMapping("all")
    public ApiResponse<Map<String, List<EnumOption>>> all(@RequestParam(value = "locale", required = false) String localeTag) {
        Locale locale = (localeTag == null || localeTag.isBlank())
                ? localeProvider.currentLocale()
                : Locale.forLanguageTag(localeTag);

        return ApiResponse.ok(service.allOptions(locale));
    }
}