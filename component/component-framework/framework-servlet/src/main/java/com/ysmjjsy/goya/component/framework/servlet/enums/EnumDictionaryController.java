package com.ysmjjsy.goya.component.framework.servlet.enums;

import com.ysmjjsy.goya.component.framework.common.constants.DefaultConst;
import com.ysmjjsy.goya.component.framework.common.enums.EnumOption;
import com.ysmjjsy.goya.component.framework.core.api.ApiRes;
import com.ysmjjsy.goya.component.framework.core.enums.dict.EnumDictionaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
@RequestMapping(DefaultConst.DEFAULT_PROJECT_NAME + "/enums")
@RestController
@RequiredArgsConstructor
public class EnumDictionaryController {

    private final EnumDictionaryService service;

    /**
     * 列出所有可导出枚举（simpleName -> fullName）。
     *
     * @return ApiResponse
     */
    @GetMapping
    public ApiRes<Map<String, String>> listEnums() {
        return ApiRes.ok(service.listEnums());
    }

    /**
     * 列出 simpleName 冲突信息。
     *
     * @return ApiResponse
     */
    @GetMapping("/conflicts")
    public ApiRes<Map<String, List<String>>> conflicts() {
        return ApiRes.ok(service.conflicts());
    }

    /**
     * 获取指定枚举的选项列表。
     *
     * @param enumName 枚举名（simpleName 或 fullName）
     * @return ApiResponse
     */
    @GetMapping("{enumName}")
    public ApiRes<List<EnumOption>> options(@PathVariable String enumName) {
        return ApiRes.ok(service.options(enumName));
    }

    /**
     * 导出所有可导出枚举的 options（simpleName 唯一者）。
     *
     * <p>适合前端启动时一次性拉取并缓存。</p>
     *
     * @return ApiResponse
     */
    @GetMapping("all")
    public ApiRes<Map<String, List<EnumOption>>> all() {
        return ApiRes.ok(service.allOptions());
    }
}