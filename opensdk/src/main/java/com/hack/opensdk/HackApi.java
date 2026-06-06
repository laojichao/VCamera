package com.hack.opensdk;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.hack.Slog;
import com.hack.server.core.TransactCallback;
import com.hack.server.core.TransactRegistry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Hack SDK 对外 API 接口类。
 * <p>
 * 提供虚拟空间/多开环境中应用管理的核心 API，包括：
 * <ul>
 *   <li>应用的安装、卸载、启动</li>
 *   <li>包信息查询、Intent 解析</li>
 *   <li>用户空间管理</li>
 *   <li>生命周期回调注册</li>
 *   <li>运行时属性获取</li>
 *   <li>包安装/卸载事件观察</li>
 * </ul>
 * 所有方法均通过 {@link Cmd} 命令机制委托给底层 Hack 引擎执行。
 * </p>
 */
public class HackApi {
    private static final String TAG = HackApi.class.getSimpleName();
    private static final TransactRegistry sTransactRegistry = new TransactRegistry();

    /**
     * 通知引擎 Application 已绑定基础 Context。
     * <p>
     * 在 {@link Application#attachBaseContext(Context)} 中调用，完成引擎端的上下文初始化。
     * </p>
     *
     * @param app  Application 实例
     * @param base 绑定的基础 Context
     */
    public static void attachBaseContext(Application app, Context base) {
        Cmd.INSTANCE().exec(CmdConstants.CMD_APPLICATION_ATTACHBASE, app, base);
    }

    /**
     * 通知引擎 Application 已完成 onCreate。
     */
    public static void onCreate() {
        Cmd.INSTANCE().exec(CmdConstants.CMD_APPLICATION_ONCREATE);
    }

    /**
     * 获取指定包的设置信息。
     *
     * @param pkg    包名
     * @param userId 用户 ID
     * @param flags  查询标志
     * @return 包设置信息的 {@link Bundle}
     */
    public static Bundle getPackageSetting(String pkg, int userId, int flags) {
        return (Bundle) Cmd.INSTANCE().exec(CmdConstants.CMD_GET_PKG_SETTINGS, pkg, userId, flags);
    }

    /**
     * 从宿主系统安装已存在的应用到虚拟空间。
     * <p>
     * 该方法用于安装系统中已存在的应用，而非从 APK 文件安装。
     * </p>
     *
     * @param packageName  要安装的应用包名
     * @param userId       目标用户 ID
     * @param forceInstall 是否强制安装（覆盖已有安装）
     * @return 安装结果码，成功时为 {@code PackageManager.INSTALL_SUCCEEDED (1)}
     */
    public static int installPackageFromHost(String packageName, int userId, boolean forceInstall) {
        return (int) Cmd.INSTANCE().exec(
                CmdConstants.CMD_INSTALL_PACKAGE,
                userId, packageName,
                forceInstall ? CmdConstants.MODE_FORCE_INSTALL : 0
        );
    }

    /**
     * 从 APK 文件安装应用到虚拟空间。
     * <p>
     * 支持单 APK 和 Split APK（多 APK）两种安装模式：
     * <ul>
     *   <li>单 APK：{@code apkPathOrDir} 传入 APK 文件的绝对路径，如 {@code /sdcard/com.xx.yy/com.xx.yy.apk}</li>
     *   <li>Split APK：{@code apkPathOrDir} 传入包含所有 APK 文件的目录路径，如 {@code /sdcard/com.xx.yy/}，
     *       该目录下不能包含不属于当前应用的其他 APK 文件</li>
     * </ul>
     * </p>
     *
     * @param apkPathOrDir  APK 文件路径或包含 Split APK 的目录路径
     * @param userId        目标用户 ID
     * @param forceInstall  是否强制安装
     * @return 安装结果码，成功时为 {@code PackageManager.INSTALL_SUCCEEDED (1)}
     */
    public static int installApkFiles(String apkPathOrDir, int userId, boolean forceInstall) {
        int flags = CmdConstants.MODE_INSTALL_ALONE;
        if (forceInstall) {
            flags |= CmdConstants.MODE_FORCE_INSTALL;
        }
        return (int) Cmd.INSTANCE().exec(
                CmdConstants.CMD_INSTALL_PACKAGE, userId, apkPathOrDir, flags
        );
    }

