package virtual.camera.app.util;

import android.os.Build;
import android.os.Process;

/**
 * CPU 架构检测工具类。
 * <p>
 * 提供判断当前设备是否运行在 64 位模式下的能力。
 * 针对 Android M（API 23）及以上版本使用 {@link Process#is64Bit()} 精确判断，
 * 低版本通过 {@link Build#CPU_ABI} 字符串进行兼容检测。
 * </p>
 */
public class AbiCore {

    /**
     * 判断当前进程是否运行在 64 位模式下。
     * <p>
     * Android M 及以上版本直接调用系统 API；
     * 低版本回退到 {@code Build.CPU_ABI} 字符串匹配。
     * </p>
     *
     * @return {@code true} 表示当前为 64 位模式，{@code false} 表示 32 位模式
     */
    public static boolean is64Bit() {
        if (isM()) {
            return Process.is64Bit();
        } else {
            return Build.CPU_ABI.equals("arm64-v8a");
        }
    }

    /**
     * 判断当前系统版本是否为 Android M（API 23）及以上。
     *
     * @return {@code true} 表示 SDK 版本 &ge; 23，{@code false} 表示低于该版本
     */
    public static boolean isM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
