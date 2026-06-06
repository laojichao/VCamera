package com.hack.server.core;

import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;

/**
 * 跨进程事务回调注册表。
 * <p>
 * 管理所有已注册的 {@link TransactCallback} 处理器，通过命令标识（cmd）作为键
 * 进行存储和检索。当收到事务请求时，根据命令类型查找并分发到对应的回调处理器。
 * 内部使用 {@link SparseArray} 结合同步锁保证线程安全。
 * </p>
 */
public class TransactRegistry implements TransactCallback {
    /** 存储命令标识到回调处理器的映射，操作时通过 synchronized 保证线程安全 */
    private final SparseArray<TransactCallback> transactCallbacks = new SparseArray<>();

    /**
     * 构造一个新的事务注册表实例。
     */
    public TransactRegistry() {
    }

    /**
     * 注册事务回调处理器。
     * <p>以命令标识为键将回调处理器存入注册表，若已存在相同命令标识的处理器则覆盖。</p>
     *
     * @param cmd      事务命令标识
     * @param callback 对应的事务回调处理器
     */
    public final void registerTransactCallback(int cmd, TransactCallback callback) {
        synchronized (transactCallbacks) {
            transactCallbacks.put(cmd, callback);
        }
    }

    /**
     * 取消注册指定命令标识的事务回调处理器。
     *
     * @param cmd 要移除的事务命令标识
     */
    public final void unregisterTransactCallback(int cmd) {
        synchronized (transactCallbacks) {
            transactCallbacks.remove(cmd);
        }

    }

    /**
     * 处理跨进程事务请求。
     * <p>根据命令标识查找已注册的回调处理器，若找到则委托其处理事务并返回结果。</p>
     *
     * @param context 当前应用上下文
     * @param cmd     事务命令标识
     * @param extra   事务附加参数
     * @return 处理结果的 {@link Bundle}，若未找到匹配的回调处理器则返回 {@code null}
     */
    @Override
    public Bundle transact(Context context, int cmd, Bundle extra) {
        TransactCallback callback;
        synchronized (transactCallbacks) {
            callback = transactCallbacks.get(cmd);
        }
        if (callback != null) {
            return callback.transact(context, cmd, extra);
        }
        return null;
    }
}
