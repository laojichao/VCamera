package virtual.camera.app.view.gms

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cbfg.rvadapter.RVAdapter
import com.afollestad.materialdialogs.MaterialDialog
import virtual.camera.app.R
import virtual.camera.app.bean.GmsBean
import virtual.camera.app.databinding.ActivityGmsBinding
import virtual.camera.app.util.InjectionUtil
import virtual.camera.app.util.inflate
import virtual.camera.app.util.toast
import virtual.camera.app.view.base.LoadingActivity

/**
 * GMS 服务管理界面。
 *
 * 提供对各用户空间 Google Mobile Services (GMS) 的安装和卸载管理功能。
 * 通过 RecyclerView 展示所有用户空间的 GMS 安装状态，
 * 支持一键启用/禁用指定用户空间的 GMS 服务。
 *
 * 继承自 [LoadingActivity] 以支持加载状态显示。
 */
class GmsManagerActivity : LoadingActivity() {

    private lateinit var viewModel: GmsViewModel

    private lateinit var mAdapter: RVAdapter<GmsBean>

    private val viewBinding: ActivityGmsBinding by inflate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.gms_manager, true)
        initViewModel()

        initRecyclerView()
    }

    /**
     * 初始化 ViewModel，设置 LiveData 观察者以响应数据变化。
     *
     * 监听 [GmsViewModel.mInstalledLiveData] 更新列表数据，
     * 监听 [GmsViewModel.mUpdateInstalledLiveData] 处理安装/卸载结果。
     */
    private fun initViewModel() {
        viewModel = ViewModelProvider(this, InjectionUtil.getGmsFactory())[GmsViewModel::class.java]
        showLoading()

        viewModel.mInstalledLiveData.observe(this) {
            hideLoading()
            mAdapter.setItems(it)
        }

        viewModel.mUpdateInstalledLiveData.observe(this) { result ->
            if (result == null) {
                return@observe
            }

            // 根据返回结果更新对应用户项的安装状态
            val items = mAdapter.getItems()
            for (index in items.indices) {
                val bean = items[index]
                if (bean.userID == result.userID) {
                    if (result.success) {
                        bean.isInstalledGms = !bean.isInstalledGms
                    }
                    mAdapter.replaceAt( index,bean)
                    break
                }
            }

            hideLoading()

            if (result.success) {
                toast(result.msg)
            } else {
                // 操作失败时弹出对话框显示错误信息
                MaterialDialog(this).show {
                    title(R.string.gms_manager)
                    message(text = result.msg)
                    positiveButton(R.string.done)
                }
            }
        }

        viewModel.getInstalledUser()
    }

    /**
     * 初始化 RecyclerView，设置列表适配器和点击事件。
     *
     * 点击列表项时根据当前安装状态触发安装或卸载 GMS 操作。
     */
    private fun initRecyclerView() {
        mAdapter = RVAdapter<GmsBean>(this, GmsAdapter()).bind(viewBinding.recyclerView)
            .setItemClickListener { view, item, _ ->
                val checkbox = view.findViewById<Switch>(R.id.checkbox)
                if (item.isInstalledGms) {
                    uninstallGms(item.userID, checkbox)
                } else {
                    installGms(item.userID, checkbox)
                }
            }
        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)

    }

    /**
     * 弹出确认对话框，确认后为指定用户空间安装 GMS。
     *
     * @param userID 目标用户空间 ID
     * @param checkbox 对应的开关控件，用于取消操作时恢复状态
     */
    private fun installGms(userID: Int, checkbox: Switch){
        MaterialDialog(this).show {
            title(R.string.enable_gms)
            message(R.string.enable_gms_hint)
            positiveButton(R.string.done){
                showLoading()
                viewModel.installGms(userID)
            }
            negativeButton(R.string.cancel){
                checkbox.isChecked = !checkbox.isChecked
            }
        }
    }

    /**
     * 弹出确认对话框，确认后为指定用户空间卸载 GMS。
     *
     * @param userID 目标用户空间 ID
     * @param checkbox 对应的开关控件，用于取消操作时恢复状态
     */
    private fun uninstallGms(userID: Int, checkbox: Switch){
        MaterialDialog(this).show {
            title(R.string.disable_gms)
            message(R.string.disable_gms_hint)
            positiveButton(R.string.done){
                showLoading()
                viewModel.uninstallGms(userID)
            }
            negativeButton(R.string.cancel){
                checkbox.isChecked = !checkbox.isChecked
            }
        }
    }

    /**
     * 伴生对象，提供便捷的页面启动方法。
     */
    companion object{
        /**
         * 启动 GMS 管理界面。
         *
         * @param context 上下文环境
         */
        fun start(context: Context){
            val intent = Intent(context,GmsManagerActivity::class.java)
            context.startActivity(intent)
        }
    }
}