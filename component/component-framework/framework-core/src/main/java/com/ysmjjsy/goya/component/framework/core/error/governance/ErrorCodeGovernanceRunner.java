package com.ysmjjsy.goya.component.framework.core.error.governance;

import com.ysmjjsy.goya.component.framework.common.error.ErrorCode;
import com.ysmjjsy.goya.component.framework.common.error.ErrorCodeCatalog;
import com.ysmjjsy.goya.component.framework.core.autoconfigure.properties.ErrorGovernanceProperties;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * <p>错误码治理执行器：在应用启动阶段对所有已注册的 {@link ErrorCodeCatalog} 进行校验</p>
 *
 * <p>校验内容：</p>
 * <ul>
 *   <li>错误码是否重复</li>
 *   <li>错误码格式是否符合 codePattern</li>
 *   <li>category 是否为空（理论上不应为空）</li>
 *   <li>defaultMessage / messageKey 是否符合要求（可配置）</li>
 * </ul>
 *
 * <p>若 {@link ErrorGovernanceProperties#failFast()} 为 true，则校验失败直接抛出异常终止启动。</p>
 *
 * @author goya
 * @since 2026/1/24 14:26
 */
public class ErrorCodeGovernanceRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ErrorCodeGovernanceRunner.class);

    private final List<ErrorCodeCatalog> catalogs;
    private final ErrorGovernanceProperties props;

    /**
     * 构造治理执行器。
     *
     * @param catalogs 所有错误码目录（可为空列表）
     * @param props    配置项（不能为空）
     */
    public ErrorCodeGovernanceRunner(List<ErrorCodeCatalog> catalogs, ErrorGovernanceProperties props) {
        this.catalogs = catalogs != null ? catalogs : List.of();
        this.props = Objects.requireNonNull(props, "props 不能为空");
    }

    /**
     * 启动时执行校验。
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!props.enabled()) {
            log.info("错误码治理：已禁用");
            return;
        }

        if (CollectionUtils.isEmpty(catalogs)) {
            log.warn("错误码治理：未发现任何 ErrorCodeCatalog（建议至少注册 CommonErrorCodeCatalog）");
            return;
        }

        Pattern pattern = Pattern.compile(props.codePattern());

        Map<String, ErrorCode> seen = new HashMap<>();
        List<String> problems = new ArrayList<>();
        int total = 0;

        for (ErrorCodeCatalog catalog : catalogs) {
            Collection<? extends ErrorCode> codes = catalog.codes();
            if (codes == null) {
                problems.add("目录 " + catalog.getClass().getName() + " 返回 codes() 为 null");
                continue;
            }

            for (ErrorCode c : codes) {
                total++;
                if (c == null) {
                    problems.add("目录 " + catalog.getClass().getName() + " 中存在 null 错误码");
                    continue;
                }

                // 1) code 非空与格式
                if (!StringUtils.hasText(c.code())) {
                    problems.add("错误码 code 为空：catalog=" + catalog.getClass().getName() + ", codeObj=" + c);
                } else if (!pattern.matcher(c.code()).matches()) {
                    problems.add("错误码格式不合法：code=" + c.code() + ", pattern=" + props.codePattern());
                }

                // 2) 重复检测
                if (StringUtils.hasText(c.code())) {
                    ErrorCode prev = seen.putIfAbsent(c.code(), c);
                    if (prev != null && prev != c) {
                        problems.add("错误码重复：code=" + c.code()
                                + ", first=" + prev.getClass().getName()
                                + ", second=" + c.getClass().getName());
                    }
                }

                // 3) category 必须存在
                if (c.category() == null) {
                    problems.add("错误码 category 为空：code=" + c.code());
                }

                // 4) defaultMessage / messageKey
                if (props.requireDefaultMessage() && !StringUtils.hasText(c.defaultMessage())) {
                    problems.add("错误码 defaultMessage 为空：code=" + c.code());
                }
                if (props.requireMessageKey() && !StringUtils.hasText(c.messageKey())) {
                    problems.add("错误码 messageKey 为空：code=" + c.code());
                }
            }
        }

        if (problems.isEmpty()) {
            log.info("错误码治理：校验通过，总计 {} 个错误码", total);
            return;
        }

        String report = buildReport(total, problems);
        if (props.failFast()) {
            throw new IllegalStateException(report);
        } else {
            log.error(report);
        }
    }

    /**
     * 构造治理报告文本。
     *
     * @param total    错误码总数
     * @param problems 问题列表
     * @return 报告文本
     */
    private String buildReport(int total, List<String> problems) {
        StringBuilder sb = new StringBuilder(256);
        sb.append("错误码治理：校验失败").append('\n');
        sb.append("总计错误码：").append(total).append('\n');
        sb.append("问题数量：").append(problems.size()).append('\n');
        sb.append("问题明细：").append('\n');
        for (int i = 0; i < problems.size(); i++) {
            sb.append("  ").append(i + 1).append(". ").append(problems.get(i)).append('\n');
        }
        return sb.toString();
    }
}
