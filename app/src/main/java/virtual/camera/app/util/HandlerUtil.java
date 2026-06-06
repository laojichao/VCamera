package virtual.camera.app.util;

import android.os.Handler;
import android.os.Looper;

/**
 * 主线程（UI 线程）任务调度工具类。
 * <p>
 * 封装 {@link Handler}，提供将 {@link Runnable} 任务投递到主线程执行的便捷方法，
 * 支持即时执行和延迟执行两种模式。
 * </p>
 */
public class HandlerUtil {

    /** 绑定到主线程 Looper 的 Handler 实例 */
    private static Handler sH = new Handler(Looper.getMainLooper());

    /**
     * 将任务立即投递到主线程执行。
     *
     * @param runnable 待执行的任务，不可为 {@code null}
     */
    public static void runOnMain(Runnable runnable) {
        sH.post(runnable);
    }

    /**
     * 将任务延迟指定时间后投递到主线程执行。
     *
     * @param runnable 待执行的任务，不可为 {@code null}
     * @param delay    延迟时间，单位为毫秒
     */
    public static void runOnMain(Runnable runnable, long delay) {
        sH.postDelayed(runnable, delay);
    }
}
