package com.ysmjjsy.goya.component.web.utils;

import com.ysmjjsy.goya.component.common.definition.constants.ISymbolConstants;
import com.ysmjjsy.goya.component.common.utils.JsonUtils;
import com.ysmjjsy.goya.component.common.utils.ResourceResolverUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringEscapeUtils;
import org.owasp.validator.html.*;

import java.io.IOException;
import java.net.URL;

/**
 * <p></p>
 *
 * @author goya
 * @since 2025/10/8 22:23
 */
@Slf4j
public class XssUtils {
    
    private static volatile XssUtils INSTANCE;
    private final AntiSamy antiSamy;
    private final String nbsp;
    private final String quot;

    private XssUtils() {
        Policy policy = createPolicy();
        this.antiSamy = ObjectUtils.isNotEmpty(policy) ? new AntiSamy(policy) : new AntiSamy();
        this.nbsp = cleanHtml(ISymbolConstants.NBSP);
        this.quot = cleanHtml(ISymbolConstants.QUOTE);
    }

    private static XssUtils getInstance() {
        if (ObjectUtils.isEmpty(INSTANCE)) {
            synchronized (XssUtils.class) {
                if (ObjectUtils.isEmpty(INSTANCE)) {
                    INSTANCE = new XssUtils();
                }
            }
        }

        return INSTANCE;
    }

    public static String process(String taintedHtml) {
        // 对转义的HTML特殊字符（<、>、"等）进行反转义，因为AntiSamy调用scan方法时会将特殊字符转义
        String cleanHtml = StringEscapeUtils.unescapeHtml4(getInstance().cleanHtml(taintedHtml));

        if (Strings.CS.startsWith(cleanHtml, ISymbolConstants.NEW_LINE)) {
            // StringEscapeUtils.unescapeHtml4 转换某些内容时，会在开头增加 \n。去除之后才好判断，否则下面判断是否是 json 会出错。
            cleanHtml = Strings.CS.removeStart(cleanHtml, ISymbolConstants.NEW_LINE);
        }

        if (JsonUtils.isJson(cleanHtml) && Strings.CS.contains(cleanHtml, ISymbolConstants.NEW_LINE)) {
            // AntiSamy会把“ ”转换 \n。如果出现时间字符串，中间包含空格就会出现错误"
            cleanHtml = cleanHtml.replaceAll(ISymbolConstants.NEW_LINE, ISymbolConstants.SPACE);
        }
        // AntiSamy会把“&nbsp;”转换成乱码，把双引号转换成"&quot;" 先将&nbsp;的乱码替换为空，双引号的乱码替换为双引号
        String temp = cleanHtml.replaceAll(getInstance().nbsp, ISymbolConstants.BLANK);
        String result = temp.replaceAll(getInstance().quot, ISymbolConstants.QUOTE);
        log.trace("[GOYA] |- Antisamy process value from [{}] to [{}]", taintedHtml, result);
        return result;
    }

    private Policy createPolicy() {
        try {
            URL url = ResourceResolverUtils.getUrl("classpath:antisamy/antisamy-anythinggoes.xml");
            return Policy.getInstance(url);
        } catch (IOException | PolicyException e) {
            log.warn("[GOYA] |- Antisamy create policy error! {}", e.getMessage());
            return null;
        }
    }

    private CleanResults scan(String taintedHtml) throws ScanException, PolicyException {
        return antiSamy.scan(taintedHtml);
    }

    private String cleanHtml(String taintedHtml) {
        try {
            // 使用AntiSamy清洗数据
            final CleanResults cleanResults = scan(taintedHtml);
            return cleanResults.getCleanHTML();
        } catch (ScanException | PolicyException e) {
            log.error("[GOYA] |- Antisamy scan catch error! {}", e.getMessage());
            return taintedHtml;
        }
    }
}
