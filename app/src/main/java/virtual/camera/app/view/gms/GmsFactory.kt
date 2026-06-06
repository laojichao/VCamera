package virtual.camera.app.view.gms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import virtual.camera.app.data.GmsRepository

/**
 * GMS 管理模块的 ViewModel 工厂类。
 *
 * 通过依赖注入 [GmsRepository]，为 [GmsViewModel] 提供构造所需的仓库实例。
 *
 * @param repo GMS 数据仓库，负责 GMS 安装/卸载的业务逻辑
 */
class GmsFactory(private val repo:GmsRepository): ViewModelProvider.NewInstanceFactory() {

    /**
     * 创建 [GmsViewModel] 实例。
     *
     * @param modelClass 要创建的 ViewModel 类型
     * @return [GmsViewModel] 实例
     * @throws ClassCastException 如果请求的类型不是 [GmsViewModel]
     */
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return GmsViewModel(repo) as T
    }
}