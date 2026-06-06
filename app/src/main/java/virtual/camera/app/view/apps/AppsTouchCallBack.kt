package virtual.camera.app.view.apps

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


/**
 * RecyclerView 列表项拖拽回调处理器，支持上下左右四个方向的拖拽排序。
 *
 * 当用户完成拖拽移动后，通过 [onMoveBlock] 回调通知外部更新数据顺序。
 *
 * @property onMoveBlock 拖拽移动完成时的回调，参数为起始位置 [from] 和目标位置 [to]
 */
class AppsTouchCallBack(private val onMoveBlock: (from: Int, to: Int) -> Unit) :
    ItemTouchHelper.Callback() {

    /**
     * 定义列表项支持的移动方向和滑动方向。
     *
     * @param recyclerView 关联的 RecyclerView
     * @param viewHolder 当前列表项的 ViewHolder
     * @return 包含拖拽方向标志和滑动方向标志的复合标志位，支持上下左右四个方向拖拽
     */
    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0)

    }

    /**
     * 当列表项被拖拽到新位置时调用，触发数据顺序更新回调。
     *
     * @param recyclerView 关联的 RecyclerView
     * @param viewHolder 被拖拽的列表项 ViewHolder
     * @param target 拖拽目标位置的 ViewHolder
     * @return 始终返回 true，表示允许移动
     */
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val fromPosition = viewHolder.bindingAdapterPosition
        val toPosition = target.bindingAdapterPosition
        onMoveBlock(fromPosition, toPosition)
        return true
    }

    /**
     * 列表项被滑动删除时调用（当前未实现）。
     *
     * @param viewHolder 被滑动的列表项 ViewHolder
     * @param direction 滑动方向
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }
}