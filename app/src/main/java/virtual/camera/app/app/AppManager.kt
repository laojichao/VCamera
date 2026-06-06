package virtual.camera.app.app

import android.content.Context
import android.content.SharedPreferences

/**
 * 应用管理器单例，提供全局共享的 SharedPreferences 实例。
 *
 * 用于存储用户备注、应用排序列表等持久化数据。
 */
object AppManager {

    /**
     * 用户备注信息的 SharedPreferences 实例，懒加载初始化。
     *
     * SP 文件名为 "UserRemark"，以私有模式打开。
     */
    @JvmStatic
    val mRemarkSharedPreferences: SharedPreferences by lazy {
        App.getContext().getSharedPreferences("UserRemark",Context.MODE_PRIVATE)
    }
}
