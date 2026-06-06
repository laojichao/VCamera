package com.hack.agent;

import static com.hack.opensdk.CmdConstants.CMD_AGENT_JOB_SERVICE_BIND;
import static com.hack.opensdk.CmdConstants.CMD_AGENT_JOB_SERVICE_UNBIND;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.hack.opensdk.Cmd;

/**
 * Hack 任务服务。
 * <p>
 * 继承自 {@link Service}，作为代理服务处理 Job 相关的绑定与解绑操作。
 * 通过 {@link Cmd} 命令机制将绑定/解绑请求委托给底层引擎处理，
 * 实现跨进程的服务代理功能。
 * </p>
 */
public class HackJobService extends Service {
    private static final String TAG = "HackJobService";

    /**
     * 当客户端绑定到此服务时调用。
     * <p>
     * 将绑定请求委托给底层 Hack 引擎，由引擎返回实际的 {@link IBinder} 对象。
     * </p>
     *
     * @param intent 绑定 Intent，包含客户端请求信息
     * @return 由底层引擎返回的 Binder 对象，供客户端进行跨进程通信
     */
    @Override
    public IBinder onBind(Intent intent) {
        return (IBinder) Cmd.INSTANCE().exec(CMD_AGENT_JOB_SERVICE_BIND, intent);
    }

    /**
     * 当所有客户端解除绑定时调用。
     * <p>
     * 将解绑请求委托给底层 Hack 引擎处理。
     * </p>
     *
     * @param intent 解绑 Intent
     * @return {@code true} 表示支持重新绑定，{@code false} 表示不支持，具体值由引擎决定
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return (boolean) Cmd.INSTANCE().exec(CMD_AGENT_JOB_SERVICE_UNBIND, intent);
    }
}
