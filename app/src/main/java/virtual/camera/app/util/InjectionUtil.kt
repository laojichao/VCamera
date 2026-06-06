package virtual.camera.app.util

import virtual.camera.app.data.AppsRepository
import virtual.camera.app.data.GmsRepository
import virtual.camera.app.data.XpRepository
import virtual.camera.app.view.apps.AppsFactory
import virtual.camera.app.view.gms.GmsFactory
import virtual.camera.app.view.list.ListFactory

/**
 * 依赖注入工具单例，提供 ViewModel 工厂实例的创建方法。
 *
 * 作为简易的依赖注入容器，管理 [AppsRepository]、[XpRepository]、[GRepository]
 * 三个数据仓库的单例，并为各 ViewModel 工厂提供所需的仓库实例。
 */
object InjectionUtil {

    private val appsRepository = AppsRepository()

    private val xpRepository = XpRepository()

    private val gmsRepository = GmsRepository()


    /**
     * 获取应用管理相关的 ViewModel 工厂。
     *
     * @return [AppsFactory] 实例，内部持有 [AppsRepository]
     */
    fun getAppsFactory() : AppsFactory {
        return AppsFactory(appsRepository)
    }

    /**
     * 获取应用列表相关的 ViewModel 工厂。
     *
     * @return [ListFactory] 实例，内部持有 [AppsRepository]
     */
    fun getListFactory(): ListFactory {
        return ListFactory(appsRepository)
    }

    /**
     * 获取 GMS 管理相关的 ViewModel 工厂。
     *
     * @return [GmsFactory] 实例，内部持有 [GmsRepository]
     */
    fun getGmsFactory():GmsFactory{
        return GmsFactory(gmsRepository)
    }
}