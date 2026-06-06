package virtual.camera.app.data

import androidx.lifecycle.MutableLiveData
import com.hack.opensdk.HackApi
import virtual.camera.app.R
import virtual.camera.app.app.AppManager
import virtual.camera.app.bean.GmsBean
import virtual.camera.app.bean.GmsInstallBean
import virtual.camera.app.util.getString

/**
 * Google 服务（GMS）数据仓库，负责管理虚拟用户空间中 GMS 的安装与卸载。
 */
class GmsRepository {

    /**
     * 获取各用户空间的 GMS 安装状态列表。
     *
     * @param mInstalledLiveData 用于通知 UI 层 GMS 安装状态列表变化的 LiveData
     */
    fun getGmsInstalledList(mInstalledLiveData: MutableLiveData<List<GmsBean>>) {

    }

    /**
     * 在指定用户空间中安装 Google 服务套件。
     *
     * @param userID 目标虚拟用户空间 ID
     * @param mUpdateInstalledLiveData 用于通知 UI 层安装结果的 LiveData
     */
    fun installGms(
        userID: Int,
        mUpdateInstalledLiveData: MutableLiveData<GmsInstallBean>
    ) {

    }

    /**
     * 从指定用户空间中卸载 Google 服务套件。
     *
     * @param userID 目标虚拟用户空间 ID
     * @param mUpdateInstalledLiveData 用于通知 UI 层卸载结果的 LiveData
     */
    fun uninstallGms(
        userID: Int,
        mUpdateInstalledLiveData: MutableLiveData<GmsInstallBean>
    ) {

    }
}