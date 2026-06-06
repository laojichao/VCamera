package virtual.camera.app.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import virtual.camera.app.util.MathUtil;


/**
 * 自定义摇杆控件（Joystick View）。
 * <p>
 * 基于 {@link SurfaceView} 实现的虚拟摇杆组件，常用于游戏或远程控制场景。
 * 支持自定义活动区域半径、摇杆半径、颜色或位图等外观属性，
 * 通过 {@link RockerListener} 接口实时回调摇杆的角度和距离信息。
 * </p>
 * <p>
 * 采用双线程架构：
 * <ul>
 *   <li>绘制线程 - 按 {@link #mRefreshCycle} 周期刷新画面</li>
 *   <li>回调线程 - 按 {@link #mCallbackCycle} 周期触发监听器回调</li>
 * </ul>
 * </p>
 *
 * @author GcsSloop
 * @see <a href="https://github.com/GcsSloop">GitHub 主页</a>
 */
public class RockerView extends SurfaceView implements Runnable, SurfaceHolder.Callback {

    private static final int DEFAULT_AREA_RADIUS = 100;
    private static final int DEFAULT_ROCKER_RADIUS = 35;

    private static final int DEFAULT_AREA_COLOR = Color.argb(128,0,0,0);
    private static final int DEFAULT_ROCKER_COLOR = Color.argb(128,0,0,0);

    private static final int DEFAULT_REFRESH_CYCLE = 30;
    private static final int DEFAULT_CALLBACK_CYCLE = 300;

    private SurfaceHolder mHolder;
    private static Thread mDrawThread;
    private static Thread mCallbackThread;
    private static boolean mDrawOk = true;
    private static boolean mCallbackOk = true;

    private Paint mPaint;

    /**
     * The rocker active area center position.
     * usually, it is the center of this view.
     */
    private Point mAreaPosition;

    /**
     * The Rocker position.
     * usually, it as same asmAreaPosition .
     * if this view touched, it will follow the touch position.
     * <p/>
     * we get position information from this.
     */
    private Point mRockerPosition;


    private int mAreaRadius = -1;
    private int mRockerRadius = -1;

    private int mAreaColor;
    private int mRockerColor;
    private Bitmap mAreaBitmap;
    private Bitmap mRockerBitmap;

    private boolean canMove = true;


    private RockerListener mListener;
    /** 摇杆事件类型：触摸操作触发的回调 */
    public static final int EVENT_ACTION = 1;

    /** 摇杆事件类型：定时轮询触发的回调 */
    public static final int EVENT_CLOCK = 2;

    private int mRefreshCycle = DEFAULT_REFRESH_CYCLE;
    private int mCallbackCycle = DEFAULT_CALLBACK_CYCLE;


    /*Life Cycle***********************************************************************************/

    /**
     * 使用默认属性的构造方法。
     *
     * @param context 上下文对象
     */
    public RockerView(Context context) {
        this(context, null);
    }

    /**
     * 从 XML 布局加载时使用的构造方法。
     *
     * @param context  上下文对象
     * @param attrs    XML 属性集合
     */
    public RockerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * 完整参数构造方法。
     * <p>
     * 初始化自定义属性、画笔，并配置 SurfaceView 和 SurfaceHolder。
     * </p>
     *
     * @param context      上下文对象
     * @param attrs        XML 属性集合
     * @param defStyleAttr 默认样式属性
     */
    public RockerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // init attrs
        initAttrs(context, attrs);

        // set paint
        setPaint();

        if (isInEditMode()) {
            return;
        }

        // config surfaceView
        configSurfaceView();

