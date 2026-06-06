package check.env;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.io.File;

import check.env.databinding.ActivityMainBinding;

/**
 * 环境检测示例 Activity。
 * <p>
 * 该 Activity 用于演示和验证应用运行环境的完整性检测，通过 Java 层和 Native 层
 * 两种方式分别检查文件路径是否存在，并将检测结果展示在界面上，帮助开发者识别
 * 文件系统是否被挂载劫持或虚拟化。
 * </p>
 * <p>
 * 加载本地 {@code libenv.so} 库，通过 JNI 调用 Native 方法进行更底层的路径检测，
 * 以规避 Java 层可能被 Hook 篡改的风险。
 * </p>
 *
 * @author hack
 */
public class MainActivity extends AppCompatActivity {

    /**
     * 加载本地 Native 库 {@code libenv.so}，用于提供 JNI 方法支持。
     * 在类加载时执行，确保后续 Native 方法调用时库已被加载到内存。
     */
    static {
        System.loadLibrary("env");
    }

    /** ViewBinding 实例，用于访问布局中的视图组件 */
    private ActivityMainBinding binding;

    /**
     * Activity 创建时的生命周期回调。
     * <p>
     * 初始化视图绑定，分别通过 Java {@link File#exists()} 和
     * Native {@link #isPathReallyExist(String)} 两种方式检测应用私有文件目录是否存在，
     * 并将检测结果拼接为文本展示在 {@code sampleText} 控件中。
     * </p>
     *
     * @param savedInstanceState 如果 Activity 之前被销毁，此参数包含其保存的状态数据；
     *                           首次创建时为 {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView tv = binding.sampleText;
        // 获取应用内部文件目录的绝对路径，作为检测目标
        String path = getFilesDir().getAbsolutePath();
        StringBuffer sb = new StringBuffer();
        sb.append("=====Check Result=====" + "\n");
        // 显示待检测的目标路径
        sb.append("Files Dir path:" + path + "\n");
        // Java 层路径检测：通过标准 File.exists() 判断，可能被 Hook 篡改结果
        sb.append("isPathReallyExist By Java:" + new File(path).exists() + "\n");
        // Native 层路径检测：通过 JNI 调用底层系统调用，更难被 Hook 篡改
        sb.append("isPathReallyExist By Native:" + (isPathReallyExist(path)) + "\n");
        tv.setText(sb.toString());
    }

    /**
     * Native 方法：通过 JNI 在底层检测指定路径是否真实存在于文件系统中。
     * <p>
     * 与 Java 层的 {@link File#exists()} 不同，该方法直接调用系统级接口进行检测，
     * 可有效规避 Xposed/LSPosed 等框架对 Java 层方法的 Hook 篡改，用于检测
     * 文件系统是否被虚拟化或挂载劫持。
     * </p>
     * <p>
     * 对应的 Native 实现位于 {@code libenv.so} 中。
     * </p>
     *
     * @param path 待检测的文件或目录的绝对路径
     * @return 路径真实存在返回 {@code 1}，不存在返回 {@code 0}；
     *         检测失败时的返回值由 Native 层定义
     */
    public native int isPathReallyExist(String path);
}