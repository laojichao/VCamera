package virtual.camera.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/**
 * Context 扩展工具对象，提供常用的 Context 扩展函数。
 */
object ContextUtil {

    /**
     * 打开当前应用的系统设置详情页面。
     *
     * 以新任务栈方式启动，用户可在该页面中管理应用权限、存储等系统级设置。
     */
    fun Context.openAppSystemSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            data = Uri.fromParts("package", packageName, null)
        })
    }
}