package virtual.camera.app.util

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * ViewBinding 扩展工具集，为 Activity、Fragment、Dialog 提供基于懒加载的 ViewBinding 绑定能力。
 *
 * 通过反射调用 ViewBinding 生成类的静态 `inflate` 方法，避免手写样板代码。
 */

/**
 * 为 Activity 创建懒加载的 [ViewBinding] 实例。
 *
 * @param T ViewBinding 实现类型
 * @return 懒加载的 [ViewBinding] 实例
 */
inline fun <reified T : ViewBinding> Activity.inflate(): Lazy<T> = lazy {
    inflateBinding(layoutInflater)
}

/**
 * 为 Fragment 创建懒加载的 [ViewBinding] 实例。
 *
 * @param T ViewBinding 实现类型
 * @return 懒加载的 [ViewBinding] 实例
 */
inline fun <reified T : ViewBinding> Fragment.inflate(): Lazy<T> = lazy {
    inflateBinding(layoutInflater)
}

/**
 * 为 Dialog 创建懒加载的 [ViewBinding] 实例。
 *
 * @param T ViewBinding 实现类型
 * @return 懒加载的 [ViewBinding] 实例
 */
inline fun <reified T : ViewBinding> Dialog.inflate(): Lazy<T> = lazy {
    inflateBinding(layoutInflater)
}


/**
 * 通过反射调用 ViewBinding 生成类的 `inflate(LayoutInflater)` 方法创建实例。
 *
 * @param T ViewBinding 实现类型
 * @param layoutInflater 布局加载器
 * @return 创建的 [ViewBinding] 实例
 */
inline fun <reified T : ViewBinding> inflateBinding(layoutInflater: LayoutInflater): T {
    val method = T::class.java.getMethod("inflate", LayoutInflater::class.java)
    return method.invoke(null, layoutInflater) as T
}

/**
 * 通过反射调用 ViewBinding 生成类的 `inflate(LayoutInflater, ViewGroup, Boolean)` 方法创建实例。
 *
 * 通常用于 RecyclerView 的 ViewHolder 中创建列表项的 ViewBinding。
 *
 * @param T ViewBinding 实现类型
 * @param viewGroup 父容器
 * @param attachToParent 是否立即附加到父容器，默认为 false
 * @return 创建的 [ViewBinding] 实例
 */
inline fun <reified T : ViewBinding> newBindingViewHolder(viewGroup: ViewGroup, attachToParent:Boolean = false): T {
    val method = T::class.java.getMethod("inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java)
    return method.invoke(null,LayoutInflater.from(viewGroup.context),viewGroup,attachToParent) as T
}
