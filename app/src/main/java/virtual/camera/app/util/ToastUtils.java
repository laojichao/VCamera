package virtual.camera.app.util;

import android.content.Context;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

import virtual.camera.app.app.App;


/**
 * Toast 消息提示工具类。
 * <p>
 * 封装 Android {@link Toast}，提供多种参数重载的静态方法以简化调用。
 * 自动处理非主线程调用场景，支持居中显示，复用 {@link Toast} 实例以避免频繁创建。
 * </p>
 */
public class ToastUtils {

    /**
     * 通过资源 ID 显示短时 Toast 消息（使用全局上下文）。
     *
     * @param messageID 字符串资源 ID
     */
        showToast(App.getContext(), messageID);
    }


    /**
     * 显示短时 Toast 消息（使用全局上下文）。
     *
     * @param message 消息文本，不可为 {@code null}
     */
        showToast(App.getContext(), message);
    }


    /**
     * 通过资源 ID 显示 Toast 消息（使用全局上下文）。
     *
     * @param messageID 字符串资源 ID
     * @param duration  显示时长，取值 {@link Toast#LENGTH_SHORT} 或 {@link Toast#LENGTH_LONG}
     */
        showToast(App.getContext(), messageID, duration);
    }

    /**
     * 显示 Toast 消息（使用全局上下文）。
     *
     * @param message  消息文本，不可为 {@code null}
     * @param duration 显示时长，取值 {@link Toast#LENGTH_SHORT} 或 {@link Toast#LENGTH_LONG}
     */
        showToast(App.getContext(), message, duration);
    }


    /**
     * 通过资源 ID 显示短时 Toast 消息。
     *
     * @param context 上下文对象，不可为 {@code null}
     * @param resId   字符串资源 ID
     */
        showToast(context, context.getString(resId), Toast.LENGTH_SHORT);
    }

    /**
     * 显示短时 Toast 消息。
     *
     * @param context 上下文对象，不可为 {@code null}
     * @param message 消息文本，不可为 {@code null}
     */
        showToast(context, message, Toast.LENGTH_SHORT);
    }


    private static void showToast(Context context, int resId, int duration) {
        ///Toast.makeText(context, resId, duration).show();
        showToast(context, context.getString(resId), duration);
    }

    /** 复用的 Toast 实例，避免重复创建 */
    static Toast toast;

    /**
     * 显示 Toast 消息的核心实现方法。
     * <p>
     * 自动检测当前线程：若不在主线程则切换到主线程执行显示操作，
     * 避免在子线程中直接操作 UI 导致崩溃。
     * </p>
     *
     * @param context  上下文对象，不可为 {@code null}
     * @param message  消息文本
     * @param duration 显示时长
     */
    private static void showToast(Context context, String message, int duration) {
        if (context == null) {
            return;
        }
        if (Looper.getMainLooper() != Looper.myLooper()) {
            HandlerUtil.runOnMain(new Runnable() {
                @Override
                public void run() {
                    toast(context, message, duration);
                }
            });
        } else {
            toast(context, message, duration);
        }
    }

    /**
     * 创建并显示自定义样式的 Toast（垂直居中）。
     * 若创建自定义 Toast 失败，则回退使用系统默认 {@code Toast.makeText()} 方式。
     *
     * @param context  上下文对象
     * @param message  消息文本
     * @param duration 显示时长
     */
    private static void toast(Context context, String message, int duration) {
        try {
            if (toast == null) {
                toast = new Toast(context);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.setDuration(duration);
            }
            toast.setText(message);
            toast.show();
        } catch (Exception e) {
            Toast.makeText(context, message, duration).show();
            e.printStackTrace();
        }
    }
}
