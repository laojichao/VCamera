package com.hack.server.core;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import com.hack.opensdk.R;

/**
 * 前台服务实现类。
 * <p>
 * 通过创建前台通知保持进程存活，防止系统在内存不足时杀死宿主服务进程。
 * 该服务在 Android O 及以上版本会创建通知渠道，并根据系统版本构建对应的通知样式。
 * </p>
 */
public class ForgroundService extends Service {

    /** 通知渠道唯一标识 */
    private static final String FORGROUND_CHANNEL_ID = "hack_forground_id";
    /** 通知渠道显示名称 */
    private static final String FORGROUND_CHANNEL_NAME = "hack_forground_channel";
    /** 前台通知的唯一 ID */
    private static final int FORGROUND_NOTIFICATION_ID = 1000000;

    /**
     * 服务创建时的回调。
     * <p>在此方法中初始化前台通知，将服务提升为前台进程以保证持续运行。</p>
     */
    @Override
    public void onCreate() {
        super.onCreate();
        startForgroundNotification(getApplicationContext());
    }

    /**
     * 绑定服务时的回调。
     * <p>该服务不支持绑定，始终返回 {@code null}。</p>
     *
     * @param intent 绑定请求所携带的 Intent
     * @return 始终返回 {@code null}，表示不提供 IBinder
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 创建并启动前台通知。
     * <p>在 Android O 及以上版本会先创建通知渠道；在 O_MR1 及以上版本使用
     * {@link Notification.Builder} 构建完整通知，低版本则创建空通知。</p>
     *
     * @param context 应用上下文，用于获取系统通知管理器
     */
    private void startForgroundNotification(Context context) {
        //1.create channel if needed
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            final NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            final NotificationChannel forgroundChannel = new NotificationChannel(FORGROUND_CHANNEL_ID,
                    FORGROUND_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(forgroundChannel);
        }

        //2.create notification
        final Notification notification;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            notification = new Notification();
        } else {

            Intent notificationIntent = new Intent();
            notificationIntent.setPackage(getPackageName());
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            String name = String.valueOf(getApplicationInfo().loadLabel(getPackageManager()));
            notification = new Notification.Builder(this, FORGROUND_CHANNEL_ID)
                    .setContentTitle(getString(R.string.notification_running_title, name))
                    .setContentText(getString(R.string.notification_running_warn, name))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(false)
                    .build();
        }

        try {
            //3.start
            startForeground(FORGROUND_NOTIFICATION_ID, notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
