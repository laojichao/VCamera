package virtual.camera.app.view.gms

import androidx.lifecycle.MutableLiveData
import virtual.camera.app.bean.GmsBean
import virtual.camera.app.bean.GmsInstallBean
import virtual.camera.app.data.GmsRepository
import virtual.camera.app.view.base.BaseViewModel

/**
 * GMS 管理模块的 ViewModel。
 *
 * 负责协调 GMS 服务的查询、安装和卸载操作，
 * 通过 [GmsRepository] 执行具体的业务逻辑，
 * 使用 LiveData 将结果通知给 UI 层。
 *
 * @param mRepo GMS 数据仓库实例
 */
class GmsViewModel(private val mRepo: GmsRepository) : BaseViewModel() {

    /** 所有用户空间的 GMS 安装状态列表 */
    val mInstalledLiveData = MutableLiveData<List<GmsBean>>()

    /** GMS 安装/卸载操作的结果 */
    val mUpdateInstalledLiveData = MutableLiveData<GmsInstallBean>()

    /**
     * 获取所有用户空间的 GMS 安装状态列表。
     *
     * 通过协程在 UI 线程安全地调用仓库方法，结果通过 [mInstalledLiveData] 回传。
     */
    fun getInstalledUser() {
        launchOnUI {
            mRepo.getGmsInstalledList(mInstalledLiveData)
        }
    }

    /**
     * 为指定用户空间安装 GMS 服务。
     *
     * @param userID 目标用户空间 ID
     */
    fun installGms(userID: Int) {
        launchOnUI {
            mRepo.installGms(userID,mUpdateInstalledLiveData)
        }
    }

    /**
     * 为指定用户空间卸载 GMS 服务。
     *
     * @param userID 目标用户空间 ID
     */
    fun uninstallGms(userID: Int) {
        launchOnUI {
            mRepo.uninstallGms(userID,mUpdateInstalledLiveData)
        }
    }
}