package virtual.camera.app.view.gms

import android.view.View
import android.view.ViewGroup
import cbfg.rvadapter.RVHolder
import cbfg.rvadapter.RVHolderFactory
import virtual.camera.app.R
import virtual.camera.app.bean.GmsBean
import virtual.camera.app.databinding.ItemGmsBinding

/**
 * GMS 服务管理列表的 ViewHolder 工厂类。
 *
 * 负责创建 [GmsVH] 实例，用于在 RecyclerView 中展示每个用户空间的 GMS 安装状态，
 * 包含用户名和安装开关。
 */
class GmsAdapter : RVHolderFactory() {

    /**
     * 创建 GMS 列表项的 ViewHolder。
     *
     * @param parent 父 ViewGroup
     * @param viewType 视图类型
     * @param item 当前绑定的数据对象
     * @return [GmsVH] 视图持有者实例
     */
    override fun createViewHolder(parent: ViewGroup?, viewType: Int, item: Any): RVHolder<out Any> {
        return GmsVH(inflate(R.layout.item_gms,parent))
    }

    /**
     * GMS 列表项的 ViewHolder，用于展示单个用户空间的 GMS 安装状态。
     *
     * 通过 [ItemGmsBinding] 绑定布局，显示用户名和 GMS 安装复选框，
     * 并处理复选框的手动切换事件以触发列表项点击。
     */
    class GmsVH(itemView:View):RVHolder<GmsBean>(itemView){

        private val binding = ItemGmsBinding.bind(itemView)

        /**
         * 绑定 GMS 数据到视图，显示用户名和安装状态。
         *
         * @param item GMS 数据实体，包含用户名和安装状态
         * @param isSelected 是否被选中
         * @param payload 局部更新的载荷数据
         */
        override fun setContent(item: GmsBean, isSelected: Boolean, payload: Any?) {
            binding.tvTitle.text = item.userName
            binding.checkbox.isChecked = item.isInstalledGms
            // 当用户手动点击复选框时，触发整个列表项的点击事件
            binding.checkbox.setOnCheckedChangeListener  { buttonView, _ ->
                if(buttonView.isPressed){
                    binding.root.performClick()
                }
            }
        }
    }
}