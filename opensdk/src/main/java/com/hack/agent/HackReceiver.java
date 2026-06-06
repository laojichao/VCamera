package com.hack.agent;

import static com.hack.opensdk.CmdConstants.CMD_AGENT_INTENT_SENDER;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hack.opensdk.Cmd;

/**
 * Hack 广播接收器。
 * <p>
 * 继承自 {@link BroadcastReceiver}，作为代理广播接收器，
 * 将收到的广播 Intent 委托给底层 Hack 引擎进行处理。
 * 通过 {@link Cmd} 命令机制实现广播的跨进程代理转发。
 * </p>
 */
public class HackReceiver extends BroadcastReceiver {

    private static final String TAG = HackReceiver.class.getSimpleName();

    /**
     * 接收到广播时调用。
     * <p>
     * 将广播的上下文和 Intent 委托给底层引擎，由引擎执行实际的广播处理逻辑。
     * </p>
     *
     * @param context 接收广播时的上下文
     * @param intent  接收到的广播 Intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Cmd.INSTANCE().exec(CMD_AGENT_INTENT_SENDER, context, intent);
    }
}
