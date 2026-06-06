package virtual.camera.app.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;

import virtual.camera.app.app.App;
import virtual.camera.app.settings.LogUtil;

/**
 * 应用进程管理工具类。
 * <p>
 * 提供与应用进程生命周期相关的操作方法，
 * 主要用于在设置变更后清理相关子进程以确保配置生效。
 * </p>
 */
public class AppUtil {

    /**
     * 终止当前应用的所有子进程。
     * <p>
     * 遍历系统中属于当前应用 UID 的所有运行中进程，
     * 仅终止非主进程（即进程名包含 ":agent" 的子进程），
     * 保留主进程不被杀死。
     * </p>
     */
    public static void killAllApps() {
        try {
            int uid = App.getContext().getPackageManager().getApplicationInfo(App.getContext().getPackageName(), 0).uid;
            String str = App.getContext().getPackageName();
            for (ActivityManager.RunningAppProcessInfo processInfo : ((ActivityManager) App.getContext().getSystemService(Context.ACTIVITY_SERVICE)).getRunningAppProcesses()) {
                if (processInfo.uid != uid) {
                    continue;
                }
                if(processInfo.processName.startsWith(str) && !processInfo.processName.contains(":agent")){
                    continue;
                }
                LogUtil.log("kill processInfo:"+processInfo.processName);
                Process.killProcess(processInfo.pid);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}
