package com.hack.agent;

import android.app.Application;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.os.Bundle;

import com.hack.Features;
import com.hack.opensdk.Cmd;
import com.hack.opensdk.HackRuntime;
import com.hack.server.core.transact.TransactActivityLifecycle;

/**
 * Provider 抽象基类。
 * <p>
 * 继承自 {@link AppAgentFileProvider}，为所有代理 ContentProvider 提供统一的初始化和调用逻辑。
 * 在 {@link #attachInfo(Context, ProviderInfo)} 中完成 Activity 生命周期回调注册和 Provider 信息绑定，
 * 在 {@link #call(String, String, Bundle)} 中将调用委托给底层引擎。
 * </p>
 * <p>
 * 子类必须实现 {@link #getProviderCallType()} 以指定该 Provider 对应的命令调用类型。
 * </p>
 */
public abstract class ProviderBase extends AppAgentFileProvider {
    private static final boolean DEBUG = Features.DEBUG;
    private static final String TAG = ProviderBase.class.getSimpleName();

    /**
     * 绑定 Provider 信息。
     * <p>
     * 在 ContentProvider 初始化时调用，完成以下操作：
     * <ol>
     *   <li>注册全局 Activity 生命周期回调 {@link TransactActivityLifecycle}</li>
     *   <li>将 ProviderInfo 传递给 {@link HackRuntime} 进行绑定</li>
     *   <li>调用父类的 attachInfo 完成标准初始化</li>
     * </ol>
     * </p>
     *
     * @param context Provider 所属的上下文
     * @param info    Provider 的注册信息
     */
    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        Application application = (Application) context.getApplicationContext();
        application.registerActivityLifecycleCallbacks(TransactActivityLifecycle.INSTANCE);
        HackRuntime.attachProviderInfo(info);
        super.attachInfo(context, info);
    }

    /**
     * ContentProvider 创建时调用。
     *
     * @return 始终返回 {@code true}，表示初始化成功
     */
    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * 处理 ContentProvider 的 call 请求。
     * <p>
     * 将调用参数委托给底层 Hack 引擎，由引擎根据 {@link #getProviderCallType()}
     * 返回的命令类型执行实际逻辑。
     * </p>
     *
     * @param method 调用的方法名
     * @param arg    方法参数
     * @param extras 附加参数 Bundle
     * @return 引擎执行结果
     */
    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        return (Bundle) Cmd.INSTANCE().exec(getProviderCallType(), method, arg, extras);
    }

    /**
     * 获取该 Provider 对应的命令调用类型。
     * <p>
     * 子类必须实现此方法，返回用于 {@link Cmd#exec(int, Object...)} 的命令常量。
     * </p>
     *
     * @return 命令调用类型常量
     */
    public abstract int getProviderCallType();
}
