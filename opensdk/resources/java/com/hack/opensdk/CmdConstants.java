package com.hack.opensdk;

/**
 * OpenSDK 命令常量定义类。
 * <p>
 * 集中定义了 OpenSDK 框架中所有命令码（CMD）、传输键名（TRANSACT）、
 * 运行时属性键名（RUNTIME）等常量，供客户端与服务端在 IPC 通信中统一使用。
 * </p>
 * <p>
 * 基于安全考虑，每个版本的 CMD 命令码不能重复。因此将此文件放在工程根目录的
 * {@code src/main} 下，方便后续通过脚本自动生成和校验。
 * </p>
 *
 * @author hack
 * @see com.hack.opensdk
 */
public class CmdConstants {

    // ==================== 安装模式标志位 ====================

    /** 单独安装模式，标识位第 0 位 */
    public static int MODE_INSTALL_ALONE = 0x01 << 0;
    /** 强制安装模式，标识位第 1 位 */
    public static int MODE_FORCE_INSTALL = 0x01 << 1;

    // ==================== Application 生命周期命令 (1~9) ====================

    /** Application.attachBaseContext 生命周期回调命令 */
    public static final int CMD_APPLICATION_ATTACHBASE = 1;
    /** Application.onCreate 生命周期回调命令 */
    public static final int CMD_APPLICATION_ONCREATE = 2;

    // ==================== 包管理命令 (10~19) ====================

    /** 安装应用包 */
    public static final int CMD_INSTALL_PACKAGE = 10;
    /** 卸载应用包 */
    public static final int CMD_UNINSTALL_PACKAGE = 11;
    /** 更新应用包 */
    public static final int CMD_UPDATA_PACKAGE = 12;
    /** 移除应用包数据 */
    public static final int CMD_REMOVE_PKG_DATA = 13;
    /** 删除应用包缓存 */
    public static final int CMD_DELETE_PKG_CACHE = 14;
    /** 杀死应用包进程 */
    public static final int CMD_KILL_PACKAGE = 15;
    /** 应用包可能变为可见状态通知 */
    public static final int CMD_PACKAGE_MAYBE_VISIBLE = 16;

    // ==================== 包查询命令 (20~29) ====================

    /** 获取应用包信息 */
    public static final int CMD_GET_PACKAGE_INFO = 20;
    /** 解析 Intent 对应的目标组件 */
    public static final int CMD_RESOLVE_INTENT = 21;
    /** 查询匹配的 Activity 列表 */
    public static final int CMD_QUERY_ACTIVITIES = 22;
    /** 获取指定 Activity 的信息 */
    public static final int CMD_GET_ACTIVITY_INFO = 23;
    /** 获取已安装的应用包列表 */
    public static final int CMD_GET_INSTALLED_PKGS = 24;
    /** 获取应用包的配置设置 */
    public static final int CMD_GET_PKG_SETTINGS = 25;
    /** 获取启动器 Intent */
    public static final int CMD_GET_LAUNCHER_INTENT = 26;
    /** 获取不可用的应用包列表 */
    public static final int CMD_GET_UNAVAILABLE_PKGS = 27;

    // ==================== 启动命令 (30~39) ====================

    /** 启动应用包 */
    public static final int CMD_START_PACKAGE = 30;
    /** 启动指定 Activity */
    public static final int CMD_START_ACTIVITY = 31;
    /** 快速启动指定 Activity（跳过部分初始化流程） */
    public static final int CMD_QUICK_START_ACTIVITY = 32;

    // ==================== 观察者注册/注销命令 (40~49) ====================

    /** 注册卸载观察者 */
    public static final int CMD_REGISTER_UNINSTALL_OBSERVER = 40;
    /** 注销卸载观察者 */
    public static final int CMD_UNREGISTER_UNINSTALL_OBSERVER = 41;
    /** 注册安装观察者 */
    public static final int CMD_REGISTER_INSTALL_OBSERVER = 42;
    /** 注销安装观察者 */
    public static final int CMD_UNREGISTER_INSTALL_OBSERVER = 43;

    // ==================== Agent 任务服务命令 (90~99) ====================

    /** 绑定 Agent JobService */
    public static final int CMD_AGENT_JOB_SERVICE_BIND = 90;
    /** 解绑 Agent JobService */
    public static final int CMD_AGENT_JOB_SERVICE_UNBIND = 91;

    // ==================== Agent Provider 命令 (100~119) ====================

    /** Agent ContentProvider 调用命令 */
    public static final int CMD_AGENT_PROVIDER_CALL = 100;

    // ==================== Agent Intent 命令 (120~129) ====================

    /** Agent IntentSender 发送命令 */
    public static final int CMD_AGENT_INTENT_SENDER = 120;

    // ==================== Core Provider 命令 (150~159) ====================

    /** 创建核心 ContentProvider */
    public static final int CMD_CORE_PROVIDER_CREATE = 150;
    /** 调用核心 ContentProvider */
    public static final int CMD_CORE_PROVIDER_CALL = 151;

    // ==================== 用户 ID 命令 (160~169) ====================

