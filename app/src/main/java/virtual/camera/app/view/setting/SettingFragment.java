package virtual.camera.app.view.setting;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;

import com.blankj.utilcode.util.SPUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import virtual.camera.app.R;
import virtual.camera.app.app.App;
import virtual.camera.app.settings.LogUtil;
import virtual.camera.app.settings.MethodType;
import virtual.camera.app.util.AppUtil;
import virtual.camera.app.util.HandlerUtil;
import virtual.camera.app.util.ToastUtils;

/**
 * 虚拟摄像头设置页面 Fragment。
 * <p>
 * 提供摄像头保护方式的选择（禁用摄像头、本地视频、网络视频），
 * 以及相关视频路径、音频开关等配置项的管理。
 * 用户可通过此界面选择替换视频源并保存设置，
 * 保存后会自动终止相关子进程使配置生效。
 * </p>
 */
public class SettingFragment extends BaseFragment {

    /** 保护方式选择按钮 */
    private AppCompatButton mProtectMethodBtn, mSave;

    /** 当前保护方式文本、提示文本、音频标签文本 */
    private AppCompatTextView mProtectMethodText, mTip, mAudioText;

    /** 视频路径/URL 输入框 */
    private AppCompatEditText mInput;

    /** 音频开关 */
    private SwitchCompat mAudioSwitch;

    /** 选择本地视频按钮 */
    private AppCompatButton mChoiseVideo;

    /** 保护方式弹出菜单 */
    private PopupMenu mPopupMenu = null;

    /** 当前选中的保护方式类型，参见 {@link MethodType} */
    private int mMethodType = 0;

    /** 标记是否已通过文件选择器选择过视频 */
    private boolean mHasOpenDocuments = false;

