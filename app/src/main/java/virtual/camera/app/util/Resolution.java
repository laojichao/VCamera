package virtual.camera.app.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Field;

/**
 * 屏幕分辨率与显示相关的工具类。
 * <p>
 * 提供屏幕宽高获取、dp/sp/px 单位互转、软键盘显示/隐藏、
 * 状态栏高度获取、导航栏尺寸获取等常用 UI 辅助方法。
 * 兼容不同 Android API 版本的屏幕信息获取方式。
 * </p>
 */
public class Resolution {
    private static final String TAG = "UtilsScreen";

    /**
     * Gets the width of the display, in pixels.
     * <p>
     * Note that this value should not be used for computing layouts, since a
     * device will typically have screen decoration (such as a status bar) along
     * the edges of the display that reduce the amount of application space
     * available from the size returned here. Layouts should instead use the
     * window size.
     * <p>
     * The size is adjusted based on the current rotation of the display.
     * <p>
     * The size returned by this method does not necessarily represent the
     * actual raw size (native resolution) of the display. The returned size may
     * be adjusted to exclude certain system decoration elements that are always
     * visible. It may also be scaled to provide compatibility with older
     * applications that were originally designed for smaller displays.
     *
     * @return Screen width in pixels.
     */
    public static int getScreenWidth(Context context) {
        return getScreenSize(context, null).x;
    }

    /**
     * Gets the height of the display, in pixels.
     * <p>
     * Note that this value should not be used for computing layouts, since a
     * device will typically have screen decoration (such as a status bar) along
     * the edges of the display that reduce the amount of application space
     * available from the size returned here. Layouts should instead use the
     * window size.
     * <p>
     * The size is adjusted based on the current rotation of the display.
     * <p>
     * The size returned by this method does not necessarily represent the
     * actual raw size (native resolution) of the display. The returned size may
     * be adjusted to exclude certain system decoration elements that are always
     * visible. It may also be scaled to provide compatibility with older
     * applications that were originally designed for smaller displays.
     *
     * @return Screen height in pixels.
     */
    public static int getScreenHeight(Context context) {
        return getScreenSize(context, null).y;
    }

