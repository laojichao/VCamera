package com.hack.server.core.transact;

import static com.hack.opensdk.CmdConstants.TRANSACT_KEY_FROM_TOKEN;
import static com.hack.opensdk.CmdConstants.TRANSACT_KEY_INTENT;
import static com.hack.opensdk.CmdConstants.TRANSACT_KEY_PKG;
import static com.hack.opensdk.CmdConstants.TRANSACT_KEY_RESULT;
import static com.hack.opensdk.CmdConstants.TRANSACT_KEY_SHELL_PKG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.hack.server.core.TransactCallback;
import com.hack.utils.IntentUtils;
import com.hack.utils.ThreadUtils;

/**
 * 外部 Activity 启动监控处理器。
 * <p>
 * 实现 {@link TransactCallback} 接口，处理来自外部进程的 Activity 启动请求。
 * 当目标 Intent 未找到对应的 Activity 时，该处理器会拦截请求并尝试在当前进程中
 * 启动目标 Activity，主要处理系统 Launcher Home 类型的 Intent。
 * </p>
 * <p>
 * 事务请求中需包含以下参数：
 * <ul>
 *   <li>{@link CmdConstants#TRANSACT_KEY_PKG} - 调用方包名</li>
 *   <li>{@link CmdConstants#TRANSACT_KEY_SHELL_PKG} - 调用方 Shell 包名</li>
 *   <li>{@link CmdConstants#TRANSACT_KEY_INTENT} - 目标 Intent</li>
 *   <li>{@link CmdConstants#TRANSACT_KEY_FROM_TOKEN} - 调用方 Activity Token</li>
 * </ul>
 * </p>
 */
public class OuterActivityMonitor implements TransactCallback {
    /**
     * 处理外部 Activity 启动事务请求。
     * <p>从 extras 中提取调用方信息和目标 Intent，记录调试日志后尝试启动 Activity。</p>
     *
     * @param context 当前应用上下文
     * @param cmd     事务命令标识
     * @param extras  事务附加参数，包含调用方包名、Intent 等信息
     * @return 若成功启动 Activity 则返回包含 {@link CmdConstants#TRANSACT_KEY_RESULT} 的 Bundle，
     *         否则返回 {@code null}
     */
    @Override
    public Bundle transact(Context context, int cmd, Bundle extras) {
        String callerPkg = extras.getString(TRANSACT_KEY_PKG);
        String callerShell = extras.getString(TRANSACT_KEY_SHELL_PKG);
        Intent intent = extras.getParcelable(TRANSACT_KEY_INTENT);
        IBinder callerActivityToken = extras.getBinder(TRANSACT_KEY_FROM_TOKEN);

        Log.d("TransactProvider", String.format("target intent not found! " +
                        "[caller: %s-%s] [fromToken: %s] [intent: %s]",
                callerPkg, callerShell, callerActivityToken, intent));


        Integer ret = startActivity(context, intent);
        if (ret != null) {
            Bundle bundle = new Bundle();
            bundle.putInt(TRANSACT_KEY_RESULT, ret);
            return bundle;
        }
        return null;
    }

    /**
     * 启动目标 Activity。
     * <p>仅处理系统 Launcher Home 类型的 Intent，在主线程中执行启动操作。
     * 因当前进程可能不在前台，启动可能失败，需要通过 Shell 包来启动目标 Activity。</p>
     *
     * @param context 当前应用上下文
     * @param intent  待启动的 Activity Intent
     * @return 启动成功返回 0，不支持的 Intent 返回 {@code null}
     */
    public Integer startActivity(Context context, Intent intent) {
        if (IntentUtils.isSysLauncherHome(intent)) {
            ThreadUtils.postOnMainThread(() -> {
                //当前进程可能不在前台，startActivity可能失败，应该使用callerShell包去启动目标activity
                //所以辅包也需要注册TransactProvider
                context.startActivity(intent);
            });
            return 0;
        }
        return null;
    }
}
