package com.hack.server.core.transact;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.hack.Features;
import com.hack.opensdk.CmdConstants;
import com.hack.opensdk.HackApi;

/**
 * Activity 生命周期监听器，用于修正任务描述信息。
 * <p>
 * 实现 {@link Application.ActivityLifecycleCallbacks} 接口，以单例模式运行。
 * 当 Activity 创建且为任务根 Activity 时，自动修正最近任务列表中的图标和标题，
 * 使其与当前虚拟空间的应用信息保持一致。
 * </p>
 */
public class TransactActivityLifecycle implements Application.ActivityLifecycleCallbacks {
    private static final String TAG = TransactActivityLifecycle.class.getSimpleName();
    /** 全局单例实例 */
    public static final TransactActivityLifecycle INSTANCE = new TransactActivityLifecycle();

    /**
     * Activity 创建时的回调。
     * <p>若 Activity 是任务根节点，则修正其任务描述（图标和标签）。</p>
     *
     * @param activity          当前创建的 Activity
     * @param savedInstanceState 保存的实例状态，可能为 {@code null}
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (Features.DEBUG) {
            Log.e(TAG, "onActivityCreated: " + activity.getLocalClassName());
        }
        if (activity.isTaskRoot()) {
            fixTaskDescription(activity);
        }

    }

    /**
     * 修正 Activity 的任务描述信息。
     * <p>从虚拟空间获取 Activity 的原始图标和标签，若当前处于非零空间，
     * 则在标签后追加空间编号后缀。将修正后的信息设置到最近任务列表中。</p>
     *
     * @param activity 需要修正任务描述的 Activity
     * @return {@code true} 表示修正成功，{@code false} 表示获取 ActivityInfo 失败
     */
    private boolean fixTaskDescription(Activity activity) {
        int space = HackApi.getRuntimeProperty(CmdConstants.RUNTIME_PROPERTIES_SPACE, 0);
        ActivityInfo info = HackApi.getActivityInfo(new ComponentName(activity.getPackageName(), activity.getClass().getName()), 0, space);
        if (info == null) {
            Log.e(TAG, "fixTaskDescription: fail");
            return false;
        }
        PackageManager pm = activity.getPackageManager();
        Drawable data = info.loadIcon(pm);
        String label = String.valueOf(info.loadLabel(pm));
        if (space != 0) {
            label += "(" + (space + 1) + ")";
        }
        Bitmap icon;
        if (data instanceof BitmapDrawable) {
            icon = ((BitmapDrawable) data).getBitmap();
        } else {
            icon = Bitmap.createBitmap(data.getIntrinsicWidth(), data.getIntrinsicHeight(), data.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(icon);
            data.setBounds(0, 0, data.getIntrinsicWidth(), data.getIntrinsicHeight());
            data.draw(canvas);
        }
        activity.setTaskDescription(new ActivityManager.TaskDescription(label, icon));
        return true;
    }


    /**
     * Activity 启动时的回调（未使用）。
     *
     * @param activity 当前 Activity
     */
    @Override
    public void onActivityStarted(Activity activity) {

    }

    /**
     * Activity 恢复时的回调（未使用）。
     *
     * @param activity 当前 Activity
     */
    @Override
    public void onActivityResumed(Activity activity) {

    }

    /**
     * Activity 暂停时的回调（未使用）。
     *
     * @param activity 当前 Activity
     */
    @Override
    public void onActivityPaused(Activity activity) {

    }

    /**
     * Activity 停止时的回调（未使用）。
     *
     * @param activity 当前 Activity
     */
    @Override
    public void onActivityStopped(Activity activity) {

    }

    /**
     * Activity 保存实例状态时的回调（未使用）。
     *
     * @param activity    当前 Activity
     * @param outState    保存状态的 Bundle
     */
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    /**
     * Activity 销毁时的回调（未使用）。
     *
     * @param activity 当前 Activity
     */
    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
