package virtual.camera.app.bean

import android.graphics.drawable.Drawable

/**
 * 应用信息数据类，封装已安装应用的基本信息。
 *
 * @property name 应用显示名称
 * @property icon 应用图标
 * @property packageName 应用包名
 * @property sourceDir 应用 APK 文件路径
 * @property isXpModule 是否为 Xposed 模块
 */
data class AppInfo(val name:String,val icon:Drawable,val packageName:String,val sourceDir:String,val isXpModule:Boolean)