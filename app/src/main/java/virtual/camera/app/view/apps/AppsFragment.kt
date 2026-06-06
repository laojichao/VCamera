package virtual.camera.app.view.apps

import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import cbfg.rvadapter.RVAdapter
import com.afollestad.materialdialogs.MaterialDialog
import com.hack.opensdk.HackApi
import virtual.camera.app.R
import virtual.camera.app.bean.AppInfo
import virtual.camera.app.databinding.FragmentAppsBinding
import virtual.camera.app.util.InjectionUtil
import virtual.camera.app.util.ShortcutUtil
import virtual.camera.app.util.inflate
import virtual.camera.app.util.toast
import virtual.camera.app.view.base.LoadingActivity
import virtual.camera.app.view.main.MainActivity
import java.util.*
import kotlin.math.abs


/**
 * 应用管理页面 Fragment，展示指定虚拟用户空间中已安装的应用列表。
 *
 * 主要功能：
 * - 以网格布局展示已安装应用，支持图标、名称和 Xposed 模块标识
 * - 支持拖拽排序，拖拽完成后自动保存顺序
 * - 支持点击启动应用、长按弹出操作菜单（卸载等）
 * - 支持通过外部调用 [installApk] 安装新应用
 * - 通过 [AppsViewModel] 与数据层交互，使用 LiveData 观察数据变化
 */
class AppsFragment : Fragment() {

    /** 当前虚拟用户 ID */
    var userID: Int = 0

    /** 应用管理页面的 ViewModel 实例 */
    private lateinit var viewModel: AppsViewModel

    /** 应用列表的 RecyclerView 适配器 */
    private lateinit var mAdapter: RVAdapter<AppInfo>

    /** 页面视图绑定实例 */
    private val viewBinding: FragmentAppsBinding by inflate()

    /** 当前显示的弹出菜单实例 */
    private var popupMenu: PopupMenu? = null

    /**
     * Fragment 创建时初始化 ViewModel 并从参数中获取虚拟用户 ID。
     *
     * @param savedInstanceState 保存的实例状态
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel =
            ViewModelProvider(this, InjectionUtil.getAppsFactory()).get(AppsViewModel::class.java)
        userID = requireArguments().getInt("userID", 0)
    }

    /**
     * 创建 Fragment 的视图层次结构，初始化 RecyclerView、拖拽排序和点击事件。
     *
     * @param inflater 布局加载器
     * @param container 父 ViewGroup
     * @param savedInstanceState 保存的实例状态
     * @return Fragment 的根视图
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewBinding.stateView.showEmpty()

        mAdapter =
            RVAdapter<AppInfo>(requireContext(), AppsAdapter()).bind(viewBinding.recyclerView)

        viewBinding.recyclerView.adapter = mAdapter
        viewBinding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        // 配置拖拽排序回调
        val touchCallBack = AppsTouchCallBack { from, to ->
            onItemMove(from, to)
            viewModel.updateSortLiveData.postValue(true)
        }

        val itemTouchHelper = ItemTouchHelper(touchCallBack)
        itemTouchHelper.attachToRecyclerView(viewBinding.recyclerView)

        // 配置列表项点击事件：启动对应应用
        mAdapter.setItemClickListener { _, data, _ ->
            showLoading()
            viewModel.launchApk(data.packageName, userID)
        }


        interceptTouch()
        setOnLongClick()
        return viewBinding.root
    }

    /**
     * 视图创建完成后初始化数据观察。
     *
     * @param view Fragment 的根视图
     * @param savedInstanceState 保存的实例状态
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    /**
     * Fragment 可见时刷新已安装应用列表。
     */
    override fun onStart() {
        super.onStart()
        viewModel.getInstalledApps(userID)
    }

