package virtual.camera.app.view.list

import android.view.View
import android.view.ViewGroup
import cbfg.rvadapter.RVHolder
import cbfg.rvadapter.RVHolderFactory
import virtual.camera.app.R
import virtual.camera.app.bean.InstalledAppBean
import virtual.camera.app.databinding.ItemPackageBinding

/**
 * 已安装应用/模块列表的 ViewHolder 工厂类。
 *
 * 负责创建 [ListVH] 实例，用于在 RecyclerView 中展示已安装应用或模块的
 * 图标、名称、包名以及安装状态角标。
 */
class ListAdapter : RVHolderFactory() {

    /**
     * 创建已安装应用列表项的 ViewHolder。
     *
     * @param parent 父 ViewGroup
     * @param viewType 视图类型
     * @param item 当前绑定的数据对象
     * @return [ListVH] 视图持有者实例
     */
    override fun createViewHolder(parent: ViewGroup?, viewType: Int, item: Any): RVHolder<out Any> {
        return ListVH(inflate(R.layout.item_package,parent))
    }

    /**
     * 已安装应用列表项的 ViewHolder。
     *
     * 通过 [ItemPackageBinding] 绑定布局，展示应用图标、名称、包名，
     * 以及是否已安装在当前用户空间的角标标识。
     */
    class ListVH(itemView:View) :RVHolder<InstalledAppBean>(itemView){

        val binding = ItemPackageBinding.bind(itemView)

        /**
         * 绑定应用数据到视图。
         *
         * @param item 已安装应用数据实体，包含图标、名称、包名和安装状态
         * @param isSelected 是否被选中
         * @param payload 局部更新的载荷数据
         */
        override fun setContent(item: InstalledAppBean, isSelected: Boolean, payload: Any?) {
            binding.icon.setImageDrawable(item.icon)
            binding.name.text = item.name
            binding.packageName.text = item.packageName
            binding.cornerLabel.visibility = if (item.isInstall) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }
}