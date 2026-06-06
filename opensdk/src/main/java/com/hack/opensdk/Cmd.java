package com.hack;

import com.hack.utils.RefUtils;
import com.hack.utils.Singleton;

import java.util.Arrays;

/**
 * 命令执行器（单例）。
 * <p>
 * 作为 SDK 与底层 Hack 引擎之间的命令通信桥梁。
 * 通过反射调用引擎层 {@code com.core.Cmd} 类的静态 {@code exec} 方法，
 * 将上层的命令请求传递给引擎执行并返回结果。
 * </p>
 * <p>
 * 使用单例模式确保全局唯一实例，构造时通过类加载器加载引擎命令类，
 * 并缓存方法引用以提升后续调用性能。
 * </p>
 */
public class Cmd {
    private static final String TAG = Cmd.class.getSimpleName();
    private static final boolean DEBUG = Features.DEBUG;

    /** 引擎命令类的全限定名 */
    private static final String ENGINE_CMD_CLASS = "com.core.Cmd";

    /** 引擎 exec 方法的反射引用 */
    private RefUtils.MethodRef mEngineExecMethod;

    /**
     * 获取 {@link Cmd} 的全局单例实例。
     *
     * @return {@link Cmd} 单例
     */
    public static Cmd INSTANCE() {
        return singleton.get();
    }

    /** 单例持有者 */
    private static Singleton<Cmd> singleton = new Singleton<Cmd>() {
        @Override
        protected Cmd create() {
            return new Cmd();
        }
    };

    /**
     * 私有构造方法。
     * <p>
     * 初始化流程：
     * <ol>
     *   <li>首先尝试通过当前类加载器加载引擎命令类</li>
     *   <li>若失败，则尝试通过 {@link HackRuntime#getHackClassLoader()} 加载</li>
     *   <li>缓存 {@code exec(int, Object[])} 方法的反射引用</li>
     * </ol>
     * </p>
     */
    private Cmd() {
        Class engineCmdClass = null;
        try {
            engineCmdClass = Class.forName(ENGINE_CMD_CLASS);
        } catch (ClassNotFoundException ignore) {
        }

        if (engineCmdClass == null) {
            try {
                engineCmdClass = HackRuntime.getHackClassLoader().loadClass(ENGINE_CMD_CLASS);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        mEngineExecMethod = new RefUtils.MethodRef(
                engineCmdClass,
                true, "exec",
                new Class[]{int.class, Object[].class});
    }

    /**
     * 执行指定的命令。
     * <p>
     * 将命令类型和参数传递给底层引擎的 {@code exec} 方法，
     * 调试模式下会记录命令的开始和结束日志。
     * </p>
     *
     * @param cmd  命令类型常量，定义于 {@link com.hack.opensdk.CmdConstants}
     * @param args 命令参数列表，可变长度
     * @return 引擎执行结果，具体类型取决于命令类型
     */
    public Object exec(int cmd, Object... args) {
        if (DEBUG) Slog.d(TAG, "begin exec %d %s", cmd, Arrays.toString(args));
        Object ret = mEngineExecMethod.invoke(null, cmd, args);
        if (DEBUG) Slog.d(TAG, "end exec %d %s", cmd, ret);
        return ret;
    }
}
