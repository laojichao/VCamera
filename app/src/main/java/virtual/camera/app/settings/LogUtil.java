package virtual.camera.app.settings;

import android.util.Log;

/**
 * 日志工具类。
 * <p>
 * 封装 Android 原生 {@link Log}，提供统一的日志输出入口，
 * 便于在调试阶段集中控制日志标记和级别。
 * </p>
 */
public class LogUtil {

    /**
     * 输出错误级别日志，标签固定为 "VCamera"。
     *
     * @param msg 日志内容，不可为 {@code null}
     */
    public static void log(String msg) {
        Log.e("VCamera", msg);
    }
}