    /**
     * 卸载指定应用。
     *
     * @param packageName 要卸载的应用包名
     * @param userId      目标用户 ID
     * @return 卸载结果码，成功时为 {@code PackageManager.DELETE_SUCCEEDED (1)}
     */
    public static int uninstallPackage(String packageName, int userId) {
        return (int) Cmd.INSTANCE().exec(
                CmdConstants.CMD_UNINSTALL_PACKAGE,
                userId,
                packageName,
                0
        );
    }

    /**
     * 删除指定应用的数据。
     * <p>
     * 仅清除应用数据，不卸载应用本身。
     * </p>
     *
     * @param packageName 应用包名
     * @param userId      目标用户 ID
     * @return 操作结果码，成功时为 {@code PackageManager.DELETE_SUCCEEDED (1)}
     */
    public static int deletePackageData(String packageName, int userId) {
        return (int) Cmd.INSTANCE().exec(
                CmdConstants.CMD_REMOVE_PKG_DATA,
                userId,
                packageName,
                0
        );
    }

    /**
     * 删除指定应用的缓存数据。
     *
     * @param packageName 应用包名
     * @param userId      目标用户 ID
     * @return 操作结果码，成功时为 {@code PackageManager.DELETE_SUCCEEDED (1)}
     */
    public static int deletePackageCache(String packageName, int userId) {
        return (int) Cmd.INSTANCE().exec(
                CmdConstants.CMD_DELETE_PKG_CACHE,
                userId,
                packageName,
                0
        );
    }

    /**
     * 获取指定应用的包信息。
     *
     * @param packageName 应用包名
     * @param userId      目标用户 ID
     * @param flags       查询标志，控制返回信息的详细程度
     * @return {@link PackageInfo} 包信息对象
     */
    public static PackageInfo getPackageInfo(String packageName, int userId, int flags) {
        return (PackageInfo) Cmd.INSTANCE().exec(
                CmdConstants.CMD_GET_PACKAGE_INFO,
                userId,
                packageName,
                flags
        );
    }

    /**
     * 解析 Intent 对应的目标组件信息。
     *
     * @param intent        要解析的 Intent
     * @param resolvedType  Intent 的 MIME 类型
     * @param flags         解析标志
     * @param userId        目标用户 ID
     * @return 解析结果 {@link ResolveInfo}，无匹配时返回 {@code null}
     */
    public static ResolveInfo resolveIntent(Intent intent, String resolvedType, int flags, int userId) {
        return (ResolveInfo) Cmd.INSTANCE().exec(
                CmdConstants.CMD_RESOLVE_INTENT,
                intent,
                resolvedType,
                flags,
                userId
        );
    }

