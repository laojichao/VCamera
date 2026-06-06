package virtual.camera.app.bean

/**
 * Google 服务（GMS）状态数据类，表示某个用户空间的 GMS 安装状态。
 *
 * @property userID 虚拟用户空间 ID
 * @property userName 用户空间名称
 * @property isInstalledGms 该用户空间是否已安装 GMS
 */
data class GmsBean(val userID:Int,val userName:String,var isInstalledGms:Boolean)

/**
 * GMS 安装/卸载操作结果数据类。
 *
 * @property userID 虚拟用户空间 ID
 * @property success 操作是否成功
 * @property msg 操作结果提示信息
 */
data class GmsInstallBean(val userID: Int,val success:Boolean,val msg:String)