package virtual.camera.app.data

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import com.hack.opensdk.HackApi
import virtual.camera.app.R
import virtual.camera.app.app.App
import virtual.camera.app.app.AppManager
import virtual.camera.app.bean.AppInfo
import virtual.camera.app.bean.InstalledAppBean
import virtual.camera.app.util.AbiUtils
import virtual.camera.app.util.getString
import java.io.File


/**
 * 应用数据仓库，负责管理虚拟用户空间中应用的安装、卸载、启动和排序等操作。
 *
 * 通过 [HackApi] 与 OpenSDK 交互，实现虚拟环境下的应用生命周期管理。
 */
class AppsRepository {
    val TAG: String = "AppsRepository"
    private var mInstalledList = mutableListOf<AppInfo>()

    /**
     * 扫描宿主机已安装的非系统应用列表，缓存到 [mInstalledList] 中。
     *
     * 过滤条件：排除系统应用、排除不支持当前 ABI 的应用。
     */
    fun previewInstallList() {
        synchronized(mInstalledList) {
            val installedApplications: List<ApplicationInfo> =
                App.getContext().getPackageManager().getInstalledApplications(0)
            val installedList = mutableListOf<AppInfo>()

            for (installedApplication in installedApplications) {
                val file = File(installedApplication.sourceDir)

                // 跳过系统应用
                if ((installedApplication.flags and ApplicationInfo.FLAG_SYSTEM) != 0) continue

                // 跳过不支持当前 ABI 的应用
                if (!AbiUtils.isSupport(file)) continue

                val isXpModule = false

                val info = AppInfo(
                    installedApplication.loadLabel(App.getContext().getPackageManager()).toString(),
                    installedApplication.loadIcon(App.getContext().getPackageManager()),
                    installedApplication.packageName,
                    installedApplication.sourceDir,
                    isXpModule
                )
                installedList.add(info)
            }
            this.mInstalledList.clear()
            this.mInstalledList.addAll(installedList)
        }


    }

    /**
     * 获取宿主机应用在指定虚拟用户空间中的安装状态列表。
     *
     * @param userID 虚拟用户空间 ID
     * @param loadingLiveData 用于通知 UI 层加载状态的 LiveData
     * @param appsLiveData 用于通知 UI 层应用列表数据的 LiveData
     */
    fun getInstalledAppList(
        userID: Int,
        loadingLiveData: MutableLiveData<Boolean>,
        appsLiveData: MutableLiveData<List<InstalledAppBean>>
    ) {
        loadingLiveData.postValue(true)
        synchronized(mInstalledList) {
            Log.d(TAG, mInstalledList.joinToString(","))
            val newInstalledList = mInstalledList.map {
                var isInstalled = HackApi.getPackageInfo(it.packageName,userID,0) != null
                InstalledAppBean(
                    it.name,
                    it.icon,
                    it.packageName,
                    it.sourceDir,
                    isInstalled
                )
            }
            appsLiveData.postValue(newInstalledList)
            loadingLiveData.postValue(false)
        }
    }

    /**
     * 获取已安装的 Xposed 模块列表。
     *
     * @param loadingLiveData 用于通知 UI 层加载状态的 LiveData
     * @param appsLiveData 用于通知 UI 层模块列表数据的 LiveData
     */
    fun getInstalledModuleList(
        loadingLiveData: MutableLiveData<Boolean>,
        appsLiveData: MutableLiveData<List<InstalledAppBean>>
    ) {

        loadingLiveData.postValue(true)
        synchronized(mInstalledList) {
            val moduleList = mInstalledList.filter {
                it.isXpModule
            }.map {
                InstalledAppBean(
                    it.name,
                    it.icon,
                    it.packageName,
                    it.sourceDir,
                    false
                )
            }
            appsLiveData.postValue(moduleList)
            loadingLiveData.postValue(false)
        }

    }


    /**
     * 获取虚拟用户空间中已安装的应用列表，并按用户自定义排序。
     *
     * 从持久化的排序列表中读取排序信息，对应用进行排序后通过 LiveData 返回。
     *
     * @param userId 虚拟用户空间 ID
     * @param appsLiveData 用于通知 UI 层应用列表数据的 LiveData
     */
    fun getVmInstallList(userId: Int, appsLiveData: MutableLiveData<List<AppInfo>>) {
        // 读取用户自定义排序列表
        val sortListData =
            AppManager.mRemarkSharedPreferences.getString("AppList$userId", "")
        val sortList = sortListData?.split(",")

        var installedPkgs = HackApi.getInstalledPackages(0, userId)
        if(installedPkgs != null && installedPkgs.size > 0){
            installedPkgs.remove("com.waxmoon.ma.gp");
        }

        var applicationList = mutableListOf<ApplicationInfo>()
        installedPkgs.forEach {
            var packageInfo = HackApi.getPackageInfo(it, userId, 0)
            applicationList.add(packageInfo.applicationInfo)
        }

        val appInfoList = mutableListOf<AppInfo>()
        applicationList.also {
            if (sortList.isNullOrEmpty()) {
                return@also
            }
            // 按用户自定义顺序排序
            it.sortWith(AppsSortComparator(sortList))

        }.forEach {
            val info = AppInfo(
                it.loadLabel(App.getContext().getPackageManager()).toString(),
                it.loadIcon(App.getContext().getPackageManager()),
                it.packageName,
                it.sourceDir,
                isInstalledXpModule(it.packageName)
            )

            appInfoList.add(info)
        }


        appsLiveData.postValue(appInfoList)
    }