    /** 获取所有用户 ID 列表 */
    public static final int CMD_GET_ALL_USERID = 160;
    /** 获取应用包关联的所有用户 ID */
    public static final int CMD_PKG_ALL_USERID = 161;

    // ==================== 属性与 Binder 命令 (200~209) ====================

    /** 获取归因信息的 Parcel 数据 */
    public static final int CMD_GET_ATTRIBUTION_PARCEL = 200;
    /** 获取 Binder 调用方的 UID */
    public static final int CMD_BINDER_CALLING_UID = 201;

    // ==================== Assist Provider 命令 (210~219) ====================

    /** 辅助 ContentProvider 调用命令 */
    public static final int CMD_ASSIST_PROVIDER_CALL = 210;

    // ==================== Assist Activity 命令 (220~229) ====================

    /** 辅助 Activity 调用命令 */
    public static final int CMD_ASSIST_ACTIVITY_CALL = 220;
    /** 辅助 Activity 调用命令（第二版） */
    public static final int CMD_ASSIST_ACTIVITY_CALL2 = 221;

    // ==================== FileProvider 命令 (240~249) ====================

    /** 通过 FileProvider 生成 URI */
    public static final int CMD_FILE_PROVIDER_MAKE_URI = 240;

    // ==================== 运行时属性命令 (300~309) ====================

    /** 获取运行时属性 */
    public static final int CMD_GET_RUNTIME_PROPERTIES = 300;

    // ==================== Application 回调命令 (400~409) ====================

    /** 注册 Application 回调 */
    public static final int CMD_REGISTER_APPLICATION_CALLBACK = 400;

    // ==================== CMD_GET_PKG_SETTINGS Bundle 键名 ====================

    /** 请求辅助进程的键名 */
    public static final String PKG_SET_REQUEST_ASSISTANT = "request_assistant";
    /** 应用支持的 ABI 列表键名 */
    public static final String PKG_SUPPORTED_ABIS = "supported_abis";
    /** 应用安装模式键名 */
    public static final String PKG_INSTALLED_MODE = "installed_mode";

    // ==================== TransactProvider 相关常量 ====================

    /**************BEGIN TransactProvider*******************/
    /** TransactProvider 的 Authority 标识 */
    public static final String TRANSACT_PROVIDER_AUTHORITY = "com.hack.server.core.TransactProvider";
    /** TransactProvider 的调用方法名 */
    public static final String TRANSACT_PROVIDER_METHOD = "transact";
    /** Transact 命令码的 Bundle 键名 */
    public static final String TRANSACT_KEY_CMD = "transact_cmd";

    /** 目标应用包名的 Bundle 键名 */
    public static final String TRANSACT_KEY_PKG = "pkg";
    /** 宿主（Shell）应用包名的 Bundle 键名 */
    public static final String TRANSACT_KEY_SHELL_PKG = "shell_pkg";
    /** Agent 进程名的 Bundle 键名 */
    public static final String TRANSACT_KEY_PROCESS = "agent_process";
    /** 多开空间标识的 Bundle 键名 */
    public static final String TRANSACT_KEY_SPACE = "space";
    /** Intent 数据的 Bundle 键名 */
    public static final String TRANSACT_KEY_INTENT = "intent";
    /** 来源 Token 的 Bundle 键名 */
    public static final String TRANSACT_KEY_FROM_TOKEN = "from_token";

    /** 返回结果的 Bundle 键名 */
    public static final String TRANSACT_KEY_RESULT = "ret";
    /** 输出 Intent 的 Bundle 键名 */
    public static final String TRANSACT_KEY_OUT_INTENT = "outIntent";

    /** Authority 标识的 Bundle 键名 */
    public static final String TRANSACT_KEY_AUTHORITY = "authority";

    /** Transact 命令：进程已绑定 */
    public static final int TRANSACT_CMD_PROCESS_BINDED = 1;
    /** Transact 命令：外部 Intent 转发 */
    public static final int TRANSACT_CMD_OUTER_INTENT = 2;
    /** Transact 命令：获取 ContentProvider */
    public static final int TRANSACT_CMD_ACQUIRE_PROVIDER = 3;
    /**************END TransactProvider*******************/

    // ==================== 运行时属性键名 ====================

    /**************BEGIN RUNTIME_PROPERTIES*******************/
    /** 运行时自定义类加载器的属性键名 */
    public static final String RUNTIME_HACK_CLASSLOADER = "hack.runtime.classloader";
    /** 运行时属性集合的键名 */
    public static final String RUNTIME_PROPERTIES = "hack.runtime.properties";
    /** 多开空间标识的运行时属性键名 */
    public static final String RUNTIME_PROPERTIES_SPACE = "space";
    /** 应用包名的运行时属性键名 */
    public static final String RUNTIME_PROPERTIES_PKG = "pkg";
    /** 进程名的运行时属性键名 */
    public static final String RUNTIME_PROPERTIES_PROCESS = "process";
    /** 应用 ID 的运行时属性键名 */
    public static final String RUNTIME_PROPERTIES_APPID = "appId";
    /**************END RUNTIME_PROPERTIES*******************/
}
