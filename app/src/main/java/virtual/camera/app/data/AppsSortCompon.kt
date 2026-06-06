package virtual.camera.app.data

import android.content.pm.ApplicationInfo

/**
 * 应用排序比较器，根据预设的包名排序列表对 [ApplicationInfo] 进行排序。
 *
 * 排序依据为包名在 [sortedList] 中的索引位置，未找到的包名索引为 -1。
 *
 * @property sortedList 预设的包名排序列表
 */
class AppsSortComparator(private val sortedList: List<String>) : Comparator<ApplicationInfo> {

    /**
     * 比较两个 [ApplicationInfo] 在排序列表中的位置。
     *
     * @param o1 第一个应用信息，为 null 时视为相等
     * @param o2 第二个应用信息，为 null 时视为相等
     * @return 负值表示 o1 排在 o2 前面，0 表示相等，正值表示 o1 排在 o2 后面
     */
    override fun compare(o1: ApplicationInfo?, o2: ApplicationInfo?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }

        val first = sortedList.indexOf(o1.packageName)
        val second = sortedList.indexOf(o2.packageName)
        return first - second

    }
}