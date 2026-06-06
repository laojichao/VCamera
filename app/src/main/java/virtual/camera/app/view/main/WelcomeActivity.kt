package virtual.camera.app.view.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import virtual.camera.app.R
import virtual.camera.app.util.HandlerUtil
import virtual.camera.app.util.InjectionUtil
import virtual.camera.app.view.list.ListViewModel

/**
 * 应用欢迎/启动页界面。
 *
 * 在应用冷启动时显示启动画面，同时后台预加载已安装应用列表缓存。
 * 延迟 3.5 秒后自动跳转至 [MainActivity]。
 *
 * 若在启动页显示期间再次收到 Intent（热启动），则立即跳转。
 */
class WelcomeActivity : AppCompatActivity() {

    /**
     * 处理热启动场景下的新 Intent，立即跳转至主界面。
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        jump()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        previewInstalledAppList()
        // 延迟 3.5 秒后跳转，给用户时间查看启动画面
        HandlerUtil.runOnMain(
            {
                jump()
            },
            3500
        );
    }

    /**
     * 跳转至主界面并关闭当前启动页。
     */
    private fun jump() {
        MainActivity.start(this)
        finish()
    }

    /**
     * 后台预加载已安装应用列表，避免主界面首次打开时的加载等待。
     */
    private fun previewInstalledAppList() {
        val viewModel =
            ViewModelProvider(this, InjectionUtil.getListFactory()).get(ListViewModel::class.java)
        viewModel.previewInstalledList()
    }
}