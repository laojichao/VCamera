package com.hack.server.core;

import android.content.Context;
import android.os.Bundle;

/**
 * 跨进程事务回调接口。
 * <p>
 * 定义了跨进程通信（IPC）中事务处理的标准回调契约。
 * 实现此接口的类可用于注册到 {@link TransactRegistry}，
 * 以便通过 {@link TransactProvider} 接收并处理来自客户端的事务请求。
 * </p>
 */
public interface TransactCallback  {
    /**
     * 处理跨进程事务请求。
     *
     * @param context 当前应用上下文
     * @param cmd     事务命令标识，用于区分不同的事务类型
     * @param extras  事务携带的附加参数键值对
     * @return 处理结果的 {@link Bundle}，若无需返回结果则返回 {@code null}
     */
    Bundle transact(Context context,int cmd, Bundle extras);
}
