package virtual.camera.app.bean

import android.graphics.drawable.Drawable

/**
 * 已安装应用信息数据类，用于在虚拟空间中展示应用列表及其安装状态。
 *
 * @property name 应用显示名称
 * @property icon 应用图标
 * @property packageName 应用包名
 * @property sourceDir 应用 APK 文件路径
 * @property isInstall 该应用是否已在当前虚拟用户空间中安装
 */
data class InstalledAppBean(val name:String, val icon: Drawable, val packageName:String, val sourceDir:String, val isInstall:Boolean)
