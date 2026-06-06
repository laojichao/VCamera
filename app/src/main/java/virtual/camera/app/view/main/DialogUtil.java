package virtual.camera.app.view.main;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.SPUtils;

import virtual.camera.app.R;
import virtual.camera.app.util.ToastUtils;

/**
 * 对话框工具类。
 * <p>
 * 提供应用启动时的 GitHub 仓库引导对话框，
 * 支持"前往查看"、"取消"和"不再显示"三种操作。
 * 对话框显示状态通过 {@link SPUtils} 持久化。
 * </p>
 */
public class DialogUtil {

    /**
     * 显示应用启动引导对话框。
     * <p>
     * 若 {@code check} 为 {@code true} 且用户已选择"不再显示"，则跳过显示。
     * 对话框包含以下按钮：
     * <ul>
     *   <li>"前往" - 跳转到 GitHub 仓库页面</li>
     *   <li>"取消" - 关闭对话框</li>
     *   <li>"不再显示" - 记住用户选择，后续启动不再弹出（仅 {@code check} 为 true 时显示）</li>
     * </ul>
     * </p>
     *
     * @param activity 宿主 Activity，不可为 {@code null}
     * @param check    是否检查"不再显示"标记；{@code true} 时检查，{@code false} 时强制显示
     */
    public static void showDialog(final Activity activity, boolean check) {
        try {
            boolean show_start_dialog = SPUtils.getInstance().getBoolean("show_start_dialog", true);
            if (!show_start_dialog && check) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(activity).setCancelable(false);
            builder.setTitle(R.string.tips).setMessage(R.string.dialog_github_start);
            builder.setPositiveButton(R.string.goto_str, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://github.com/andvipgroup/VCamera"));
                        activity.startActivity(intent);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        ToastUtils.showToast("open failed.");
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        dialog.dismiss();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
            if (check) {
                builder.setNeutralButton(R.string.never_show, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            dialog.dismiss();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        SPUtils.getInstance().put("show_start_dialog", false);
                    }
                });
            }
            AlertDialog alertDialog = builder.show();
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextAppearance(R.style.VCameraDialog);
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextAppearance(R.style.VCameraDialog);
            alertDialog.show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
