package virtual.camera.app.util;

import android.graphics.Point;
import android.graphics.PointF;

/**
 * 数学计算工具类。
 * <p>
 * 提供二维平面几何相关的计算方法，包括两点间距离、
 * 角度与弧度互换、直线上按截距取点等。
 * 主要服务于 {@link virtual.camera.app.widget.RockerView} 摇杆控件的方向和距离计算。
 * </p>
 */
public class MathUtil {

    /** 默认构造方法 */
    public MathUtil() {
    }

    /**
     * 计算两个 {@link PointF} 点之间的欧氏距离（取整）。
     *
     * @param A 第一个点，不可为 {@code null}
     * @param B 第二个点，不可为 {@code null}
     * @return 两点之间的距离（像素值，取整）
     */
    public static int getDistance(PointF A, PointF B) {
        return (int) Math.sqrt(Math.pow(A.x - B.x, 2) + Math.pow(A.y - B.y, 2));
    }

    /**
     * 根据两组坐标计算欧氏距离（取整）。
     *
     * @param x1 第一个点的 X 坐标
     * @param y1 第一个点的 Y 坐标
     * @param x2 第二个点的 X 坐标
     * @param y2 第二个点的 Y 坐标
     * @return 两点之间的距离（像素值，取整）
     */
    public static int getDistance(float x1, float y1, float x2, float y2) {
        return (int) Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * 在线段 AB 上，从点 A 出发按指定截距长度取点。
     * <p>
     * 先计算线段 AB 的弧度，再沿该方向从 A 点移动指定距离。
     * </p>
     *
     * @param A         起始点，不可为 {@code null}
     * @param B         终止点，不可为 {@code null}
     * @param cutLength 截距长度（像素值）
     * @return 截距点的坐标
     */
    public static Point getPointByCutLength(Point A, Point B, int cutLength) {
        float radian = getRadian(A, B);
        return new Point(A.x + (int) (cutLength * Math.cos(radian)), A.y + (int) (cutLength * Math.sin(radian)));
    }

    /**
     * 计算线段 AB 与水平轴之间的弧度角。
     * <p>
     * 结果范围为 [-π, π]。当 B 在 A 上方时为负值，下方时为正值。
     * </p>
     *
     * @param A 起始点，不可为 {@code null}
     * @param B 终止点，不可为 {@code null}
     * @return 线段 AB 与水平轴的弧度值
     */
    public static float getRadian(Point A, Point B) {
        // 计算水平和垂直分量
        float lenA = B.x - A.x;
        float lenB = B.y - A.y;
        // 计算斜边长度
        float lenC = (float) Math.sqrt(lenA * lenA + lenB * lenB);
        // 通过反余弦得到弧度
        float radian = (float) Math.acos(lenA / lenC);
        // B 在 A 上方时取负值（屏幕坐标系 Y 轴向下）
        radian = radian * (B.y < A.y ? -1 : 1);
        return radian;
    }


    /**
     * 将角度值转换为弧度值。
     *
     * @param angle 角度值
     * @return 对应的弧度值
     */
    public static double angle2Radian(double angle) {
        return angle / 180 * Math.PI;
    }

    /**
     * 将弧度值转换为角度值。
     *
     * @param radian 弧度值
     * @return 对应的角度值
     */
    public static double radian2Angle(double radian) {
        return radian / Math.PI * 180;
    }
}