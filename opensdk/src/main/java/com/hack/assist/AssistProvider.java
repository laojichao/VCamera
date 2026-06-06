package com.hack.assist;

import com.hack.agent.ProviderBase;
import com.hack.opensdk.CmdConstants;

/**
 * 辅助进程 ContentProvider。
 * <p>
 * 继承自 {@link ProviderBase}，用于在辅助进程（Assist Process）中提供 ContentProvider 代理能力。
 * 通过指定 {@link CmdConstants#CMD_ASSIST_PROVIDER_CALL} 命令类型，
 * 将 {@code call()} 请求委托给底层引擎处理。
 * </p>
 */
public class AssistProvider extends ProviderBase {

    /**
     * 获取该 Provider 对应的命令调用类型。
     *
     * @return 辅助 Provider 调用的命令类型常量 {@link CmdConstants#CMD_ASSIST_PROVIDER_CALL}
     */
    @Override
    public int getProviderCallType() {
        return CmdConstants.CMD_ASSIST_PROVIDER_CALL;
    }
}
