package virtual.camera.app.view.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.hack.opensdk.HackApi
import virtual.camera.app.R
import virtual.camera.app.app.App
import virtual.camera.app.app.AppManager
import virtual.camera.app.databinding.ActivityMainBinding
import virtual.camera.app.util.AppUtil
import virtual.camera.app.util.Resolution
import virtual.camera.app.util.ToastUtils
import virtual.camera.app.util.inflate
import virtual.camera.app.view.apps.AppsFragment
import virtual.camera.app.view.base.LoadingActivity
import virtual.camera.app.view.list.ListActivity
import virtual.camera.app.view.setting.SettingActivity


/**
 * 应用主界面。
 *
 * 通过 ViewPager2 + DotsIndicator 展示多用户空间的应用列表，
 * 每个用户空间对应一个 [AppsFragment] 页面。
 * 支持以下功能：
 * - 左右滑动切换用户空间
 * - 通过 FAB 按钮安装新应用（跳转至 [ListActivity] 选择 APK）
 * - 点击工具栏副标题编辑用户备注
 * - 菜单中提供设置页面入口和一键杀掉所有应用功能
 */
class MainActivity : LoadingActivity() {

    private val viewBinding: ActivityMainBinding by inflate()

    private lateinit var mViewPagerAdapter: ViewPagerAdapter

    /** 所有用户空间对应的 Fragment 列表 */
    private val fragmentList = mutableListOf<AppsFragment>()

    /** 当前选中的用户空间 ID */
    private var currentUser = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.app_name)
        initViewPager()
        initFab()
        initToolbarSubTitle()
        DialogUtil.showDialog(this,true)
    }

    /**
     * 初始化工具栏副标题。
     *
     * 设置默认用户备注，并为副标题注册点击事件以弹出编辑对话框。
     * 通过 hack 方式直接获取工具栏的子视图来绑定点击事件。
     */
    private fun initToolbarSubTitle() {
        updateUserRemark(0)
        //hack code
        viewBinding.toolbarLayout.toolbar.getChildAt(1).setOnClickListener {
            MaterialDialog(this).show {
                title(res = R.string.userRemark)
                input(
                    hintRes = R.string.userRemark,
                    prefill = viewBinding.toolbarLayout.toolbar.subtitle
                ) { _, input ->
                    AppManager.mRemarkSharedPreferences.edit {
                        putString("Remark$currentUser", input.toString())
                        viewBinding.toolbarLayout.toolbar.subtitle = input
                    }
                }
                positiveButton(res = R.string.done)
                negativeButton(res = R.string.cancel)
            }
        }
    }

    /**
     * 初始化 ViewPager。
     *
     * 从 [HackApi] 获取可用用户空间列表，为每个用户空间创建对应的 [AppsFragment]，
     * 并额外添加一个"新建用户"的占位 Fragment。注册页面切换回调以更新用户备注和 FAB 状态。
     */
    private fun initViewPager() {
        val userList = HackApi.getAvailableUserSpace()
        userList.forEach {
            fragmentList.add(AppsFragment.newInstance(it))
        }

        currentUser = userList.firstOrNull() ?: 0
        // 末尾追加一个用于"新建用户空间"的占位 Fragment
        fragmentList.add(AppsFragment.newInstance(userList.size))

        mViewPagerAdapter = ViewPagerAdapter(this)
        mViewPagerAdapter.replaceData(fragmentList)
        viewBinding.viewPager.adapter = mViewPagerAdapter
        viewBinding.dotsIndicator.setViewPager2(viewBinding.viewPager)
        viewBinding.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentUser = fragmentList[position].userID
                updateUserRemark(currentUser)
                showFloatButton(true)
            }
        })

    }

    /**
     * 初始化 FAB 悬浮按钮，点击后跳转至 [ListActivity] 选择要安装的 APK。
     */
    private fun initFab() {
        viewBinding.fab.setOnClickListener {
            val userId = viewBinding.viewPager.currentItem
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra("userID", userId)
            apkPathResult.launch(intent)
        }
    }

    /**
     * 控制 FAB 悬浮按钮的显示/隐藏动画。
     *
     * @param show true 显示按钮，false 隐藏按钮（向下平移并淡出）
     */
    fun showFloatButton(show: Boolean) {
        val tranY: Float = Resolution.convertDpToPixel(120F, App.getContext())
        val time = 200L
        if (show) {
            viewBinding.fab.animate().translationY(0f).alpha(1f).setDuration(time)
                .start()
        } else {
            viewBinding.fab.animate().translationY(tranY).alpha(0f).setDuration(time)
                .start()
        }
    }

    /**
     * 扫描用户空间变化，同步更新 Fragment 列表。
     *
     * 当用户空间数量增加时添加新 Fragment，减少时移除末尾多余项。
     */
    fun scanUser() {
        val userList = HackApi.getAvailableUserSpace()

        if (fragmentList.size == userList.size) {
            fragmentList.add(AppsFragment.newInstance(fragmentList.size))
        } else if (fragmentList.size > userList.size + 1) {
            fragmentList.removeLast()
        }

        mViewPagerAdapter.notifyDataSetChanged()

    }

    /**
     * 更新工具栏副标题为指定用户空间的备注名称。
     *
     * @param userId 用户空间 ID
     */
    private fun updateUserRemark(userId: Int) {
        var remark = AppManager.mRemarkSharedPreferences.getString("Remark$userId", "User $userId")
        if (remark.isNullOrEmpty()) {
            remark = "User $userId"
        }

        viewBinding.toolbarLayout.toolbar.subtitle = remark
    }

    /**
     * APK 安装结果回调。
     *
     * 从 [ListActivity] 接收选中的 APK 路径和目标用户空间 ID，
     * 调用对应 Fragment 的安装方法执行安装。
     */
    private val apkPathResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.let { data ->
                    val userId = data.getIntExtra("userID", 0)
                    val source = data.getStringExtra("source")
                    if (source != null) {
                        fragmentList[userId].installApk(source)
                    }
                }

            }
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    /**
     * 处理菜单项点击事件。
     *
     * - main_setting: 跳转至设置页面
     * - killApps: 杀掉所有已运行的虚拟应用
     * - open_source: 显示开源信息对话框
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.main_setting -> {
                SettingActivity.start(this)
            }
            R.id.killApps->{
                AppUtil.killAllApps()
                ToastUtils.showToast("done.")
            }
            R.id.open_source->{
                DialogUtil.showDialog(this,false)
            }
        }
        return true
    }

    /**
     * 伴生对象，提供便捷的页面启动方法。
     */
    companion object {
        /**
         * 启动主界面。
         *
         * @param context 上下文环境
         */
        fun start(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }

}
