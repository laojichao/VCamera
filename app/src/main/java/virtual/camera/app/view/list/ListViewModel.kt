package virtual.camera.app.view.list

import androidx.lifecycle.MutableLiveData
import virtual.camera.app.bean.InstalledAppBean
import virtual.camera.app.data.AppsRepository
import virtual.camera.app.view.base.BaseViewModel

/**
 * 已安装应用/模块列表的 ViewModel。
 *
 * 负责协调已安装应用和 Xposed 模块列表的获取操作，
 * 通过 [AppsRepository] 执行具体的业务逻辑，
 * 使用 LiveData 将加载状态和列表数据通知给 UI 层。
 *
 * @param repo 已安装应用数据仓库
 */
class ListViewModel(private val repo: AppsRepository) : BaseViewModel() {

    /** 已安装应用/模块列表数据 */
    val appsLiveData = MutableLiveData<List<InstalledAppBean>>()

    /** 列表加载状态，true 表示正在加载 */
    val loadingLiveData = MutableLiveData<Boolean>()

    /**
     * 预加载已安装应用列表，用于后台缓存。
     *
     * 不直接更新 UI，仅触发仓库的预加载逻辑。
     */
    fun previewInstalledList() {
        launchOnUI{
            repo.previewInstallList()
        }
    }

    /**
     * 获取指定用户空间的已安装应用列表。
     *
     * @param userID 目标用户空间 ID
     */
    fun getInstallAppList(userID:Int){
        launchOnUI {
            repo.getInstalledAppList(userID,loadingLiveData,appsLiveData)
        }
    }

    /**
     * 获取设备上已安装的 Xposed 模块列表。
     */
    fun getInstalledModules() {
        launchOnUI {
            repo.getInstalledModuleList(loadingLiveData, appsLiveData)
        }
    }

}