    /**
     * 查询匹配 Intent 的所有 Activity 信息。
     *
     * @param intent        查询 Intent
     * @param resolvedType  Intent 的 MIME 类型
     * @param flags         查询标志
     * @param userId        目标用户 ID
     * @return 匹配的 {@link ResolveInfo} 列表
     */
    public static List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, int flags, int userId) {
        return (List) Cmd.INSTANCE().exec(
                CmdConstants.CMD_QUERY_ACTIVITIES,
                intent,
                resolvedType,
                flags,
                userId
        );
    }

    /**
     * 获取指定组件的 Activity 信息。
     *
     * @param component 组件名
     * @param flags     查询标志
     * @param userId    目标用户 ID
     * @return {@link ActivityInfo} 对象
     */
    public static ActivityInfo getActivityInfo(ComponentName component, int flags, int userId) {
        return (ActivityInfo) Cmd.INSTANCE().exec(
                CmdConstants.CMD_GET_ACTIVITY_INFO,
                component,
                flags,
                userId
        );
    }

    /**
     * 获取已安装应用的包名列表。
     *
     * @param flags  查询标志
     * @param userId 目标用户 ID
     * @return 已安装应用的包名列表
     */
    public static List<String> getInstalledPackages(int flags, int userId) {
        return (List<String>) Cmd.INSTANCE().exec(
                CmdConstants.CMD_GET_INSTALLED_PKGS,
                flags,
                userId
        );
    }

    /**
     * 获取不可用的应用包信息。
     * <p>
     * 返回当前虚拟空间中不可用（如被冻结、被限制等）的包及其关联的用户 ID 列表。
     * </p>
     *
     * @param flags 查询标志
     * @return 包名到用户 ID 列表的映射
     */
    public static Map<String, List<Integer>> getUnavailablePackages(int flags) {
        return (Map<String, List<Integer>>) Cmd.INSTANCE().exec(
                CmdConstants.CMD_GET_UNAVAILABLE_PKGS,
                flags, 0);
    }

    /**
     * 注册事务回调。
     * <p>
     * 当指定命令被引擎执行时，通过回调通知调用方。
     * </p>
     *
     * @param cmd      命令类型常量
     * @param callback 事务回调对象
     */
    public static void registerTransactCallback(int cmd, TransactCallback callback) {
        sTransactRegistry.registerTransactCallback(cmd, callback);
    }

    /**
     * 注销事务回调。
     *
     * @param cmd 要注销回调的命令类型常量
     */
    public static void unregisterTransactCallback(int cmd) {
        sTransactRegistry.unregisterTransactCallback(cmd);
    }

    /**
     * 启动指定应用（已废弃）。
     *
     * @param packageName 要启动的应用包名
     * @param userId      目标用户 ID
     * @return 启动是否成功
     * @deprecated 请使用 {@link #startActivity(Intent, int)} 替代
     */
    @Deprecated
    public static boolean startPackage(String packageName, int userId) {
        return (boolean) Cmd.INSTANCE().exec(
                CmdConstants.CMD_START_PACKAGE,
                packageName,
                userId,
                0
        );
    }

    /**
     * 启动指定的 Activity。
     * <p>
     * 返回值含义：
     * <ul>
     *   <li>{@code 0} - START_SUCCESS</li>
     *   <li>{@code 1} - START_RETURN_INTENT_TO_CALLER</li>
     *   <li>{@code 2} - START_TASK_TO_FRONT</li>
     *   <li>{@code 3} - START_DELIVERED_TO_TOP</li>
     *   <li>其他值表示失败或异常</li>
     * </ul>
     * </p>
     *
     * @param intent 要启动的 Activity Intent
     * @param userId 目标用户 ID
     * @return 启动结果码
     */
    public static int startActivity(Intent intent, int userId) {
        return (int) Cmd.INSTANCE().exec(
                CmdConstants.CMD_START_ACTIVITY,
                intent,
                null,
                userId
        );
    }

    /**
     * 强制终止指定应用进程。
     *
     * @param userId  目标用户 ID
     * @param pkg     要终止的应用包名
     * @param reason  终止原因描述（不可为 null）
     */
    public static void killApplication(int userId, String pkg, String reason/*NonNull*/) {
        Cmd.INSTANCE().exec(CmdConstants.CMD_KILL_PACKAGE, userId, pkg, reason);
    }

    /**
     * 检查指定应用是否有正在运行的 Activity。
     *
     * @param userId 目标用户 ID
     * @param pkg    应用包名
     * @return 如果应用有可见的 Activity 则返回 {@code true}
     */
    public static boolean hasAnyRunningActivity(int userId, String pkg) {
        return (boolean) Cmd.INSTANCE().exec(CmdConstants.CMD_PACKAGE_MAYBE_VISIBLE, userId, pkg);
    }

    /**
     * 注册应用卸载事件观察者。
     * <p>
     * callback 对象必须包含以下方法（引擎通过反射调用）：
     * <pre>
     * public void onPackageDeleted(String packageName, int returnCode, String msg, int userId);
     * </pre>
     * </p>
     *
     * @param callback 卸载事件回调对象
     * @return 观察者标识，用于后续通过 {@link #unregisterDeleteObserver(Object)} 注销
     */
    public static Object registerDeleteObserver(Object callback) {
        return (int) Cmd.INSTANCE().exec(
                CmdConstants.CMD_REGISTER_UNINSTALL_OBSERVER,
                callback
        );
    }

    /**
     * 注销应用卸载事件观察者。
     *
     * @param observer 由 {@link #registerDeleteObserver(Object)} 返回的观察者标识
     */
    public static void unregisterDeleteObserver(Object observer) {
        Cmd.INSTANCE().exec(
                CmdConstants.CMD_UNREGISTER_UNINSTALL_OBSERVER,
                observer
        );
    }

    /**
     * 注册应用安装事件观察者。
     * <p>
     * callback 对象必须包含以下方法（引擎通过反射调用）：
     * <pre>
     * public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras, int userId);
     * </pre>
     * </p>
     *
     * @param callback 安装事件回调对象
     * @return 观察者标识，用于后续通过 {@link #unregisterInstallObserver(Object)} 注销
     */
    public static Object registerInstallObserver(Object callback) {
        return (int) Cmd.INSTANCE().exec(
                CmdConstants.CMD_REGISTER_INSTALL_OBSERVER,
                callback
        );
    }

    /**
     * 注销应用安装事件观察者。
     *
     * @param observer 由 {@link #registerInstallObserver(Object)} 返回的观察者标识
     */
    public static void unregisterInstallObserver(Object observer) {
        Cmd.INSTANCE().exec(
                CmdConstants.CMD_UNREGISTER_INSTALL_OBSERVER,
                observer
        );
    }

    /**
     * 获取所有可用的用户空间 ID。
     *
     * @return 可用用户 ID 数组
     */
    public static int[] getAvailableUserSpace() {
        return (int[]) Cmd.INSTANCE().exec(
                CmdConstants.CMD_GET_ALL_USERID
        );
    }

    /**
     * 获取指定应用已安装的用户空间 ID 列表。
     *
     * @param packageName 应用包名
     * @return 该应用已安装的用户 ID 数组
     */
    public static int[] getInstallUsersForPackage(String packageName) {
        return (int[]) Cmd.INSTANCE().exec(CmdConstants.CMD_PKG_ALL_USERID, packageName);
    }

    /**
     * 获取一个可用的（未安装该应用的）用户空间 ID。
     * <p>
     * 遍历从 0 开始的用户 ID，找到第一个未安装指定应用的用户 ID 并返回。
     * </p>
     *
     * @param packageName 应用包名
     * @return 可用的用户 ID
     */
    public static int getAvailableUser(String packageName) {
        int userId = 0;
        long time = System.currentTimeMillis();
        int[] users = getInstallUsersForPackage(packageName);
        if (users != null) {
            // 遍历从 0 开始的用户 ID，找到第一个未被占用的 ID
            for (int i = 0; i < Integer.MAX_VALUE; i++) {
                boolean found = false;
                for (int user : users) {
                    if (user == i) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    userId = i;
                    break;
                }
            }
        }
        Slog.v(TAG, "getUsers consume: " + (System.currentTimeMillis() - time) / 1000);
        return userId;
    }

    /**
     * 获取全局事务注册表。
     *
     * @return {@link TransactRegistry} 实例
     */
    public static TransactRegistry getTransactRegistry() {
        return sTransactRegistry;
    }

    /**
     * 获取指定应用的启动 Intent。
     * <p>
     * 按以下优先级查找：
     * <ol>
     *   <li>首先查找具有 {@code CATEGORY_INFO} 的 Activity</li>
     *   <li>若未找到，再查找具有 {@code CATEGORY_LAUNCHER} 的主 Activity</li>
     *   <li>两者均未找到时返回 {@code null}</li>
     * </ol>
     * </p>
     *
     * @param packageName 目标应用包名
     * @param uerId       目标用户 ID
     * @return 启动 Intent，无匹配 Activity 时返回 {@code null}
     */
    public static Intent getLaunchIntentForPackage(String packageName, int uerId) {
        // 首先查找具有 INFO category 的 Activity，这类 Activity 通常是应用的首选入口
        Intent intentToResolve = new Intent(Intent.ACTION_MAIN);
        intentToResolve.addCategory(Intent.CATEGORY_INFO);
        intentToResolve.setPackage(packageName);
        List<ResolveInfo> ris = queryIntentActivities(intentToResolve, null, 0, uerId);

        // 若未找到，则查找具有 LAUNCHER category 的主 Activity
        if (ris == null || ris.size() <= 0) {
            // reuse the intent instance
            intentToResolve.removeCategory(Intent.CATEGORY_INFO);
            intentToResolve.addCategory(Intent.CATEGORY_LAUNCHER);
            intentToResolve.setPackage(packageName);
            ris = queryIntentActivities(intentToResolve, null, 0, uerId);
        }
        if (ris == null || ris.size() <= 0) {
            return null;
        }
        Intent intent = new Intent(intentToResolve);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(ris.get(0).activityInfo.packageName, ris.get(0).activityInfo.name);
        return intent;
    }

    /**
     * 获取 Hack 引擎的运行时属性。
     *
     * @return 属性键值对映射
     */
    public static Map<String, Object> getRuntimeProperties() {
        Map<String,Object> map =  (Map<String, Object>) Cmd.INSTANCE().exec(CmdConstants.CMD_GET_RUNTIME_PROPERTIES);
        return map;
    }

    /**
     * 获取指定的运行时属性值。
     * <p>
     * 若属性不存在或获取失败，返回默认值。
     * </p>
     *
     * @param <T>          属性值类型
     * @param key          属性键名
     * @param defaultValue 属性不存在时的默认值
     * @return 属性值，不存在时返回 {@code defaultValue}
     */
    public static <T> T getRuntimeProperty(String key, T defaultValue) {
        try {
            Object o = getRuntimeProperties().get(key);
            if (o == null) {
                return defaultValue;
            }
            return (T) o;
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    /**
     * 注册 Application 生命周期回调。
     * <p>
     * 通过动态代理机制将 {@link ApplicationCallback} 的各生命周期方法
     * 桥接为命令分发，由引擎在对应时机调用。
     * </p>
     *
     * @param callback 应用生命周期回调对象
     */
    public static void registerApplicationCallback(ApplicationCallback callback){
        Cmd.INSTANCE().exec(CmdConstants.CMD_REGISTER_APPLICATION_CALLBACK, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                int cmd = (int) args[0];
                switch (cmd) {
                    case 0: {
                        callback.onInitAppContext(args[1], (Context) args[2]);
                        break;
                    }
                    case 1: {
                        callback.onAttachBaseContext((Application) args[1]);
                        break;
                    }
                    case 2: {
                        callback.onInstallProviders((Application) args[1]);
                        break;
                    }
                    case 3: {
                        callback.onCreate((Application) args[1]);
                        break;
                    }

                }
                return null;
            }
        });
    }

    /**
     * Application 生命周期回调接口。
     * <p>
     * 定义了 Application 从初始化到创建完成的四个关键生命周期节点，
     * 供上层模块监听并执行自定义逻辑。
     * </p>
     */
    public interface ApplicationCallback {

        /**
         * 应用上下文初始化时回调。
         *
         * @param loadedApk  加载的 APK 对象
         * @param appContext 应用上下文
         */
        void onInitAppContext(Object loadedApk, Context appContext);

        /**
         * Application 绑定基础 Context 时回调。
         *
         * @param app Application 实例
         */
        void onAttachBaseContext(Application app);

        /**
         * ContentProvider 安装完成时回调。
         *
         * @param app Application 实例
         */
        void onInstallProviders(Application app);

        /**
         * Application 创建完成时回调。
         *
         * @param app Application 实例
         */
        void onCreate(Application app);

    }
}
