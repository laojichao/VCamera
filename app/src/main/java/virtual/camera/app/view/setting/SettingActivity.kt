package virtual.camera.app.view.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import virtual.camera.app.R
import virtual.camera.app.databinding.ActivitySettingBinding
import virtual.camera.app.util.inflate
import virtual.camera.app.view.base.BaseActivity

/**
 * 应用设置界面。
 *
 * 作为 [SettingFragment] 的容器 Activity，加载设置 Fragment 到布局中。
 * 继承自 [BaseActivity] 以复用工具栏初始化等通用逻辑。
 */
class SettingActivity : BaseActivity() {

    private val viewBinding: ActivitySettingBinding by inflate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.setting, true)
        // 将 SettingFragment 加载到 fragment 容器中
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, SettingFragment())
                .commit()
    }

    /**
     * 伴生对象，提供便捷的页面启动方法。
     */
    companion object{
        /**
         * 启动设置界面。
         *
         * @param context 上下文环境
         */
        fun start(context: Context){
            val intent = Intent(context,SettingActivity::class.java)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            context.startActivity(intent)
        }
    }

}