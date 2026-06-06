package virtual.camera.app.widget

import android.content.Context
import android.view.MotionEvent
import com.imuxuan.floatingview.FloatingMagnetView
import virtual.camera.app.R

/**
 * 悬浮摇杆控件的父容器视图。
 *
 * 继承自 [FloatingMagnetView]，提供可拖拽的悬浮窗口功能。
 * 内部嵌入 [RockerView] 摇杆控件，用户可通过摇杆操控虚拟定位的方向和速度。
 *
 * 触摸事件处理逻辑：
 * - 按下时禁止摇杆随手指移动（固定摇杆位置以便操控方向）
 * - 抬起时恢复摇杆可移动状态（允许拖拽悬浮窗）
 *
 * @param mContext 上下文环境
 */
class EnFloatView(mContext: Context) : FloatingMagnetView(mContext) {

    private val TAG = "RockerManager"

    private var rockerView: RockerView? = null

    /** 摇杆方向和距离变化的回调监听器 */
    private var mListener: LocationListener? = null

    init {
        inflate(mContext, R.layout.view_float_rocker, this)
        initRockerView()
    }

    /**
     * 初始化摇杆视图，设置摇杆事件监听器。
     *
     * 当摇杆处于时钟方向事件且角度有效时，
     * 将距离按比例缩小后通过 [mListener] 回调通知外部。
     */
    private fun initRockerView() {

        rockerView = findViewById(R.id.rocker)
        rockerView?.setListener { type, currentAngle, currentDistance ->
            if (type == RockerView.EVENT_CLOCK && currentAngle != -1F) {
                val realAngle = currentAngle
                // 将距离值缩小为 0.001 倍，拉满摇杆时大约对应每秒五米的移动速度
                val realDistance = currentDistance * 0.001F
                //拉满的话，大概就是一秒五米

                mListener?.invoke(realAngle, realDistance)

            }
        }
    }

    /**
     * 处理触摸事件，控制摇杆的可移动状态。
     *
     * - ACTION_DOWN: 禁止摇杆随手指移动，以便精确控制方向
     * - ACTION_UP: 恢复摇杆可移动状态
     *
     * @param event 触摸事件
     * @return 是否消费该事件
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            rockerView?.setCanMove(false)
        } else if (event?.action == MotionEvent.ACTION_UP) {
            rockerView?.setCanMove(true)
        }
        return super.onTouchEvent(event)
    }

    /**
     * 设置摇杆方向和距离变化的回调监听器。
     *
     * @param listener 摇杆事件监听器，接收角度和距离参数
     */
    fun setListener(listener: LocationListener) {
        this.mListener = listener
    }

}

/**
 * 虚拟定位摇杆事件的回调类型别名。
 *
 * @param angle 摇杆指向的角度（0-360度）
 * @param distance 摇杆偏移的距离值（已按比例缩小）
 */
typealias LocationListener = (angle: Float, distance: Float) -> Unit