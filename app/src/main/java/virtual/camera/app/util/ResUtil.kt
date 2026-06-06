package virtual.camera.app.util

import androidx.annotation.StringRes
import virtual.camera.app.app.App


/**
 * 获取应用级字符串资源。
 *
 * 通过全局 [App.getContext] 获取字符串资源，支持格式化参数。
 *
 * @param id 字符串资源 ID
 * @param arg 可选的格式化参数
 * @return 格式化后的字符串
 */
fun getString(@StringRes id:Int,vararg arg:String):String{
    if(arg.isEmpty()){
        return App.getContext().getString(id)
    }
    return App.getContext().getString(id,*arg)
}

