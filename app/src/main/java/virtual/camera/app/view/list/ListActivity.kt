package virtual.camera.app.view.list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cbfg.rvadapter.RVAdapter
import com.ferfalk.simplesearchview.SimpleSearchView
import virtual.camera.app.R
import virtual.camera.app.bean.InstalledAppBean
import virtual.camera.app.databinding.ActivityListBinding
import virtual.camera.app.util.InjectionUtil
import virtual.camera.app.util.inflate
import virtual.camera.app.view.base.BaseActivity


/**
 * 已安装应用/模块列表界面。
 *
 * 用于展示设备上已安装的应用或 Xposed 模块列表，
 * 支持按名称和包名进行搜索过滤。
 * 用户选择某个应用后，将包名作为结果返回给调用方。
 *
 * 通过 Intent 参数 "onlyShowXp" 控制显示模式：
 * - true: 显示已安装的 Xposed 模块
 * - false: 显示指定用户空间的已安装应用
 *
 * 通过 Intent 参数 "userID" 指定目标用户空间 ID。
 */
class ListActivity : BaseActivity() {

    private val viewBinding: ActivityListBinding by inflate()

    private lateinit var mAdapter: RVAdapter<InstalledAppBean>

    private lateinit var viewModel: ListViewModel

    /** 当前应用列表数据，用于搜索过滤的基准数据源 */
    private var appList: List<InstalledAppBean> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.installed_app, true)

        mAdapter = RVAdapter<InstalledAppBean>(this,ListAdapter()).bind(viewBinding.recyclerView).setItemClickListener { _, item, _ ->
            finishWithResult(item.packageName)
        }

        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)


        initSearchView()
        initViewModel()
    }

    /**
     * 初始化搜索视图，监听搜索文本变化以实时过滤列表。
     */
    private fun initSearchView() {
        viewBinding.searchView.setOnQueryTextListener(object : SimpleSearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                filterApp(newText)
                return true
            }

            override fun onQueryTextCleared(): Boolean {
                return true
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

        })
    }

    /**
     * 初始化 ViewModel，根据 Intent 参数决定加载应用列表或模块列表。
     *
     * 设置加载状态观察者和数据观察者，数据到达后自动刷新列表。
     */
    private fun initViewModel() {
        viewModel = ViewModelProvider(this, InjectionUtil.getListFactory()).get(ListViewModel::class.java)
        val onlyShowXp = intent.getBooleanExtra("onlyShowXp", false)
        val userID = intent.getIntExtra("userID",0)

        if (onlyShowXp) {
            viewModel.getInstalledModules()
            viewBinding.toolbarLayout.toolbar.setTitle(R.string.installed_module)
        } else {
            viewModel.getInstallAppList(userID)
            viewBinding.toolbarLayout.toolbar.setTitle(R.string.installed_app)
        }

        viewModel.loadingLiveData.observe(this) {
            if (it) {
                viewBinding.stateView.showLoading()
            } else {
                viewBinding.stateView.showContent()

            }
        }

        viewModel.appsLiveData.observe(this) {
            if (it != null) {
                this.appList = it
                viewBinding.searchView.setQuery("", false)
                filterApp("")
                if (it.isNotEmpty()) {
                    viewBinding.stateView.showContent()
                    viewModel.previewInstalledList()
                } else {
                    viewBinding.stateView.showEmpty()
                }
            }
        }
    }

    /**
     * 根据关键字过滤应用列表，匹配应用名称或包名（不区分大小写）。
     *
     * @param newText 搜索关键字文本
     */
    private fun filterApp(newText: String) {
        val newList = this.appList.filter {
            it.name.contains(newText, true) or it.packageName.contains(newText, true)
        }
        mAdapter.setItems(newList)
    }

    /** 文件选择结果回调，用于从文件管理器选取 APK 文件 */
    private val openDocumentedResult = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.run {
            finishWithResult(it.toString())
        }
    }

    /**
     * 将选中的资源路径作为结果返回给调用方，并关闭当前页面。
     *
     * @param source 选中的应用包名或文件路径
     */
    private fun finishWithResult(source: String) {
        intent.putExtra("source", source)
        setResult(Activity.RESULT_OK, intent)
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        window.peekDecorView()?.run {
            imm.hideSoftInputFromWindow(windowToken, 0)
        }
        finish()
    }

    /**
     * 拦截返回键：搜索栏打开时先关闭搜索，否则执行默认返回逻辑。
     */
    override fun onBackPressed() {
        if (viewBinding.searchView.isSearchOpen) {
            viewBinding.searchView.closeSearch()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_list, menu)
        val item = menu!!.findItem(R.id.list_search)
        viewBinding.searchView.setMenuItem(item)

        return true
    }

    override fun onStop() {
        super.onStop()
        // 清理 LiveData 观察者，避免内存泄漏
        viewModel.loadingLiveData.postValue(true)
        viewModel.loadingLiveData.removeObservers(this)
        viewModel.appsLiveData.postValue(null)
        viewModel.appsLiveData.removeObservers(this)
    }

    /**
     * 伴生对象，提供便捷的页面启动方法。
     */
    companion object{
        /**
         * 启动已安装应用/模块列表界面。
         *
         * @param context 上下文环境
         * @param onlyShowXp 是否仅显示 Xposed 模块
         */
        fun start(context: Context,onlyShowXp:Boolean){
            val intent = Intent(context,ListActivity::class.java)
            intent.putExtra("onlyShowXp",onlyShowXp)
            context.startActivity(intent)
        }
    }
}