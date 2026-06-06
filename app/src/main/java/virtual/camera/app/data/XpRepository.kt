package virtual.camera.app.data

import androidx.lifecycle.MutableLiveData
import virtual.camera.app.bean.XpModuleInfo

/**
 * Xposed 模块数据仓库，负责管理 Xposed 模块的查询、安装和卸载操作。
 */
class XpRepository {

    /**
     * 获取已安装的 Xposed 模块列表。
     *
     * @param modulesLiveData 用于通知 UI 层模块列表变化的 LiveData
     */
    fun getInstallModules(modulesLiveData: MutableLiveData<List<XpModuleInfo>>) {
    }

    /**
     * 安装指定的 Xposed 模块。
     *
     * @param source 模块安装源（包名或路径）
     * @param resultLiveData 用于通知 UI 层安装结果的 LiveData
     */
    fun installModule(source: String, resultLiveData: MutableLiveData<String>) {

    }

    /**
     * 卸载指定的 Xposed 模块。
     *
     * @param packageName 要卸载的模块包名
     * @param resultLiveData 用于通知 UI 层卸载结果的 LiveData
     */
    fun unInstallModule(packageName: String, resultLiveData: MutableLiveData<String>) {

    }
}