package virtual.camera.app.view.main

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import virtual.camera.app.view.apps.AppsFragment

/**
 * ViewPager2 的 Fragment 状态适配器。
 *
 * 管理多用户空间对应的 [AppsFragment] 列表，
 * 支持通过 [replaceData] 动态替换数据源并刷新页面。
 *
 * @param appCompatActivity 宿主 Activity，用于 Fragment 生命周期管理
 */
class ViewPagerAdapter(appCompatActivity: AppCompatActivity) : FragmentStateAdapter(appCompatActivity) {

    private var fragmentList = mutableListOf<AppsFragment>()

    /**
     * 替换整个 Fragment 数据列表并刷新适配器。
     *
     * @param list 新的 Fragment 列表
     */
    fun replaceData(list: MutableList<AppsFragment>){
        this.fragmentList = list
        notifyDataSetChanged()
    }

    /**
     * 获取 Fragment 总数量。
     *
     * @return Fragment 列表大小
     */
    override fun getItemCount(): Int {
        return fragmentList.size
    }

    /**
     * 根据位置创建对应的 Fragment 实例。
     *
     * @param position 目标页面位置索引
     * @return 对应位置的 [AppsFragment]
     */
    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }

}