package virtual.camera.app.util;

import android.os.Build;
import android.os.Process;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * APK 原生库（ABI）架构兼容性检测工具类。
 * <p>
 * 通过解析 APK 文件中的 {@code lib/} 目录，识别其所包含的原生库架构
 * （如 {@code arm64-v8a}、{@code armeabi}、{@code armeabi-v7a}），
 * 并结合当前设备的 CPU 架构判断该 APK 是否与设备兼容。
 * 内部使用缓存机制避免对同一 APK 文件的重复解析。
 * </p>
 */
public class AbiUtils {

    /** APK 中检测到的原生库架构集合 */
    private final Set<String> mLibs = new HashSet<>();

    /** 已解析 APK 文件的缓存，键为 APK 文件，值为对应的 AbiUtils 实例 */
    private static final Map<File, AbiUtils> sAbiUtilsMap = new HashMap<>();

    /**
     * 判断指定 APK 文件是否与当前设备架构兼容。
     * <p>
     * 若 APK 不含任何原生库，则视为兼容（纯 Java 应用）。
     * 否则，根据当前设备是 64 位还是 32 位，检查 APK 是否包含对应架构的库。
     * </p>
     *
     * @param apkFile APK 文件，不可为 {@code null}
     * @return {@code true} 表示 APK 与当前设备兼容，{@code false} 表示不兼容
     */
    public static boolean isSupport(File apkFile) {
        AbiUtils abiUtils = sAbiUtilsMap.get(apkFile);
        if (abiUtils == null) {
            abiUtils = new AbiUtils(apkFile);
            sAbiUtilsMap.put(apkFile, abiUtils);
        }
        if (abiUtils.isEmptyAib()) {
            return true;
        }

        if (AbiCore.is64Bit()) {
            return abiUtils.is64Bit();
        } else {
            return abiUtils.is32Bit();
        }
    }

    /**
     * 构造方法，解析指定 APK 文件中包含的原生库架构。
     * <p>
     * 遍历 APK（ZIP 格式）中所有以 {@code lib/} 开头的条目，
     * 识别 {@code arm64-v8a}、{@code armeabi}、{@code armeabi-v7a} 三种架构。
     * </p>
     *
     * @param apkFile APK 文件路径，不可为 {@code null}
     */
    public AbiUtils(File apkFile) {
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(apkFile);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (name.startsWith("lib/arm64-v8a")) {
                    mLibs.add("arm64-v8a");
                } else if (name.startsWith("lib/armeabi")) {
                    mLibs.add("armeabi");
                } else if (name.startsWith("lib/armeabi-v7a")) {
                    mLibs.add("armeabi-v7a");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(zipFile);
        }
    }

    /**
     * 判断 APK 是否包含 64 位（arm64-v8a）原生库。
     *
     * @return {@code true} 表示包含 64 位库，{@code false} 表示不包含
     */
    public boolean is64Bit() {
        return mLibs.contains("arm64-v8a");
    }

    /**
     * 判断 APK 是否包含 32 位（armeabi 或 armeabi-v7a）原生库。
     *
     * @return {@code true} 表示包含 32 位库，{@code false} 表示不包含
     */
    public boolean is32Bit() {
        return mLibs.contains("armeabi") || mLibs.contains("armeabi-v7a");
    }

    /**
     * 判断 APK 中是否未包含任何原生库架构。
     * <p>
     * 空架构通常表示纯 Java/Kotlin 应用，可兼容所有设备。
     * </p>
     *
     * @return {@code true} 表示无原生库，{@code false} 表示至少包含一种架构
     */
    public boolean isEmptyAib() {
        return mLibs.isEmpty();
    }

    /**
     * 安全关闭一个或多个 {@link Closeable} 资源。
     * <p>
     * 对每个非 {@code null} 的资源调用 {@code close()}，忽略关闭时抛出的异常。
     * </p>
     *
     * @param closeables 待关闭的资源数组，可为 {@code null}
     */
    public static void close(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
        }
    }
}
