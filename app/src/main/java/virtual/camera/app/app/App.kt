package virtual.camera.app.app

import android.annotation.SuppressLint
import android.content.Context
import com.hack.opensdk.HackApplication

/**
 * 应用程序入口类，继承自 [HackApplication]。
 *
 * 负责在应用启动时初始化全局上下文，供其他模块通过 [getContext] 获取应用级 Context。
 */
class App : HackApplication() {

    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private lateinit var mContext: Context

        /**
         * 获取全局应用上下文。
         *
         * @return 应用级 [Context] 实例，需在 [attachBaseContext] 调用后使用
         */
        @JvmStatic
        fun getContext(): Context {
            return mContext
        }
    }

    /**
     * 在应用基础上下文附加时保存 Context 引用。
     *
     * @param base 基础上下文，不可为 null
     */
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        mContext = base!!
    }
}