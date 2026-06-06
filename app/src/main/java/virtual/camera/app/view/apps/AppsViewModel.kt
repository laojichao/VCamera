package virtual.camera.app.view.apps

import androidx.lifecycle.MutableLiveData
import virtual.camera.app.bean.AppInfo
import virtual.camera.app.data.AppsRepository
import virtual.camera.app.view.base.BaseViewModel

/**
 * 应用管理页面的 ViewModel，负责处理应用列表的加载、安装、卸载、启动等业务逻辑。
 *
 * 通过 [AppsRepository] 执行具体的异步数据操作，并将结果通过 LiveData 通知 UI 层。
 * 所有耗时操作均在协程中通过 [BaseViewModel.launchOnUI] 异步执行。
 *
 * @property repo 应用数据仓库，提供虚拟环境中应用的增删改查操作
 */
class AppsViewModel(private val repo: AppsRepository) : BaseViewModel() {

    /** 已安装应用列表数据，UI 层观察此数据以刷新列表 */
    val appsLiveData = MutableLiveData<List<AppInfo>>()

    /** 操作结果消息（如安装/卸载结果），UI 层观察此数据以显示提示 */
    val resultLiveData = MutableLiveData<String>()

    /** 应用启动结果，true 表示启动成功，false 表示启动失败 */
    val launchLiveData = MutableLiveData<Boolean>()

    /** 利用 LiveData 只更新最后一次的特性，用来保存应用排序变更事件 */
    val updateSortLiveData = MutableLiveData<Boolean>()

    /**
     * 获取指定虚拟用户下已安装的应用列表。
     *
     * @param userId 虚拟用户 ID
     */
    fun getInstalledApps(userId: Int) {
        launchOnUI {
            repo.getVmInstallList(userId, appsLiveData)
        }
    }

    /**
     * 在指定虚拟用户空间中安装 APK 应用。
     *
     * @param source APK 文件路径或来源标识
     * @param userID 虚拟用户 ID
     */
    fun install(source: String, userID: Int) {
        launchOnUI {
            repo.installApk(source, userID, resultLiveData)
        }
    }

    /**
     * 从指定虚拟用户空间中卸载应用。
     *
     * @param packageName 待卸载应用的包名
     * @param userID 虚拟用户 ID
     */
    fun unInstall(packageName: String, userID: Int) {
        launchOnUI {
            repo.unInstall(packageName, userID, resultLiveData)
        }
    }

    /**
     * 清除指定应用在虚拟用户空间中的数据。
     *
     * @param packageName 目标应用的包名
     * @param userID 虚拟用户 ID
     */
    fun clearApkData(packageName: String,userID: Int){
        launchOnUI {
            repo.clearApkData(packageName,userID,resultLiveData)
        }
    }

    /**
     * 在指定虚拟用户空间中启动应用。
     *
     * @param packageName 待启动应用的包名
     * @param userID 虚拟用户 ID
     */
    fun launchApk(packageName: String, userID: Int) {
        launchOnUI {
            repo.launchApk(packageName, userID, launchLiveData)
        }
    }

    /**
     * 更新指定虚拟用户空间中应用的排列顺序。
     *
     * @param userID 虚拟用户 ID
     * @param dataList 按新顺序排列的应用列表
     */
    fun updateApkOrder(userID: Int,dataList:List<AppInfo>){
        launchOnUI {
            repo.updateApkOrder(userID,dataList)
        }
    }
}