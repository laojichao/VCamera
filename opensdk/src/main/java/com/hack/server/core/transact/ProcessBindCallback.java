package com.hack.server.core.transact;

import static com.hack.opensdk.CmdConstants.TRANSACT_KEY_PKG;
import static com.hack.opensdk.CmdConstants.TRANSACT_KEY_PROCESS;
import static com.hack.opensdk.CmdConstants.TRANSACT_KEY_SPACE;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.hack.opensdk.CmdConstants;
import com.hack.server.core.TransactCallback;

/**
 * 进程绑定完成回调处理器。
 * <p>
 * 实现 {@link TransactCallback} 接口，专门处理进程绑定完成的事务通知
 * （{@link CmdConstants#TRANSACT_CMD_PROCESS_BINDED}）。当代理进程完成绑定后，
 * 从 extras 中提取空间标识、包名和进程名，并调用 {@link #onBindProcess(String, String, int)}
 * 通知子类绑定已完成。
 * </p>
 */
public class ProcessBindCallback implements TransactCallback {
    /**
     * 处理进程绑定完成的事务请求。
     * <p>仅处理 {@link CmdConstants#TRANSACT_CMD_PROCESS_BINDED} 命令，
     * 其他命令直接返回 {@code null}。</p>
     *
     * @param context 当前应用上下文
     * @param cmd     事务命令标识
     * @param extras  事务附加参数，包含空间标识、包名和进程名
     * @return 始终返回 {@code null}
     */
    @Override
    public final Bundle transact(Context context, int cmd, Bundle extras) {
        if (cmd != CmdConstants.TRANSACT_CMD_PROCESS_BINDED) {
            return null;
        }
        int space = extras.getInt(TRANSACT_KEY_SPACE);
        String pkg = extras.getString(TRANSACT_KEY_PKG);
        String process = extras.getString(TRANSACT_KEY_PROCESS);
        onBindProcess(process, pkg, space);
        return null;
    }

    /**
     * 进程绑定完成时的回调方法。
     * <p>子类可重写此方法以执行自定义的进程绑定后处理逻辑。</p>
     *
     * @param process 绑定完成的进程名称
     * @param pkg     关联的应用包名
     * @param space   空间标识
     */
    protected void onBindProcess(String process, String pkg, int space) {
        Log.d("TransactProvider", String.format("agent process bind complete! " + "[space: %d] [pkg: %s] [process: %s]", space, pkg, process));
    }
}