    /**
     * 判断指定包名是否为已安装的 Xposed 模块。
     *
     * @param packageName 应用包名
     * @return 始终返回 false，待实现
     */
    private fun isInstalledXpModule(packageName: String): Boolean {
        return false
    }


    /**
     * 在指定虚拟用户空间中安装应用。
     *
     * 安装成功后会更新排序列表，安装结果通过 [resultLiveData] 通知 UI 层。
     *
     * @param source 要安装的应用包名
     * @param userId 目标虚拟用户空间 ID
     * @param resultLiveData 用于通知 UI 层安装结果的 LiveData
     */
    fun installApk(source: String, userId: Int, resultLiveData: MutableLiveData<String>) {
        var packageName:String = source;
        val installResult =  HackApi.installPackageFromHost(packageName,userId,false)
        Log.e("11111","source:"+source+",installResult:"+installResult)
        var INSTALL_SUCCEEDED:Int = 1
        if (installResult == INSTALL_SUCCEEDED) {
            updateAppSortList(userId, packageName, true)
            resultLiveData.postValue(getString(R.string.install_success))
        } else {
            resultLiveData.postValue(getString(R.string.install_fail, "failed code:"+installResult))
        }
        scanUser()
    }

    /**
     * 从指定虚拟用户空间中卸载应用。
     *
     * @param packageName 要卸载的应用包名
     * @param userID 目标虚拟用户空间 ID
     * @param resultLiveData 用于通知 UI 层卸载结果的 LiveData
     */
    fun unInstall(packageName: String, userID: Int, resultLiveData: MutableLiveData<String>) {
        HackApi.uninstallPackage(packageName,userID)
        updateAppSortList(userID, packageName, false)
        scanUser()
        resultLiveData.postValue(getString(R.string.uninstall_success))
    }


    /**
     * 在虚拟用户空间中启动指定应用。
     *
     * @param packageName 要启动的应用包名
     * @param userId 虚拟用户空间 ID
     * @param launchLiveData 用于通知 UI 层启动结果的 LiveData，true 表示启动成功
     */
    fun launchApk(packageName: String, userId: Int, launchLiveData: MutableLiveData<Boolean>) {
        val intent: Intent = HackApi.getLaunchIntentForPackage(packageName,userId)
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        var result = HackApi.startActivity(intent, 0) == 0
        launchLiveData.postValue(result)
    }


    /**
     * 清除指定应用在虚拟用户空间中的数据。
     *
     * @param packageName 要清除数据的应用包名
     * @param userID 目标虚拟用户空间 ID
     * @param resultLiveData 用于通知 UI 层操作结果的 LiveData
     */
    fun clearApkData(packageName: String, userID: Int, resultLiveData: MutableLiveData<String>) {
        HackApi.deletePackageData(packageName,userID)
        resultLiveData.postValue(getString(R.string.clear_success))
    }

    /**
     * 倒序递归扫描用户空间。
     *
     * 如果用户空间中没有安装任何应用，则删除该用户空间及其备注信息和应用排序列表。
     * 当前已被禁用（方法入口直接返回）。
     */
    private fun scanUser() {
        if(1==1){
            return
        }
        val userList = HackApi.getAvailableUserSpace()

        if (userList.isEmpty()) {
            return
        }

        val id = userList.last()

        if (HackApi.getInstalledPackages(0, id).isEmpty()) {
            AppManager.mRemarkSharedPreferences.edit {
                remove("Remark$id")
                remove("AppList$id")
            }
            scanUser()
        }
    }


    /**
     * 更新指定用户空间的应用排序列表。
     *
     * @param userID 虚拟用户空间 ID
     * @param pkg 要添加或移除的应用包名
     * @param isAdd true 表示添加到排序列表，false 表示从排序列表中移除
     */
    private fun updateAppSortList(userID: Int, pkg: String, isAdd: Boolean) {

        val savedSortList =
            AppManager.mRemarkSharedPreferences.getString("AppList$userID", "")

        val sortList = linkedSetOf<String>()
        if (savedSortList != null) {
            sortList.addAll(savedSortList.split(","))
        }

        if (isAdd) {
            sortList.add(pkg)
        } else {
            sortList.remove(pkg)
        }

        AppManager.mRemarkSharedPreferences.edit {
            putString("AppList$userID", sortList.joinToString(","))
        }

    }

    /**
     * 保存应用排序后的包名顺序到持久化存储。
     *
     * @param userID 虚拟用户空间 ID
     * @param dataList 排序后的应用信息列表
     */
    fun updateApkOrder(userID: Int, dataList: List<AppInfo>) {
        AppManager.mRemarkSharedPreferences.edit {
            putString("AppList$userID",
                dataList.joinToString(",") { it.packageName })
        }

    }

}
