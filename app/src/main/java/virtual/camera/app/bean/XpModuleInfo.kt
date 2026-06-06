package virtual.camera.app.bean

import android.graphics.drawable.Drawable

/**
 * Xposed 模块信息数据类，封装已安装的 Xposed 模块的详细信息。
 *
 * @property name 模块显示名称
 * @property desc 模块功能描述
 * @property packageName 模块包名
 * @property version 模块版本号
 * @property enable 该模块是否已启用，可变属性
 * @property icon 模块图标
 */
data class XpModuleInfo(
        val name: String,
        val desc: String,
        val packageName: String,
        val version: String,
        var enable:Boolean,
        val icon: Drawable
)
