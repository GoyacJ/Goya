package com.ysmjjsy.goya.component.mybatisplus.permission.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.DataPermissionHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import lombok.EqualsAndHashCode;

/**
 * <p>数据权限拦截器</p>
 *
 * <p>默认仅对查询生效，写入权限由业务侧控制。</p>
 *
 * @author goya
 * @since 2026/1/29
 */
@EqualsAndHashCode(callSuper = true)
public class GoyaDataPermissionInterceptor extends DataPermissionInterceptor {

    private final boolean applyToWrite;

    /**
     * 构造方法。
     *
     * @param handler 数据权限处理器
     * @param applyToWrite 是否应用到写操作
     */
    public GoyaDataPermissionInterceptor(DataPermissionHandler handler, boolean applyToWrite) {
        super(handler);
        this.applyToWrite = applyToWrite;
    }

    /**
     * 更新语句处理。
     *
     * @param update 更新语句
     * @param index 索引
     * @param sql 原始 SQL
     * @param obj 参数
     */
    @Override
    protected void processUpdate(net.sf.jsqlparser.statement.update.Update update, int index, String sql, Object obj) {
        if (applyToWrite) {
            super.processUpdate(update, index, sql, obj);
        }
    }

    /**
     * 删除语句处理。
     *
     * @param delete 删除语句
     * @param index 索引
     * @param sql 原始 SQL
     * @param obj 参数
     */
    @Override
    protected void processDelete(net.sf.jsqlparser.statement.delete.Delete delete, int index, String sql, Object obj) {
        if (applyToWrite) {
            super.processDelete(delete, index, sql, obj);
        }
    }
}
