package virtual.camera.app.view.base

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * Activity 基类，提供通用的 Toolbar 初始化和用户 ID 获取能力。
 *
 * 所有需要 Toolbar 和虚拟用户 ID 的 Activity 均应继承此类。
 */
open class BaseActivity : AppCompatActivity() {

    /**
     * 初始化 Toolbar，支持设置标题和可选的返回按钮。
     *
     * @param toolbar 待初始化的 Toolbar 实例
     * @param title 标题的字符串资源 ID
     * @param showBack 是否显示返回按钮，默认为 false
     * @param onBack 返回按钮的额外回调，在 Activity finish 之前执行，可为 null
     */
    protected fun initToolbar(toolbar: Toolbar,title:Int, showBack: Boolean = false, onBack: (() -> Unit)? = null) {
        setSupportActionBar(toolbar)
        toolbar.setTitle(title)
        if (showBack) {
            supportActionBar?.let {
                it.setDisplayHomeAsUpEnabled(true)
                toolbar.setNavigationOnClickListener {
                    if (onBack != null) {
                        onBack()
                    }
                    finish()
                }
            }
        }
    }

    /**
     * 从启动 Intent 中获取当前虚拟用户 ID。
     *
     * @return 虚拟用户 ID，默认值为 0
     */
    protected fun currentUserID():Int{
        return intent.getIntExtra("userID", 0)
    }
}