package com.ysmjjsy.goya.component.mybatisplus.permission.compiler;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>权限规则编译解释信息（Explain）</p>
 * <p>
 * 用于记录权限规则编译过程中的告警、错误与关键决策点，便于审计与排障。
 *
 * <p><b>设计原则：</b>
 * <ul>
 *   <li>Explain 只描述“发生了什么”，不携带敏感数据（如具体业务参数）</li>
 *   <li>编译失败时，Explain 应包含足够定位信息（resource/fieldKey/predicateType 等）</li>
 * </ul>
 *
 * @author goya
 * @since 2026/1/28 22:43
 */
public final class Explain implements Serializable {

    @Serial
    private static final long serialVersionUID = -6530276848168077016L;

    /**
     * 编译信息条目。
     */
    public record Entry(Level level, String message) implements Serializable {
        public enum Level { INFO, WARN, ERROR }
    }

    private final List<Entry> entries = new ArrayList<>();

    /**
     * 记录 INFO。
     *
     * @param message 信息
     */
    public void info(String message) {
        entries.add(new Entry(Entry.Level.INFO, message));
    }

    /**
     * 记录 WARN。
     *
     * @param message 警告
     */
    public void warn(String message) {
        entries.add(new Entry(Entry.Level.WARN, message));
    }

    /**
     * 记录 ERROR。
     *
     * @param message 错误
     */
    public void error(String message) {
        entries.add(new Entry(Entry.Level.ERROR, message));
    }

    /**
     * 获取不可变条目列表。
     *
     * @return 条目列表
     */
    public List<Entry> entries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * 是否存在 ERROR。
     *
     * @return true 表示存在错误
     */
    public boolean hasError() {
        return entries.stream().anyMatch(e -> e.level() == Entry.Level.ERROR);
    }

    @Override
    public String toString() {
        return "Explain{" + "entries=" + entries + '}';
    }
}