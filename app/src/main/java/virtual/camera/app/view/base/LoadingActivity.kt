package virtual.camera.app.view.base

import android.view.KeyEvent
import com.roger.catloadinglibrary.CatLoadingView
import virtual.camera.app.R

/**
 * 带加载弹窗的 Activity 抽象基类。
 *
 * 提供全局统一的加载对话框管理能力，子类可通过 [showLoading] 和 [hideLoading]
 * 控制加载状态。加载弹窗不可被用户通过返回键取消。
 */
abstract class LoadingActivity : BaseActivity() {

    /** 加载弹窗实例，懒初始化 */
    private lateinit var loadingView: CatLoadingView

    /**
     * 显示加载弹窗。
     *
     * 如果弹窗未初始化则进行初始化，设置背景色为应用主色调，
     * 禁用点击取消和返回键关闭，确保用户必须等待操作完成。
     */
    fun showLoading() {
        if (!this::loadingView.isInitialized) {
            loadingView = CatLoadingView()
        }

        if (!loadingView.isAdded) {
            loadingView.setBackgroundColor(R.color.primary)
            loadingView.show(supportFragmentManager, "")
            supportFragmentManager.executePendingTransactions()
            loadingView.setClickCancelAble(false)
            // 拦截返回键和 ESC 键，防止用户关闭加载弹窗
            loadingView.dialog?.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                    return@setOnKeyListener true
                }
                false
            }
        }
    }

    /**
     * 隐藏加载弹窗。
     *
     * 仅在弹窗已初始化时执行关闭操作，避免空指针异常。
     */
    fun hideLoading() {
        if (this::loadingView.isInitialized) {
            loadingView.dismiss()
        }
    }
}