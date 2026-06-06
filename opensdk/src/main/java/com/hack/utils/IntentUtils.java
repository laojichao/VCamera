package com.hack.utils;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.BaseBundle;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArrayMap;

import java.util.Map;
import java.util.Set;

/**
 * Intent 和 Bundle 相关的工具类。
 * <p>
 * 提供 Intent 包名提取、系统 Launcher Home 判断、Intent/Bundle 内容的
 * 简易字符串化等辅助方法。其中 {@link #toShortString(Bundle)} 通过反射访问
 * Bundle 内部的 {@code mMap} 字段，实现对未反序列化 Bundle 数据的调试输出。
 * </p>
 */
public class IntentUtils {

    /**
     * 从 Intent 中提取目标包名。
     * <p>优先从 ComponentName 中获取包名，若不存在则返回 Intent 的 package 属性。</p>
     *
     * @param intent 待提取包名的 Intent，允许为 {@code null}
     * @return 目标包名，若 Intent 为 {@code null} 或无法获取则返回 {@code null}
     */
    public static String getPackage(Intent intent) {
        if (intent == null) {
            return null;
        }
        ComponentName cnn = intent.getComponent();
        if (cnn != null) {
            return cnn.getPackageName();
        }
        return intent.getPackage();
    }

    /**
     * 判断 Intent 是否为系统 Launcher 主页启动意图。
     * <p>当 Intent 的 Action 为 {@link Intent#ACTION_MAIN} 且包含
     * {@link Intent#CATEGORY_HOME} 分类时判定为系统 Launcher Home。</p>
     *
     * @param intent 待判断的 Intent，允许为 {@code null}
     * @return {@code true} 表示是系统 Launcher Home 意图，否则返回 {@code false}
     */
    public static boolean isSysLauncherHome(Intent intent) {
        if (intent == null) {
            return false;
        }

        return intent.getAction() != null && TextUtils.equals(intent.getAction(), Intent.ACTION_MAIN)
                && intent.hasCategory(Intent.CATEGORY_HOME);
    }

    /**
     * 将 Intent 转换为简易调试字符串。
     * <p>提取 Intent 中 extras Bundle 的内容并格式化输出。若 extras 为 {@code null}，
     * 则返回空字符串的表示。</p>
     *
     * @param intent 待转换的 Intent，允许为 {@code null}
     * @return 格式化后的字符串表示，若 Intent 为 {@code null} 则返回 {@code null}
     */
    public static String toShortString(Intent intent) {
        if (intent != null) {
            StringBuilder builder = new StringBuilder();
            if (intent.getExtras() != null) {
                builder.append(toShortString(intent.getExtras()));
            }
            return builder.toString();
        }
        return null;
    }

    /**
     * 将 Bundle 转换为简易调试字符串。
     * <p>通过反射访问 Bundle 内部的 {@code mMap}（{@link ArrayMap}）字段，
     * 遍历所有键值对并格式化输出。支持递归处理嵌套的 {@link Bundle} 和 {@link Intent}。
     * 调用前会先触发 Bundle 的反序列化以确保数据可用。</p>
     *
     * @param bundle 待转换的 Bundle，允许为 {@code null}
     * @return 格式化后的字符串表示，若 Bundle 为 {@code null} 或反射失败则返回 {@code null}
     */
    public static String toShortString(Bundle bundle) {
        if (bundle != null) {
            bundle.setClassLoader(IntentUtils.class.getClassLoader());
            bundle.containsKey("test");//force unparcel
            if (bundle != null && bundle instanceof BaseBundle) {
                RefUtils.FieldRef<ArrayMap<String, Object>> filed_mMap =
                        new RefUtils.FieldRef(BaseBundle.class, false, "mMap");
                ArrayMap<String, Object> mMap = filed_mMap.get(bundle);
                if (mMap != null) {
                    StringBuilder builder = new StringBuilder("{<- ");
                    Set<Map.Entry<String, Object>> entrySet = mMap.entrySet();
                    for (Map.Entry<String, Object> entry : entrySet) {
                        builder.append("[" + entry.getKey());
                        builder.append(":");
                        Object value = entry.getValue();
                        if (value instanceof Bundle) {
                            builder.append(toShortString((Bundle)value));
                        } else if (value instanceof Intent) {
                            builder.append(toShortString((Intent)value));
                        } else {
                            builder.append(value);
                        }
                        builder.append("]");
                    }
                    builder.append(" ->}");
                    return builder.toString();
                }
            }
        }
        return null;
    }
}
