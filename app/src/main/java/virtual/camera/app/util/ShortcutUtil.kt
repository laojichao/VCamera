package virtual.camera.app.util

import android.content.Context
import virtual.camera.app.bean.AppInfo

/**
 * 桌面快捷方式工具对象，用于为虚拟空间中的应用创建桌面快捷方式。
 */
object ShortcutUtil {


    /**
     * 为指定应用创建桌面快捷方式。
     *
     * @param context 上下文
     * @param userID 虚拟用户空间 ID
     * @param info 要创建快捷方式的应用信息
     */
    fun createShortcut(context: Context,userID: Int, info: AppInfo) {

    }

    /**
     * 显示创建快捷方式所需的权限申请对话框。
     *
     * @param context 上下文
     */
    private fun showAllowPermissionDialog(context: Context){


    }
}