    /** 用于启动视频文件选择器的 ActivityResultLauncher */
    private ActivityResultLauncher<String> openDocumentedResult =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onVideoChoiseDone);

    /**
     * 视频文件选择完成后的回调。
     * <p>
     * 若用户成功选择了视频文件，则将视频 URI 设置到输入框中。
     * </p>
     *
     * @param video 用户选择的视频文件 URI，可能为 {@code null}（用户取消选择）
     */
    public void onVideoChoiseDone(Uri video) {
        if (video == null) {
        } else {
            mInput.setText(video.toString());
            mHasOpenDocuments = true;
        }
        LogUtil.log("onVideoChoiseDone:" + video);
    }

    /**
     * 创建设置页面的视图层次结构。
     *
     * @param inflater           用于加载 XML 布局的 LayoutInflater
     * @param container          父容器 ViewGroup
     * @param savedInstanceState 保存的实例状态
     * @return 设置页面的根视图
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_camera_settings, container, false);
        initView(view);
        return view;
    }

    /**
     * 初始化所有视图控件并绑定事件监听器。
     * <p>
     * 包括保护方式选择按钮、保存按钮、视频选择按钮的点击事件绑定，
     * 以及从 SharedPreferences 读取上次保存的保护方式类型并恢复 UI 状态。
     * </p>
     *
     * @param rootView 页面根视图
     */
    private void initView(View rootView) {
        mProtectMethodBtn = rootView.findViewById(R.id.protect_method_btn);
        mSave = rootView.findViewById(R.id.protect_save);
        mProtectMethodText = rootView.findViewById(R.id.protect_method_text);
        mTip = rootView.findViewById(R.id.protect_tip);
        mInput = rootView.findViewById(R.id.protect_path);
        mAudioText = rootView.findViewById(R.id.protect_audio);
        mAudioSwitch = rootView.findViewById(R.id.protect_audio_switch);
        mChoiseVideo = rootView.findViewById(R.id.protect_video_select);
        mChoiseVideo.setOnClickListener(v -> {
            openDocumentedResult.launch("video/*");
        });

        mProtectMethodBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPopupMenu = new PopupMenu(getActivitySafe(), view);
                mPopupMenu.inflate(R.menu.camera_menu);
                mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.protect_method_disable_camera:
                                onMethodTypeClick(MethodType.TYPE_DISABLE_CAMERA);
                                break;
                            case R.id.protect_method_local:
                                onMethodTypeClick(MethodType.TYPE_LOCAL_VIDEO);
                                break;
                            case R.id.protect_method_network:
                                onMethodTypeClick(MethodType.TYPE_NETWORK_VIDEO);
                                break;
                        }
                        return true;
                    }
                });
                mPopupMenu.show();
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });

        int method_type = SPUtils.getInstance().getInt("method_type", MethodType.TYPE_DISABLE_CAMERA);
        if (method_type > 0) {
            onMethodTypeClick(method_type);
        }
    }

    /**
     * 将用户选择的本地视频文件复制到应用私有目录。
     * <p>
     * 从 ContentResolver 读取源视频并写入 {@code getFilesDir()/video.xxx}，
     * 同时持久化视频路径、保护方式类型、音频开关等配置到 SharedPreferences。
     * </p>
     *
     * @param u 视频文件的 URI 字符串
     * @return {@code true} 表示复制成功，{@code false} 表示复制失败
     */
    private boolean copyLocalVideo(String u) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            Uri uri = Uri.parse(u);
            String video_path_local = SPUtils.getInstance().getString("video_path_local_final_out", "");
            if (!TextUtils.isEmpty(video_path_local)) {
                new File(video_path_local).delete();
            }
            String subfix = u.substring(u.lastIndexOf("."), u.length());
            String outPath = App.getContext().getFilesDir() + "/video" + subfix;
            fos = new FileOutputStream(outPath);
            is = getActivitySafe().getContentResolver().openInputStream(uri);
            byte[] buffer = new byte[1024];
            int len = is.read(buffer);
            while (len >= 0) {
                fos.write(buffer);
                len = is.read(buffer);
            }
            fos.flush();
            fos.close();
            is.close();

            SPUtils.getInstance().put("method_type", mMethodType);
            SPUtils.getInstance().put("video_path_local", u);
            SPUtils.getInstance().put("video_path_local_final_out", outPath);
            SPUtils.getInstance().put("video_path_local_audio_enable", mAudioSwitch.isChecked());
            ToastUtils.showToast("Save Success...:");
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            HandlerUtil.runOnMain(new Runnable() {
                @Override
                public void run() {
                    mInput.setText("");
                }
            });
            ToastUtils.showToast("Video handing failed:" + e);
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable e) {

                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Throwable e) {

                }
            }
        }
    }

    /**
     * 切换保护方式并更新 UI 显示。
     * <p>
     * 根据传入的类型更新提示文本、输入框可见性、视频选择按钮、
     * 音频开关的显示状态，并从 SharedPreferences 恢复对应的已保存值。
     * </p>
     *
     * @param type 保护方式类型，取值参见 {@link MethodType}
     */
    private void onMethodTypeClick(int type) {
        mMethodType = type;
        switch (type) {
            case MethodType.TYPE_DISABLE_CAMERA:
                mProtectMethodText.setText(R.string.protect_method_disable_camera);
                mTip.setText(R.string.protect_tip_disable);
                mInput.setVisibility(View.GONE);
                mChoiseVideo.setVisibility(View.GONE);
                mAudioText.setVisibility(View.GONE);
                mAudioSwitch.setVisibility(View.GONE);
                break;
            case MethodType.TYPE_LOCAL_VIDEO:
                mProtectMethodText.setText(R.string.protect_method_local);
                mTip.setText(R.string.protect_tip_local);
                mInput.setVisibility(View.VISIBLE);
                mChoiseVideo.setVisibility(View.VISIBLE);
                mChoiseVideo.setEnabled(true);
                mChoiseVideo.setText(R.string.choise_video);
                mAudioText.setVisibility(View.VISIBLE);
                mAudioSwitch.setVisibility(View.VISIBLE);
                mInput.setHint("");
                mInput.setEnabled(false);
                if (mHasOpenDocuments) {
                    mInput.setText(SPUtils.getInstance().getString("video_path_local", ""));
                } else {
                    mInput.setText("");
                }
                mAudioSwitch.setChecked(SPUtils.getInstance().getBoolean("video_path_local_audio_enable", true));
                break;
            case MethodType.TYPE_NETWORK_VIDEO:
                mProtectMethodText.setText(R.string.protect_method_network);
                mTip.setText(R.string.protect_tip_network);
                mInput.setVisibility(View.VISIBLE);
                mChoiseVideo.setVisibility(View.GONE);
                mAudioText.setVisibility(View.VISIBLE);
                mAudioSwitch.setVisibility(View.VISIBLE);
                mInput.setHint(R.string.protect_path_hint);
                mInput.setEnabled(true);
                mInput.setText(SPUtils.getInstance().getString("video_path_network", ""));
                mAudioSwitch.setChecked(SPUtils.getInstance().getBoolean("video_path_network_audio_enable", true));
                break;
        }
    }

    /**
     * 保存当前设置并终止相关子进程以使配置生效。
     * <p>
     * 根据当前保护方式类型执行不同逻辑：
     * <ul>
     *   <li>禁用摄像头：直接保存类型</li>
     *   <li>本地视频：校验输入后在子线程中复制视频文件</li>
     *   <li>网络视频：校验 URL 合法性后保存</li>
     * </ul>
     * 保存前会先调用 {@link AppUtil#killAllApps()} 终止子进程。
     * </p>
     */
    private void saveSettings() {
        AppUtil.killAllApps();
        switch (mMethodType) {
            case MethodType.TYPE_DISABLE_CAMERA:
                SPUtils.getInstance().put("method_type", mMethodType);
                ToastUtils.showToast("Save Success...");
                break;
            case MethodType.TYPE_LOCAL_VIDEO:
                if (TextUtils.isEmpty(mInput.getText())) {
                    ToastUtils.showToast("Video not set...");
                    return;
                }
                ProgressDialog progressDialog = new ProgressDialog(getActivitySafe(), ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                progressDialog.setMessage("Handing video...");
                progressDialog.show();
                new Thread() {
                    @Override
                    public void run() {
                        copyLocalVideo(mInput.getText().toString());
                        HandlerUtil.runOnMain(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    progressDialog.dismiss();
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }.start();
                break;
            case MethodType.TYPE_NETWORK_VIDEO:
                if (TextUtils.isEmpty(mInput.getText())) {
                    ToastUtils.showToast("Video not set...");
                    return;
                }
                if (!mInput.getText().toString().toLowerCase().startsWith("http")) {
                    ToastUtils.showToast("Video url should start with http or https");
                    return;
                }
                SPUtils.getInstance().put("method_type", mMethodType);
                SPUtils.getInstance().put("video_path_network", mInput.getText().toString());
                SPUtils.getInstance().put("video_path_network_audio_enable", mAudioSwitch.isChecked());
                ToastUtils.showToast("Save Success...");
                break;
        }
    }
}