    /**
     * 拦截 RecyclerView 的触摸事件，优化拖拽体验。
     *
     * 通过记录按下坐标来区分拖拽和点击，并在滑动时控制弹出菜单的显示/隐藏。
     */
    private fun interceptTouch() {
        val point = Point()
        viewBinding.recyclerView.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_UP -> {
                    // 抬起时，如果未发生移动则显示弹出菜单
                    if (!isMove(point, e)) {
                        popupMenu?.show()
                    }
                    popupMenu = null
                    point.set(0, 0)
                }

                MotionEvent.ACTION_MOVE -> {
                    // 首次移动时记录起始坐标
                    if (point.x == 0 && point.y == 0) {
                        point.x = e.rawX.toInt()
                        point.y = e.rawY.toInt()
                    }
                    isDownAndUp(point, e)

                    // 移动超过阈值时关闭弹出菜单
                    if (isMove(point, e)) {
                        popupMenu?.dismiss()
                    }
                }
            }
            return@setOnTouchListener false
        }
    }

    /**
     * 判断触摸事件是否构成移动操作（移动距离超过阈值）。
     *
     * @param point 触摸按下时记录的坐标点
     * @param e 当前触摸事件
     * @return 如果水平或垂直方向移动距离超过 40 像素则返回 true
     */
    private fun isMove(point: Point, e: MotionEvent): Boolean {
        val max = 40

        val x = point.x
        val y = point.y

        val xU = abs(x - e.rawX)
        val yU = abs(y - e.rawY)
        return xU > max || yU > max
    }

    /**
     * 根据触摸方向控制悬浮按钮的显示/隐藏。
     *
     * @param point 触摸按下时记录的坐标点
     * @param e 当前触摸事件
     */
    private fun isDownAndUp(point: Point, e: MotionEvent) {
        val min = 10
        val y = point.y
        val yU = y - e.rawY

        // 纵向移动超过阈值时，根据方向显示/隐藏悬浮按钮
        if (abs(yU) > min) {
            (requireActivity() as MainActivity).showFloatButton(yU < 0)
        }
    }

    /**
     * 处理列表项拖拽移动，通过交换相邻元素实现排序。
     *
     * @param fromPosition 起始位置
     * @param toPosition 目标位置
     */
    private fun onItemMove(fromPosition:Int, toPosition:Int){
        // 向后拖拽：依次向前交换
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(mAdapter.getItems(), i, i + 1)
            }
        } else {
            // 向前拖拽：依次向后交换
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(mAdapter.getItems(), i, i - 1)
            }
        }
        mAdapter.notifyItemMoved(fromPosition, toPosition)
    }

    /**
     * 设置列表项长按事件，弹出操作菜单（卸载应用等）。
     *
     * Xposed 模块类型的不可卸载，非模块类型的应用可卸载。
     */
    private fun setOnLongClick() {
        mAdapter.setItemLongClickListener { view, data, _ ->
            popupMenu = PopupMenu(requireContext(),view).also {
                it.inflate(R.menu.app_menu)
                it.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.app_remove -> {
                            if (data.isXpModule) {
                                toast(R.string.uninstall_module_toast)
                            } else {
                                unInstallApk(data)
                            }
                        }

//                        R.id.app_clear -> {
//                            clearApk(data)
//                        }

//                        R.id.app_stop -> {
//                            stopApk(data)
//                        }

//                        R.id.app_shortcut -> {
//                          ShortcutUtil.createShortcut(requireContext(), userID, data)
//                        }
                    }
                    return@setOnMenuItemClickListener true
                }
                it.show()
            }
        }
    }

    /**
     * 初始化 LiveData 观察者，监听应用列表、操作结果、启动结果和排序变更事件。
     */
    private fun initData() {
        viewBinding.stateView.showLoading()
        viewModel.getInstalledApps(userID)

        // 观察应用列表变化，更新 UI 状态
        viewModel.appsLiveData.observe(viewLifecycleOwner) {

            if (it != null) {
                mAdapter.setItems(it)
                if (it.isEmpty()) {
                    viewBinding.stateView.showEmpty()
                } else {
                    viewBinding.stateView.showContent()
                }
            }
        }

        // 观察操作结果（安装/卸载/清除），刷新列表并扫描用户
        viewModel.resultLiveData.observe(viewLifecycleOwner) {
            if (!TextUtils.isEmpty(it)) {
                hideLoading()
                requireContext().toast(it)
                viewModel.getInstalledApps(userID)
                scanUser()
            }

        }

        // 观察应用启动结果，启动失败时提示用户
        viewModel.launchLiveData.observe(viewLifecycleOwner) {
            it?.run {
                hideLoading()
                if (!it) {
                    toast(R.string.start_fail)
                }
            }
        }

        // 观察排序变更，持久化新的应用排列顺序
        viewModel.updateSortLiveData.observe(viewLifecycleOwner) {
            if (this::mAdapter.isInitialized) {
                viewModel.updateApkOrder(userID, mAdapter.getItems())
            }
        }
    }

    /**
     * Fragment 停止时清除操作结果和启动结果的 LiveData 值，避免重复处理。
     */
    override fun onStop() {
        super.onStop()
        viewModel.resultLiveData.value = null
        viewModel.launchLiveData.value = null
    }

    /**
     * 弹出卸载确认对话框，用户确认后执行卸载操作。
     *
     * @param info 待卸载应用的信息
     */
    private fun unInstallApk(info: AppInfo) {
        MaterialDialog(requireContext()).show {
            title(R.string.uninstall_app)
            message(text = getString(R.string.uninstall_app_hint, info.name))
            positiveButton(R.string.done) {
                showLoading()
                viewModel.unInstall(info.packageName, userID)
            }
            negativeButton(R.string.cancel)
        }
    }

    /**
     * 弹出清除数据确认对话框，用户确认后清除应用数据。
     *
     * @param info 目标应用的信息
     */
    private fun clearApk(info: AppInfo) {
        MaterialDialog(requireContext()).show {
            title(R.string.app_clear)
            message(text = getString(R.string.app_clear_hint,info.name))
            positiveButton(R.string.done) {
                showLoading()
                viewModel.clearApkData(info.packageName, userID)
            }
            negativeButton(R.string.cancel)
        }
    }

    /**
     * 从外部安装 APK 到当前虚拟用户空间。
     *
     * @param source APK 文件路径或来源标识
     */
    fun installApk(source: String) {
        showLoading()
        viewModel.install(source, userID)
    }

    /**
     * 通知宿主 Activity 扫描虚拟用户。
     */
    private fun scanUser() {
        (requireActivity() as MainActivity).scanUser()
    }

    /**
     * 显示加载对话框，仅当宿主 Activity 实现了 [LoadingActivity] 时生效。
     */
    private fun showLoading() {
        if(requireActivity() is LoadingActivity){
            (requireActivity() as LoadingActivity).showLoading()
        }
    }

    /**
     * 隐藏加载对话框，仅当宿主 Activity 实现了 [LoadingActivity] 时生效。
     */
    private fun hideLoading() {
        if(requireActivity() is LoadingActivity){
            (requireActivity() as LoadingActivity).hideLoading()
        }
    }

    /**
     * Fragment 的伴生对象，提供工厂方法创建带参数的实例。
     */
    companion object{
        /**
         * 创建指定虚拟用户 ID 的 [AppsFragment] 实例。
         *
         * @param userID 虚拟用户 ID
         * @return 绑定了用户 ID 参数的 [AppsFragment] 实例
         */
        fun newInstance(userID:Int): AppsFragment {
            val fragment = AppsFragment()
            val bundle = bundleOf("userID" to userID)
            fragment.arguments = bundle
            return fragment
        }
    }



}
