package com.hack.agent;

/**
 * Hack 文件提供者。
 * <p>
 * 继承自 {@link BaseAgentFileProvider}，用于在 Hack 进程环境中提供文件访问能力。
 * 该类通过 {@link #onCreate()} 初始化 ContentProvider，支持文件的跨进程代理访问。
 * </p>
 */
public class HackFileProvider extends BaseAgentFileProvider {

    /**
     * ContentProvider 创建时调用，完成初始化。
     *
     * @return 始终返回父类的初始化结果
     */
    @Override
    public boolean onCreate() {
        return super.onCreate();
    }
}