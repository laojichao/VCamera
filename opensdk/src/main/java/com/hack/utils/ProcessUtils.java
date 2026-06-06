package com.hack.utils;

import static com.hack.Features.DEBUG;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * 进程信息查询工具类。
 * <p>
 * 提供当前进程名称获取、进程类型判断、CPU 架构（32/64 位）检测等功能。
 * 通过进程名称后缀区分不同的功能进程类型：
 * </p>
 * <ul>
 *   <li>{@link ProcessType#TYPE_CLIENT} - 宿主主进程或客户端进程（后缀 ":client"）</li>
 *   <li>{@link ProcessType#TYPE_SERVICE} - 核心服务进程（后缀 ":core"）</li>
 *   <li>{@link ProcessType#TYPE_ASSIST} - 辅助进程（后缀 ":assist"）</li>
 *   <li>{@link ProcessType#TYPE_APP} - 代理应用进程（其他后缀）</li>
 * </ul>
 */
public class ProcessUtils {
    private static boolean sbInit;
    private static boolean sIs64bit;
    private static String sCurProcessName;
    private static ProcessType sProcessType = ProcessType.TYPE_UNKNOWN;
    /**
     * 进程类型枚举。
     * <p>用于标识当前进程在 SDK 架构中扮演的角色。</p>
     */
    public enum ProcessType {
        /** 未知类型，尚未初始化 */
        TYPE_UNKNOWN,
        /** 客户端主进程 */
        TYPE_CLIENT,
        /** 代理应用进程 */
        TYPE_APP,
        /** 辅助进程 */
        TYPE_ASSIST,
        /** 核心服务进程 */
        TYPE_SERVICE
    }

    /**
     * 尝试获取当前进程的类型。
     * <p>首次调用时根据进程名称后缀判断类型并缓存结果，后续调用直接返回缓存值。
     * 判断逻辑基于 {@link #getCurProcessName(Context)} 返回的进程名称与
     * 宿主包名及其后缀的匹配关系。</p>
     *
     * @param context 应用上下文，用于获取包名和进程信息
     * @return 当前进程的 {@link ProcessType} 类型
     */
    public static ProcessType tryGetProcessType(Context context) {
        if (sProcessType == ProcessType.TYPE_UNKNOWN) {
            final String hostPkg = context.getPackageName();
            String processName = ProcessUtils.getCurProcessName(context);
            if (TextUtils.equals(processName, hostPkg)
                    || TextUtils.equals(processName, hostPkg + ":client")) {
                sProcessType = ProcessType.TYPE_CLIENT;
            } else if (TextUtils.equals(processName, hostPkg + ":core")) {
                sProcessType = ProcessType.TYPE_SERVICE;
            } else if (TextUtils.equals(processName, hostPkg + ":assist")) {
                sProcessType = ProcessType.TYPE_ASSIST;
            } else {
                sProcessType = ProcessType.TYPE_APP;
            }
        }

        return sProcessType;
    }

    /**
     * 判断当前进程是否为核心服务进程。
     *
     * @return {@code true} 表示当前进程为 {@link ProcessType#TYPE_SERVICE} 类型
     */
    public static boolean isService() {
        return sProcessType == ProcessType.TYPE_SERVICE;
    }

    /**
     * 判断当前进程是否为客户端主进程。
     *
     * @return {@code true} 表示当前进程为 {@link ProcessType#TYPE_CLIENT} 类型
     */
    public static boolean isClient() {
        return sProcessType == ProcessType.TYPE_CLIENT;
    }

    /**
     * 判断当前进程是否为辅助进程。
     *
     * @return {@code true} 表示当前进程为 {@link ProcessType#TYPE_ASSIST} 类型
     */
    public static boolean isAssist() {
        return sProcessType == ProcessType.TYPE_ASSIST;
    }

    /**
     * 判断当前进程是否为代理应用进程。
     *
     * @return {@code true} 表示当前进程为 {@link ProcessType#TYPE_APP} 类型
     */
    public static boolean isApp() {
        return sProcessType == ProcessType.TYPE_APP;
    }

    /**
     * 获取当前进程的名称。
     * <p>优先通过 {@link ActivityManager} 查询运行中的进程列表匹配 PID，
     * 若失败则回退到读取 {@code /proc/[pid]/cmdline} 文件获取。</p>
     *
     * @param context 应用上下文，用于获取 ActivityManager 服务
     * @return 当前进程名称字符串，若获取失败则返回 {@code null}
     */
    public static String getCurProcessName(Context context) {
        String procName = sCurProcessName;
        if (!TextUtils.isEmpty(procName)) {
            return procName;
        }
        try {
            int pid = Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo info : mActivityManager.getRunningAppProcesses()) {
                if (info.pid == pid) {
                    sCurProcessName = info.processName;
                    return sCurProcessName;
                }
            }
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
        sCurProcessName = getProcessNameFromProc(-1);
        return sCurProcessName;
    }

    /**
     * 通过读取 {@code /proc/[pid]/cmdline} 文件获取进程名称。
     * <p>当 {@code pid} 为 -1 时，使用当前进程的 PID。</p>
     *
     * @param pid 目标进程的 PID，传入 -1 表示当前进程
     * @return 进程名称字符串，若读取失败则返回 {@code null}
     */
    public static String getProcessNameFromProc(int pid) {
        BufferedReader cmdlineReader = null;
        if (pid == -1) {
            pid = Process.myPid();
        }
        try {
            cmdlineReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(
                            "/proc/" + pid + "/cmdline"),
                    "iso-8859-1"));
            int c;
            StringBuilder processName = new StringBuilder();
            while ((c = cmdlineReader.read()) > 0) {
                processName.append((char) c);
            }
            return processName.toString();
        } catch (Throwable e) {
            // ignore
        } finally {
            if (cmdlineReader != null) {
                try {
                    cmdlineReader.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return null;
    }

    /**
     * 判断当前进程是否运行在 64 位模式下。
     * <p>检测策略按 Android 版本分级：</p>
     * <ul>
     *   <li>Android Lollipop 以下：始终返回 {@code false}</li>
     *   <li>Android Marshmallow 及以上：直接调用 {@link Process#is64Bit()}</li>
     *   <li>Android Lollipop：通过反射调用 {@code dalvik.system.VMRuntime.is64Bit()}</li>
     * </ul>
     * <p>结果会被缓存，后续调用直接返回缓存值。</p>
     *
     * @return {@code true} 表示当前进程为 64 位模式
     */
    public static boolean is64Bit() {
        if (sbInit) {
            return sIs64bit;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            sIs64bit = false;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            sIs64bit = Process.is64Bit();
        } else {
            final String VMRuntime_className = "dalvik.system.VMRuntime";
            RefUtils.MethodRef method_getRuntime = new RefUtils.MethodRef(
                    VMRuntime_className, true, "getRuntime", new Class[0]);
            RefUtils.MethodRef method_is64Bit = new RefUtils.MethodRef(
                    VMRuntime_className, false, "is64Bit", new Class[0]);

            Object invokeResult = method_is64Bit.invoke(method_getRuntime.invoke(null));
            if (invokeResult != null) {
                sIs64bit = (Boolean) invokeResult;
            }
        }
        sbInit = true;
        return sIs64bit;
    }

    /**
     * 将进程类型转换为可读的字符串表示。
     * <p>格式为 {@code 枚举值#类型名称}，例如 {@code TYPE_APP#TYPE_APP}。</p>
     *
     * @param type 进程类型枚举值
     * @return 格式化后的字符串，若类型未识别则返回 {@code null}
     */
    public static String typeToString(ProcessType type) {
        switch (type) {
            case TYPE_APP:
                return type + "#" + "TYPE_APP";
            case TYPE_CLIENT:
                return type + "#" + "TYPE_CLIENT";
            case TYPE_SERVICE:
                return type + "#" + "TYPE_SERVICE";
            case TYPE_ASSIST:
                return type + "#" + "TYPE_ASSIST";
            default:
                return null;
        }
    }
}