        // config surfaceHolder
        configSurfaceHolder();
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        mAreaColor = DEFAULT_AREA_COLOR;
        mRockerColor = DEFAULT_ROCKER_COLOR;
        mAreaRadius = DEFAULT_AREA_RADIUS;
        mRockerRadius = DEFAULT_ROCKER_RADIUS;

    }

    private void setPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    private void configSurfaceView() {
        setKeepScreenOn(true);          // do not lock screen when surfaceView is running.
        setFocusable(true);             // make sure this surfaceView can get focus from keyboard.
        setFocusableInTouchMode(true);  // make sure this surfaceView can get focus from touch.
        setZOrderOnTop(true);           // make sure this surface is placed on top of the window
    }

    private void configSurfaceHolder() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setFormat(PixelFormat.TRANSPARENT); //设置背景透明
    }

    /**
     * 测量控件尺寸。
     * <p>
     * 默认尺寸为 {@code (活动区域半径 + 摇杆半径) * 2} 的正方形，
     * 若父容器指定了确切尺寸或最大尺寸，则使用父容器约束值。
     * </p>
     *
     * @param widthMeasureSpec  宽度测量规格
     * @param heightMeasureSpec 高度测量规格
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = 0, measureHeight = 0;
        int defaultWidth = (mAreaRadius + mRockerRadius) * 2;
        int defalutHeight = defaultWidth;

        int widthsize = MeasureSpec.getSize(widthMeasureSpec);      //取出宽度的确切数值
        int widthmode = MeasureSpec.getMode(widthMeasureSpec);      //取出宽度的测量模式

        int heightsize = MeasureSpec.getSize(heightMeasureSpec);    //取出高度的确切数值
        int heightmode = MeasureSpec.getMode(heightMeasureSpec);    //取出高度的测量模式

        if (widthmode == MeasureSpec.AT_MOST || widthmode == MeasureSpec.UNSPECIFIED || widthsize < 0) {
            measureWidth = defaultWidth;
        } else {
            measureWidth = widthsize;
        }


        if (heightmode == MeasureSpec.AT_MOST || heightmode == MeasureSpec.UNSPECIFIED || heightsize < 0) {
            measureHeight = defalutHeight;
        } else {
            measureHeight = heightsize;
        }

        setMeasuredDimension(measureWidth, measureHeight);
    }

    /**
     * 控件尺寸变化时回调，重新计算活动区域中心和半径。
     * <p>
     * 活动区域中心默认设为控件中心；若未通过 {@link #setAreaRadius(int)}
     * 或 {@link #setRockerRadius(int)} 手动设置半径，则自动按 75% / 25% 的
     * 比例分配活动区域和摇杆的半径。
     * </p>
     *
     * @param w    新的宽度
     * @param h    新的高度
     * @param oldw 旧的宽度
     * @param oldh 旧的高度
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mAreaPosition = new Point(w / 2, h / 2);
        mRockerPosition = new Point(mAreaPosition);

        // this need subtract the view padding
        int tempRadius = Math.min(w - getPaddingLeft() - getPaddingRight(), h - getPaddingTop() - getPaddingBottom());
        tempRadius /= 2;
        if (mAreaRadius == -1)
            mAreaRadius = (int) (tempRadius * 0.75);
        if (mRockerRadius == -1)
            mRockerRadius = (int) (tempRadius * 0.25);
    }

    /**
     * Surface 创建完成时回调，启动绘制线程和回调线程。
     *
     * @param holder 关联的 SurfaceHolder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mDrawThread = new Thread(this);
            mDrawThread.start();

            mCallbackThread = new Thread(() -> {
                while (mCallbackOk) {

                    // listener callback
                    listenerCallback();

                    try {
                        Thread.sleep(mCallbackCycle);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            mCallbackThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Surface 尺寸变化时回调（本实现中无额外操作）。
     *
     * @param holder 关联的 SurfaceHolder
     * @param format 像素格式
     * @param width  新的宽度
     * @param height 新的高度
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * Surface 销毁时回调，停止绘制线程和回调线程。
     *
     * @param holder 关联的 SurfaceHolder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mDrawOk = false;
        mCallbackOk = false;
    }

    /**
     * 控件可见性变化时回调，根据可见状态启停线程。
     *
     * @param changedView 发生变化的视图
     * @param visibility  新的可见性状态
     */
    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            mDrawOk = true;
            mCallbackOk = true;
        } else {
            mDrawOk = false;
            mCallbackOk = false;
        }
    }

    /*Event Response*******************************************************************************/

    /**
     * 处理触摸事件，驱动摇杆位置更新。
     * <p>
     * 触摸事件处理逻辑：
     * <ul>
     *   <li>{@code ACTION_DOWN} - 若触摸点在活动区域外则忽略</li>
     *   <li>{@code ACTION_MOVE} - 更新摇杆位置（受活动区域边界约束），并触发角度/距离回调</li>
     *   <li>{@code ACTION_UP} - 摇杆归位到中心，并触发归位回调</li>
     * </ul>
     * </p>
     *
     * @param event 触摸事件
     * @return 始终返回 {@code true}，表示消费该事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            int len = MathUtil.getDistance(mAreaPosition.x, mAreaPosition.y, event.getX(), event.getY());

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                //如果屏幕接触点不在摇杆挥动范围内,则不处理
                if (len > mAreaRadius) {
                    return true;
                }
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (len <= mAreaRadius) {
                    //如果手指在摇杆活动范围内，则摇杆处于手指触摸位置
                    mRockerPosition.set((int) event.getX(), (int) event.getY());

                } else {
                    //设置摇杆位置，使其处于手指触摸方向的 摇杆活动范围边缘
                    mRockerPosition = MathUtil.getPointByCutLength(mAreaPosition,
                            new Point((int) event.getX(), (int) event.getY()), mAreaRadius);
                }
                if (mListener != null) {
                    float radian = MathUtil.getRadian(mAreaPosition, new Point((int) event.getX(), (int) event.getY()));
                    float angle = RockerView.this.getAngleConvert(radian);
                    float distance = MathUtil.getDistance(mAreaPosition.x, mAreaPosition.y, event.getX(), event.getY());
                    mListener.callback(EVENT_ACTION, angle, distance);
                }
            }
            //如果手指离开屏幕，则摇杆返回初始位置
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mRockerPosition = new Point(mAreaPosition);
                if (mListener != null) {
                    mListener.callback(EVENT_ACTION, -1, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    /*Thread - draw view***************************************************************************/

    /**
     * 绘制线程主循环。
     * <p>
     * 以 {@link #mRefreshCycle} 毫秒为周期，锁定 Canvas、清除背景、
     * 绘制活动区域和摇杆，然后解锁提交。
     * </p>
     */
    @Override
    public void run() {
        if (isInEditMode()) {
            return;
        }

        Canvas canvas = null;

        while (mDrawOk) {
            boolean canMove = this.canMove;
            try {
                if (canMove) {
                    canvas = mHolder.lockCanvas();
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                    drawArea(canvas);
                    drawRocker(canvas);
                }
                Thread.sleep(mRefreshCycle);    // 休眠

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null && canMove) {
                    mHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    /**
     * 绘制摇杆活动区域（圆形或自定义位图）。
     *
     * @param canvas 画布对象
     */
    private void drawArea(Canvas canvas) {

        if (null != mAreaBitmap) {
            mPaint.setColor(Color.BLACK);
            Rect src = new Rect(0, 0, mAreaBitmap.getWidth(), mAreaBitmap.getHeight());
            Rect dst = new Rect(
                    mAreaPosition.x - mAreaRadius,
                    mAreaPosition.y - mAreaRadius,
                    mAreaPosition.x + mAreaRadius,
                    mAreaPosition.y + mAreaRadius);
            canvas.drawBitmap(mAreaBitmap, src, dst, mPaint);
        } else {
            mPaint.setColor(mAreaColor);
            canvas.drawCircle(mAreaPosition.x, mAreaPosition.y, mAreaRadius, mPaint);
        }
    }

    /**
     * 绘制摇杆指示器（圆形或自定义位图）。
     *
     * @param canvas 画布对象
     */
    private void drawRocker(Canvas canvas) {
        if (null != mRockerBitmap) {
            mPaint.setColor(Color.BLACK);
            Rect src = new Rect(0, 0, mRockerBitmap.getWidth(), mRockerBitmap.getHeight());
            Rect dst = new Rect(
                    mRockerPosition.x - mRockerRadius,
                    mRockerPosition.y - mRockerRadius,
                    mRockerPosition.x + mRockerRadius,
                    mRockerPosition.y + mRockerRadius);
            canvas.drawBitmap(mRockerBitmap, src, dst, mPaint);
        } else {
            mPaint.setColor(mRockerColor);
            canvas.drawCircle(mRockerPosition.x, mRockerPosition.y, mRockerRadius, mPaint);
        }
    }

    /**
     * 定时轮询回调方法，由回调线程按 {@link #mCallbackCycle} 周期调用。
     * <p>
     * 若摇杆位于中心位置则回调角度 -1、距离 0；
     * 否则计算当前角度和距离并回调给监听器。
     * </p>
     */
    private void listenerCallback() {
        if (mListener != null) {
            if (mRockerPosition.x == mAreaPosition.x && mRockerPosition.y == mAreaPosition.y) {
                mListener.callback(EVENT_CLOCK, -1, 0);
            } else {
                float radian = MathUtil.getRadian(mAreaPosition, new Point(mRockerPosition.x, mRockerPosition.y));
                float angle = RockerView.this.getAngleConvert(radian);
                float distance = MathUtil.getDistance(mAreaPosition.x, mAreaPosition.y, mRockerPosition.x, mRockerPosition.y);
                mListener.callback(EVENT_CLOCK, angle, distance);
            }
        }
    }

    /**
     * 将弧度转换为摇杆偏移角度。
     * <p>
     * 角度基准：正上方为 0 度，逆时针方向为负值，顺时针方向为正值。
     * </p>
     *
     * @param radian 弧度值
     * @return 偏移角度（0~360 度范围）
     */
    private float getAngleConvert(float radian) {
        return 90 + Math.round(radian / Math.PI * 180);
    }

    /**
     * 编辑器预览模式下的绘制方法。
     *
     * @param canvas 画布对象
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            canvas.drawColor(Color.WHITE);
            drawArea(canvas);
            drawRocker(canvas);
        }
    }

    /*Getter Setter********************************************************************************/

    /**
     * 设置摇杆是否允许移动。
     *
     * @param isMove {@code true} 允许移动（默认），{@code false} 禁止移动
     */
    public void setCanMove(boolean isMove) {
        this.canMove = isMove;
    }

    /**
     * 获取活动区域半径。
     *
     * @return 活动区域半径（像素）
     */
    public int getAreaRadius() {
        return mAreaRadius;
    }

    /**
     * 设置活动区域半径。
     *
     * @param areaRadius 活动区域半径（像素）
     */
    public void setAreaRadius(int areaRadius) {
        mAreaRadius = areaRadius;
    }

    /**
     * 获取摇杆指示器半径。
     *
     * @return 摇杆半径（像素）
     */
    public int getRockerRadius() {
        return mRockerRadius;
    }

    /**
     * 设置摇杆指示器半径。
     *
     * @param rockerRadius 摇杆半径（像素）
     */
    public void setRockerRadius(int rockerRadius) {
        mRockerRadius = rockerRadius;
    }

    /**
     * 获取活动区域背景位图。
     *
     * @return 活动区域位图，未设置时为 {@code null}
     */
    public Bitmap getAreaBitmap() {
        return mAreaBitmap;
    }

    /**
     * 设置活动区域背景位图（设置后将忽略颜色属性）。
     *
     * @param areaBitmap 活动区域位图
     */
    public void setAreaBitmap(Bitmap areaBitmap) {
        mAreaBitmap = areaBitmap;
    }

    /**
     * 获取摇杆指示器位图。
     *
     * @return 摇杆位图，未设置时为 {@code null}
     */
    public Bitmap getRockerBitmap() {
        return mRockerBitmap;
    }

    /**
     * 设置摇杆指示器位图（设置后将忽略颜色属性）。
     *
     * @param rockerBitmap 摇杆位图
     */
    public void setRockerBitmap(Bitmap rockerBitmap) {
        mRockerBitmap = rockerBitmap;
    }

    /**
     * 获取绘制刷新周期。
     *
     * @return 刷新周期（毫秒）
     */
    public int getRefreshCycle() {
        return mRefreshCycle;
    }

    /**
     * 设置绘制刷新周期。
     *
     * @param refreshCycle 刷新周期（毫秒），值越小画面越流畅但 CPU 占用越高
     */
    public void setRefreshCycle(int refreshCycle) {
        mRefreshCycle = refreshCycle;
    }

    /**
     * 获取回调触发周期。
     *
     * @return 回调周期（毫秒）
     */
    public int getCallbackCycle() {
        return mCallbackCycle;
    }

    /**
     * 设置监听器回调触发周期。
     *
     * @param callbackCycle 回调周期（毫秒）
     */
    public void setCallbackCycle(int callbackCycle) {
        mCallbackCycle = callbackCycle;
    }

    /**
     * 获取活动区域颜色。
     *
     * @return 活动区域的 ARGB 颜色值
     */
    public int getAreaColor() {
        return mAreaColor;
    }

    /**
     * 设置活动区域颜色（同时清除自定义位图）。
     *
     * @param areaColor ARGB 颜色值
     */
    public void setAreaColor(int areaColor) {
        mAreaColor = areaColor;
        mAreaBitmap = null;
    }

    /**
     * 获取摇杆指示器颜色。
     *
     * @return 摇杆的 ARGB 颜色值
     */
    public int getRockerColor() {
        return mRockerColor;
    }

    /**
     * 设置摇杆指示器颜色（同时清除自定义位图）。
     *
     * @param rockerColor ARGB 颜色值
     */
    public void setRockerColor(int rockerColor) {
        mRockerColor = rockerColor;
        mRockerBitmap = null;
    }

    /**
     * 设置摇杆事件监听器。
     *
     * @param listener 摇杆监听器，不可为 {@code null}
     */
    public void setListener(@NonNull RockerListener listener) {
        mListener = listener;
    }

    /*Rocker Listener******************************************************************************/

    /**
     * 摇杆事件监听接口。
     * <p>
     * 实现此接口以接收摇杆的角度和距离变化通知。
     * </p>
     */
    public interface RockerListener {

        /**
         * 摇杆状态变化时回调。
         *
         * @param eventType       事件类型：{@link #EVENT_ACTION}（触摸触发）或 {@link #EVENT_CLOCK}（定时轮询）
         * @param currentAngle    当前摇杆偏移角度（0~360 度），摇杆归位时为 -1
         * @param currentDistance 当前摇杆与中心的距离（像素），摇杆归位时为 0
         */
        void callback(int eventType, float currentAngle, float currentDistance);
    }

}