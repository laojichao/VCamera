package com.hack.opensdk;

import android.content.Context;
import android.content.pm.ProviderInfo;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import com.hack.Slog;
import com.hack.utils.FileUtils;
import com.hack.utils.ProcessUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

/**
 * Hack 引擎运行时管理类。
 * <p>
 * 负责 Hack 引擎（Engine JAR）的加载、安装和类加载器管理。
 * 引擎以 DEX 文件形式从 assets 中提取，通过 {@link DexClassLoader} 动态加载，
 * 支持增量更新和双版本切换机制。
 * </p>
 * <p>
 * 核心职责：
 * <ul>
 *   <li>管理引擎 DEX 的解压和版本切换（双缓冲机制：version-1 和 version-2）</li>
 *   <li>根据设备架构（32/64 位）加载对应的 native 库目录</li>
 *   <li>维护全局 {@link DexClassLoader} 实例，供其他模块反射调用引擎类</li>
 *   <li>记录当前进程的 {@link ProviderInfo}，用于判断是否为 Hack 进程</li>
 * </ul>
 * </p>
 */
public class HackRuntime {
    /** 引擎文件存放目录名 */
    private static final String ENGINE_JAR_DIR = ".plugin";
    /** 引擎 JAR 文件名 */
    private static final String ENGINE_JAR_NAME = BuildConfig.ENGINE_JAR_NAME;
    /** 当前进程关联的 ProviderInfo */
    private static ProviderInfo providerInfo;
    /** Hack 引擎的类加载器 */
    private static DexClassLoader hackClassLoader;

    /**
     * 绑定 ProviderInfo 信息。
     * <p>
     * 在 Provider 初始化时调用，用于标记当前进程为 Hack 进程。
     * </p>
     *
     * @param info 当前进程的 ProviderInfo
     */
    public static void attachProviderInfo(ProviderInfo info) {
        HackRuntime.providerInfo = info;
    }

    /**
     * 判断当前进程是否为 Hack 进程。
     *
     * @return 如果已绑定 ProviderInfo 则返回 {@code true}，表示当前为 Hack 进程
     */
    public static boolean isHackProcess() {
        return HackRuntime.providerInfo != null;
    }

    /**
     * 获取当前进程的 ProviderInfo。
     *
     * @return 当前绑定的 {@link ProviderInfo}，未绑定时返回 {@code null}
     */
    public static ProviderInfo getHackProvider() {
        return providerInfo;
    }

