package com.hack.opensdk;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.hack.Features;

/**
 * Hack Application 基类。
 * <p>
 * 继承自 {@link Application}，作为被 Hook 应用的 Application 入口。
 * 在 {@link #attachBaseContext(Context)} 中完成 Hack 引擎的安装和初始化，
 * 在 {@link #onCreate()} 中通知引擎完成 Application 创建。
 * </p>
 * <p>
 * 初始化流程：
 * <ol>
 *   <li>检测当前进程是否为主进程，若为主进程则保存辅助包名到 SharedPreferences</li>
 *   <li>若非主进程，尝试创建主进程的 Context 以共享数据</li>
 *   <li>调用 {@link HackRuntime#install(Context, String, boolean)} 加载引擎 JAR</li>
 *   <li>通过 {@link Cmd} 将 Application 和 Context 传递给引擎</li>
 * </ol>
 * </p>
 */
public class HackApplication extends Application {

    private static final boolean DEBUG = Features.DEBUG;
    private static final String TAG = HackApplication.class.getSimpleName();

    /**
     * 绑定基础上下文。
     * <p>
     * 完成以下核心初始化操作：
     * <ul>
     *   <li>判断是否为主进程（主包名），若是则将辅助包名写入 SharedPreferences</li>
     *   <li>若非主进程，尝试通过 {@link Context#createPackageContext} 获取主进程 Context</li>
     *   <li>调用 {@link HackRuntime#install} 加载引擎 DEX 及 native 库</li>
     *   <li>通过 {@link Cmd} 将 attachBaseContext 事件通知给引擎</li>
     * </ul>
     * </p>
     *
     * @param base 原始基础上下文
     */
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (DEBUG) Log.d(TAG, "attachBaseContext start");
        Context engineContext = base;
        // 判断是否为主进程（宿主包名），若是则保存辅助包名到配置
        if (TextUtils.equals(base.getPackageName(), BuildConfig.MASTER_PACKAGE)) {
            engineContext.getSharedPreferences("hack", Context.MODE_PRIVATE).edit().putString("sp.assist.pkg", BuildConfig.ASSIST_PACKAGE).commit();
        } else {
            try {
                // 非主进程时，检查主包是否与当前进程同 UID，若是则创建主包的 Context
                if (engineContext.getPackageManager().getPackageInfo(BuildConfig.MASTER_PACKAGE, 0).applicationInfo.uid == Process.myUid()) {
                    engineContext = base.createPackageContext(BuildConfig.MASTER_PACKAGE, 0);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                Log.e(TAG, "master package not install ");
            }
        }
        // 安装 Hack 引擎并通知引擎 Application 已绑定
        HackRuntime.install(engineContext, "version", true);
        Cmd.INSTANCE().exec(CmdConstants.CMD_APPLICATION_ATTACHBASE, this, base);
        if (DEBUG) Log.d(TAG, "attachBaseContext end");
    }

    /**
     * Application 创建完成时调用。
     * <p>
     * 通知底层引擎 Application 的 {@code onCreate} 已执行，由引擎完成后续初始化。
     * </p>
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(TAG, "onCreate start");
        Cmd.INSTANCE().exec(CmdConstants.CMD_APPLICATION_ONCREATE);
        if (DEBUG) Log.d(TAG, "onCreate end");
    }
}