    /**
     * Gets the size of the display, in pixels.
     * <p>
     * Note that this value should not be used for computing layouts, since a
     * device will typically have screen decoration (such as a status bar) along
     * the edges of the display that reduce the amount of application space
     * available from the size returned here. Layouts should instead use the
     * window size.
     * <p>
     * The size is adjusted based on the current rotation of the display.
     * <p>
     * The size returned by this method does not necessarily represent the
     * actual raw size (native resolution) of the display. The returned size may
     * be adjusted to exclude certain system decoration elements that are always
     * visible. It may also be scaled to provide compatibility with older
     * applications that were originally designed for smaller displays.
     *
     * @param outSize null-ok. If it is null, will create a Point instance inside,
     *                otherwise use it to fill the output. NOTE if it is not null,
     *                it will be the returned value.
     * @return Screen size in pixels, the x is the width, the y is the height.
     */
    @SuppressLint("NewApi")
    public static Point getScreenSize(Context context, Point outSize) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Point ret = outSize == null ? new Point() : outSize;
        final Display defaultDisplay = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= 13) {
            defaultDisplay.getSize(ret);
        } else {
            ret.x = defaultDisplay.getWidth();
            ret.y = defaultDisplay.getHeight();
        }
        return ret;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

    ///////////////////////////////////////////////////////////////////////

    /**
     * 获取屏幕密度（density）。
     *
     * @param context 上下文对象，不可为 {@code null}
     * @return 屏幕密度值；若上下文为 {@code null} 则返回 0
     */
    public static float getDensity(Context context) {
        float density = 0f;
        if (context== null) {
            return density;
        }
        try {
            density = context.getResources().getDisplayMetrics().density;
        } catch (Exception e) {

        }
        return density;
    }

    /**
     * 检查指定分辨率是否与当前设备屏幕的真实物理分辨率一致。
     *
     * @param context Activity 上下文，不可为 {@code null}
     * @param width   待比较的宽度像素值
     * @param height  待比较的高度像素值
     * @return {@code true} 表示分辨率一致，{@code false} 表示不一致
     */
    public static boolean checkPix(Activity context, int width, int height) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            DisplayMetrics metrics = new DisplayMetrics();
            context.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            return metrics.widthPixels == width && metrics.heightPixels == height;
        } else {
            return getScreenPixWidth(context) == width && getScreenPixHeight(context) == height;
        }
    }

    /**
     * 获取屏幕分辨率的宽度（像素）。
     *
     * @param context 上下文对象，不可为 {@code null}
     * @return 屏幕宽度像素值
     */
    public static int getScreenPixWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕分辨率的高度（像素）。
     *
     * @param context 上下文对象，不可为 {@code null}
     * @return 屏幕高度像素值
     */
    public static int getScreenPixHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 将 dp（密度无关像素）值转换为 px（像素）值。
     *
     * @param context 上下文对象，不可为 {@code null}
     * @param dip     dp 值
     * @return 对应的 px 值（四舍五入取整）
     */
    public static int dipToPx(Context context, int dip) {
        return (int) (dip * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    /**
     * 将 px（像素）值转换为 dp（密度无关像素）值。
     *
     * @param context 上下文对象，不可为 {@code null}
     * @param pxValue px 值
     * @return 对应的 dp 值（四舍五入取整）
     */
    public static int pxToDip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将 sp（可缩放像素）值转换为 px（像素）值，保证文字大小在不同密度屏幕上一致。
     *
     * @param context 上下文对象，不可为 {@code null}
     * @param spValue sp 值
     * @return 对应的 px 值（四舍五入取整）
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * 隐藏指定 View 关联的软键盘。
     *
     * @param view 当前持有焦点的 View，不可为 {@code null}
     */
    public static void hideInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 为指定 View 显示软键盘。
     *
     * @param view 需要获取输入焦点的 View，不可为 {@code null}
     */
    public static void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * 延迟指定毫秒后为指定 View 显示软键盘。
     *
     * @param view       需要获取输入焦点的 View，不可为 {@code null}
     * @param delayMillis 延迟时间，单位为毫秒
     */
    public static void showInputMethod(final View view, long delayMillis) {
        // 显示输入法
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Resolution.showInputMethod(view);

            }
        }, delayMillis);
    }

    /**
     * 判断手机当前是否处于未锁屏（可交互）状态。
     *
     * @param c 上下文对象，不可为 {@code null}
     * @return {@code true} 表示未锁屏，{@code false} 表示处于锁屏状态
     */
    public static boolean isScreenLocked(Context c) {
        KeyguardManager mKeyguardManager = (KeyguardManager) c
                .getSystemService(Context.KEYGUARD_SERVICE);
        boolean bResult = !mKeyguardManager.inKeyguardRestrictedInputMode();

        return bResult;
    }

    /**
     * 获取系统状态栏高度（像素）。
     * <p>
     * 通过反射访问 {@code com.android.internal.R$dimen} 获取状态栏高度资源。
     * 若反射失败则返回默认值 38 像素。
     * </p>
     *
     * @param context 上下文对象，不可为 {@code null}
     * @return 状态栏高度（像素）
     */
    public static int getBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 38;//默认为38，貌似大部分是这样的

        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

    //http://stackoverflow.com/questions/20264268/how-to-get-height-and-width-of-navigation-bar-programmatically

    /**
     * 获取屏幕底部导航栏的尺寸。
     * <p>
     * 通过比较应用可用区域和真实屏幕尺寸的差异来计算导航栏高度。
     * 若不存在导航栏则返回空的 {@link Point}。
     * </p>
     *
     * @param context 上下文对象，不可为 {@code null}
     * @return 导航栏尺寸，x 为宽度，y 为高度（像素）
     */
    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getScreenSize(context, null);
        Point realScreenSize = getRealScreenSize(context);

//        // navigation bar on the right
//        if (appUsableSize.x < realScreenSize.x) {
//            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
//        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }


    /**
     * 获取屏幕的真实物理分辨率（包含系统装饰区域）。
     * <p>
     * API 17 及以上使用 {@code Display.getRealSize()}，
     * API 14 ~ 16 通过反射调用 {@code getRawWidth/getRawHeight}。
     * </p>
     *
     * @param context 上下文对象，不可为 {@code null}
     * @return 真实屏幕尺寸，x 为宽度，y 为高度（像素）
     */
    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (Exception e) {
            }
        }

        return size;
    }
}
