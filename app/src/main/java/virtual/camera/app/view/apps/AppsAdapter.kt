package virtual.camera.app.view.apps

import android.view.View
import android.view.ViewGroup
import cbfg.rvadapter.RVHolder
import cbfg.rvadapter.RVHolderFactory
import virtual.camera.app.R
import virtual.camera.app.bean.AppInfo
import virtual.camera.app.databinding.ItemAppBinding

/**
 * 应用列表适配器，负责将 [AppInfo] 数据绑定到 RecyclerView 的列表项视图上。
 *
 * 通过 [RVHolderFactory] 框架实现 ViewHolder 的创建与数据绑定，
 * 支持显示应用图标、名称以及 Xposed 模块角标标识。
 */
class AppsAdapter : RVHolderFactory() {

    /**
     * 创建应用列表项的 ViewHolder。
     *
     * @param parent 父 ViewGroup，用于解析布局参数
     * @param viewType 视图类型（当前未使用多类型）
     * @param item 列表项数据对象
     * @return 应用列表项对应的 [AppsVH] 实例
     */
    override fun createViewHolder(parent: ViewGroup?, viewType: Int, item: Any): RVHolder<out Any> {
        return AppsVH(inflate(R.layout.item_app,parent))
    }

    /**
     * 应用列表项的 ViewHolder，负责将 [AppInfo] 数据渲染到 [ItemAppBinding] 视图上。
     *
     * @property binding 列表项的 ViewBinding 实例
     */
    class AppsVH(itemView:View):RVHolder<AppInfo>(itemView){

        val binding = ItemAppBinding.bind(itemView)

        /**
         * 绑定应用数据到视图，设置图标、名称及 Xposed 模块角标可见性。
         *
         * @param item 当前列表项对应的 [AppInfo] 数据
         * @param isSelected 当前项是否被选中（未使用）
         * @param payload 局部刷新的附加数据（未使用）
         */
        override fun setContent(item: AppInfo, isSelected: Boolean, payload: Any?) {
            binding.icon.setImageDrawable(item.icon)
            binding.name.text = item.name
            if(item.isXpModule){
                binding.cornerLabel.visibility = View.VISIBLE
            }else{
                binding.cornerLabel.visibility = View.INVISIBLE
            }
        }

    }
}