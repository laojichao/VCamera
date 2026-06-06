package virtual.camera.app.settings;

/**
 * 摄像头保护方式类型常量类。
 * <p>
 * 定义了虚拟摄像头应用支持的三种视频替换策略：
 * 禁用摄像头、本地视频替换、网络视频替换。
 * 用于在设置界面和配置持久化时标识当前选择的替换方式。
 * </p>
 */
public class MethodType {

    /** 禁用摄像头模式，不提供视频源 */
    public static final int TYPE_DISABLE_CAMERA = 1;

    /** 本地视频模式，使用设备上已有的视频文件作为替换源 */
    public static final int TYPE_LOCAL_VIDEO = 2;

    /** 网络视频模式，使用网络 URL 指定的视频流作为替换源 */
    public static final int TYPE_NETWORK_VIDEO = 3;
}
