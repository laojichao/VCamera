package com.hack.opensdk;

import android.os.Bundle;

/**
 * 包管理观察者接口容器类。
 * <p>
 * 定义了应用安装与卸载事件的回调接口。
 * 这些接口通过反射机制被 Hack 引擎调用，因此不可混淆（ProGuard/R8 需 keep）。
 * </p>
 */
public class IPackageObserver {

    /**
     * 应用安装事件观察者接口。
     * <p>
     * 注册后，当应用安装完成时由 Hack 引擎通过反射回调
     * {@link #onPackageInstalled(String, int, String, Bundle, int)} 方法。
     * </p>
     * <b>注意：</b>该接口不可混淆，否则反射调用将失败。
     */
    public interface Install {

        /**
         * 应用安装完成时回调。
         *
         * @param basePackageName 已安装应用的包名
         * @param returnCode      安装结果码，成功时为 {@code PackageManager.INSTALL_SUCCEEDED}
         * @param msg             安装结果的描述信息
         * @param extras          附加信息 Bundle
         * @param userId          安装目标用户 ID
         */
        void onPackageInstalled(String basePackageName, int returnCode,
                                String msg, Bundle extras, int userId);
    }

    /**
     * 应用卸载事件观察者接口。
     * <p>
     * 注册后，当应用卸载完成时由 Hack 引擎通过反射回调
     * {@link #onPackageDeleted(String, int, String, int)} 方法。
     * </p>
     * <b>注意：</b>该接口不可混淆，否则反射调用将失败。
     */
    public interface Delete {

        /**
         * 应用卸载完成时回调。
         *
         * @param packageName 已卸载应用的包名
         * @param returnCode  卸载结果码，成功时为 {@code PackageManager.DELETE_SUCCEEDED}
         * @param msg         卸载结果的描述信息
         * @param userId      卸载目标用户 ID
         */
        void onPackageDeleted(String packageName, int returnCode, String msg, int userId);
    }
}
