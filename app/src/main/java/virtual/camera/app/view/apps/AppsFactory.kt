package virtual.camera.app.view.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import virtual.camera.app.data.AppsRepository

/**
 * [AppsViewModel] 的工厂类，用于向 ViewModel 注入 [AppsRepository] 依赖。
 *
 * 继承自 [ViewModelProvider.NewInstanceFactory]，在创建 ViewModel 时
 * 将 [AppsRepository] 实例传递给 [AppsViewModel] 的构造函数。
 *
 * @property appsRepository 应用数据仓库实例，提供应用安装、卸载等数据操作
 */
@Suppress("UNCHECKED_CAST")
class AppsFactory(private val appsRepository: AppsRepository) : ViewModelProvider.NewInstanceFactory() {

    /**
     * 创建 [AppsViewModel] 实例，并注入 [AppsRepository] 依赖。
     *
     * @param modelClass 请求创建的 ViewModel 类型
     * @return 已注入依赖的 [AppsViewModel] 实例
     * @throws ClassCastException 如果请求的类型不是 [AppsViewModel]
     */
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AppsViewModel(appsRepository) as T
    }
}