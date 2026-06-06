package virtual.camera.app.view.setting;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.Fragment;

/**
 * Fragment 基类。
 * <p>
 * 提供安全的 {@link Activity} 引用获取和 UI 线程任务调度能力，
 * 避免在 Fragment 生命周期中因 Activity 引用失效而导致的空指针异常。
 * 所有设置页面的 Fragment 均应继承此类。
 * </p>
 */
public class BaseFragment extends Fragment {

    /** 缓存的 Activity 引用，在 {@link #onAttach(Context)} 中赋值 */
    private Activity mActivity;

    /**
     * 安全获取关联的 Activity 实例。
     * <p>
     * 相比 {@link #getActivity()}，此方法返回在 {@link #onAttach} 时缓存的引用，
     * 即使 Fragment 已经 detach 也可使用（但需注意 Activity 可能已销毁）。
     * </p>
     *
     * @return 关联的 Activity 实例，可能为 {@code null}
     */
    public Activity getActivitySafe() {
        return mActivity;
    }

    /**
     * Fragment 附加到 Activity 时回调，缓存 Activity 引用。
     *
     * @param context 附加的目标上下文
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    /**
     * 在 UI 线程上执行任务的安全方法。
     * <p>
     * 若当前 Activity 已分离（{@code getActivity() == null}），则跳过执行，防止空指针异常。
     * </p>
     *
     * @param task 待在 UI 线程执行的任务，不可为 {@code null}
     */
    protected void runOnUiThread(Runnable task) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(task);
    }
}
