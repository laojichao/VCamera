package virtual.camera.app.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import virtual.camera.app.app.App

/** 全局 Toast 实例引用，用于在显示新 Toast 前取消上一个，避免消息堆叠。 */
var toastImpl:Toast? = null

/**
 * 在当前 Context 上显示短时 Toast 消息。
 *
 * 显示前会取消上一个未消失的 Toast，避免消息堆叠。
 *
 * @param msg 要显示的消息文本
 */
fun Context.toast(msg:String){
    toastImpl?.cancel()
    toastImpl = Toast.makeText(this,msg,Toast.LENGTH_SHORT)
    toastImpl?.show()
}

/**
 * 使用全局应用上下文显示短时 Toast 消息。
 *
 * @param msg 要显示的消息文本
 */
fun toast(msg: String){
    App.getContext().toast(msg)
}

/**
 * 使用全局应用上下文通过字符串资源 ID 显示短时 Toast 消息。
 *
 * @param msgID 字符串资源 ID
 */
fun toast(@StringRes msgID:Int){
    toast(getString(msgID))
}