package com.ysmjjsy.goya.component.framework.core.enums.dict;

import com.ysmjjsy.goya.component.framework.common.enums.EnumOption;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>枚举字典服务：将 CodeEnum 枚举导出为选项列表（支持 i18n）</p>
 *
 * @author goya
 * @since 2026/1/24 16:01
 */
public interface EnumDictionaryService {

    /**
     * 列出所有可导出枚举（simpleName -> fullName）。
     *
     * @return map
     */
    Map<String, String> listEnums();

    /**
     * 获取 simpleName 冲突信息。
     *
     * @return simpleName -> fullNames
     */
    Map<String, List<String>> conflicts();

    /**
     * 获取指定枚举的选项列表。
     *
     * @param enumName 枚举名（简单名或全限定名）
     * @param locale locale（可为空）
     * @return options
     */
    List<EnumOption> options(String enumName, Locale locale);

    /**
     * 导出所有可导出枚举的 options（simpleName 唯一者）。
     *
     * <p>返回结构：</p>
     * <pre>{@code
     * {
     *   "UserStatus": [{"code":"NORMAL","label":"正常"}, ...],
     *   "UserType":   [{"code":"ADMIN","label":"管理员"}, ...]
     * }
     * }</pre>
     *
     * <p>注意：simpleName 冲突的枚举不会包含在此结果中，需使用全限定名单独查询。</p>
     *
     * @param locale locale（可为空）
     * @return map：simpleName -> options
     */
    Map<String, List<EnumOption>> allOptions(Locale locale);
}
