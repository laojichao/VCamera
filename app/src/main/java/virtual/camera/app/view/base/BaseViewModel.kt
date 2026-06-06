package virtual.camera.app.view.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

/**
 * ViewModel 基类，提供协程作用域管理和 IO 线程切换的通用能力。
 *
 * 所有 ViewModel 均应继承此类，通过 [launchOnUI] 在 IO 线程执行耗时操作并自动捕获异常。
 * ViewModel 销毁时自动取消所有未完成的协程任务。
 */
open class BaseViewModel : ViewModel() {

    /**
     * 在 IO 调度器上执行挂起代码块，自动捕获并打印异常。
     *
     * 该方法使用 [viewModelScope] 启动协程，并通过 [withContext] 切换到 [Dispatchers.IO] 线程，
     * 所有异常通过 [Throwable.printStackTrace] 记录，不会向上抛出。
     *
     * @param block 需要在 IO 线程执行的挂起代码块
     */
    fun launchOnUI(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    block()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }

            }
        }
    }

    /**
     * ViewModel 销毁时取消关联的 [viewModelScope]，释放所有协程资源。
     */
    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }

}