    /**
     * 安装并加载 Hack 引擎。
     * <p>
     * 引擎加载策略：
     * <ol>
     *   <li>检查类加载器是否已初始化，若已初始化则跳过</li>
     *   <li>读取引擎配置文件（JSON），获取当前版本路径</li>
     *   <li>若需要检查更新，比较 APK 安装时间与配置记录时间：
     *     <ul>
     *       <li>时间不一致时，使用双缓冲机制切换到另一个版本目录（-1 或 -2）</li>
     *       <li>从 assets 中提取引擎 DEX 及 native 库</li>
     *       <li>更新配置文件记录</li>
     *     </ul>
     *   </li>
     *   <li>使用 {@link #loadEngine(File)} 创建 {@link DexClassLoader} 加载引擎</li>
     * </ol>
     * </p>
     *
     * @param app   应用上下文，用于访问文件系统和 assets
     * @param name  引擎版本名称，用于配置文件和目录命名
     * @param check 是否强制检查并更新引擎（{@code true} 时总是检查版本）
     */
    public static void install(Context app, String name, boolean check) {
        if (hackClassLoader != null) {
            return;
        }
        File root = new File(app.getFilesDir(), ENGINE_JAR_DIR);
        File config = new File(root, name + ".json");
        JSONObject object = readJson(config);
        File workspace = null;
        String workPath = object.optString("current");
        if (!TextUtils.isEmpty(workPath)) {
            workspace = new File(workPath);
        }
        if (check || TextUtils.isEmpty(workPath)) {
            if (!check) {
                Slog.e(HackRuntime.class.getName(), "engine load fail");
            }
            // 比较 APK 安装时间与配置记录时间，判断是否需要更新引擎
            long time = object.optLong("time");
            long installTime = new File(app.getPackageCodePath()).lastModified();
            if (time != installTime) {
                // 双缓冲机制：在 version-1 和 version-2 之间交替切换
                if (workspace == null) {
                    workspace = new File(root, name + "-1");
                } else {
                    if (workPath.endsWith("-1")) {
                        workspace = new File(root, name + "-2");
                    } else {
                        workspace = new File(root, name + "-1");
                    }
                    FileUtils.deleteQuietly(new File(workPath));
                }
                File sdk = new File(workspace, "base.apk");
                try {
                    // 从 assets 提取引擎 DEX 和 native 库
                    FileUtils.extractAsset(app, ENGINE_JAR_NAME, sdk);
                    FileUtils.extractFile(sdk, "lib/", workspace);
                    object.putOpt("current", workspace.getPath());
                    object.putOpt("time", installTime);
                    FileUtils.writeString(config, object.toString());
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
        loadEngine(workspace);
    }

    /**
     * 读取 JSON 配置文件。
     *
     * @param file 配置文件路径
     * @return 解析后的 {@link JSONObject}，文件不存在或解析失败时返回空对象
     */
    private static JSONObject readJson(File file) {
        String config = FileUtils.readString(file);
        if (config != null) {
            try {
                return new JSONObject(config);
            } catch (JSONException e) {
                return new JSONObject();
            }
        }
        return new JSONObject();
    }


    /**
     * 使用 {@link DexClassLoader} 加载引擎 DEX。
     * <p>
     * 根据当前设备的 CPU 架构（32 位或 64 位），收集对应的 native 库目录，
     * 构建库搜索路径后创建类加载器实例。
     * </p>
     *
     * @param root 引擎文件的根目录，包含 base.apk 和 lib 子目录
     */
    private static void loadEngine(File root) {
        File sdk = new File(root, "base.apk");
        File engineLibDir = new File(root, "lib");
        ArrayList<String> libDirs = new ArrayList<>();
        // 根据设备架构收集 native 库目录
        if (ProcessUtils.is64Bit()) {
            for (String abi : Build.SUPPORTED_64_BIT_ABIS) {
                File libDir = new File(engineLibDir, abi);
                if (libDir.exists()) libDirs.add(libDir.getAbsolutePath());
            }
        } else {
            for (String abi : Build.SUPPORTED_32_BIT_ABIS) {
                File libDir = new File(engineLibDir, abi);
                if (libDir.exists()) libDirs.add(libDir.getAbsolutePath());
            }
        }

        // 使用路径分隔符拼接多个库目录
        StringBuilder builder = new StringBuilder();

        int size = libDirs.size();
        for (int i = 0; i < size; i++) {
            builder.append(libDirs.get(i));
            if (i != (size - 1)) {
                builder.append(File.pathSeparator);
            }
        }

        String libSearchDir = builder.toString();


        hackClassLoader = new DexClassLoader(sdk.getPath(), sdk.getParent(), libSearchDir, Context.class.getClassLoader());
    }

    /**
     * 获取 Hack 引擎的类加载器。
     *
     * @return 引擎的 {@link DexClassLoader}，未初始化时返回 {@code null}
     */
    public static DexClassLoader getHackClassLoader() {
        return hackClassLoader;
    }

    /**
     * 设置 Hack 引擎的类加载器。
     * <p>
     * 允许外部替换引擎的类加载器实例，用于自定义加载场景。
     * </p>
     *
     * @param classLoader 新的 {@link DexClassLoader} 实例
     */
    public static void setHackClassLoader(DexClassLoader classLoader) {
        hackClassLoader = classLoader;
    }
}
