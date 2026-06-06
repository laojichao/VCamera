package virtual.camera.app.view.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import virtual.camera.app.data.AppsRepository

/**
 * 应用列表模块的 ViewModel 工厂类。
 *
 * 通过依赖注入 [AppsRepository]，为 [ListViewModel] 提供构造所需的仓库实例。
 *
 * @param appsRepository 已安装应用数据仓库
 */
@Suppress("UNCHECKED_CAST")
class ListFactory(private val appsRepository: AppsRepository) : ViewModelProvider.NewInstanceFactory() {

    /**
     * 创建 [ListViewModel] 实例。
     *
     * @param modelClass 要创建的 ViewModel 类型
     * @return [ListViewModel] 实例
     * @throws ClassCastException 如果请求的类型不是 [ListViewModel]
     */
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ListViewModel(appsRepository) as T
    }
}