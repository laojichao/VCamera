package com.hack;

import android.util.Log;

/**
 * 统一日志工具类。
 * <p>
 * 对 {@link android.util.Log} 的封装，提供简洁的日志输出方法，
 * 支持格式化字符串输出。所有方法均为静态方法，便于全局调用。
 * </p>
 */
public class Slog {

    /**
     * 以 DEBUG 级别输出日志（等同于 {@link #d(String, String)}）。
     *
     * @param tag    日志标签，用于标识日志来源
     * @param format 日志内容
     */
    public static void print(String tag, String format) {
        Log.d(tag, format);
    }

    /**
     * 以 DEBUG 级别输出日志。
     *
     * @param tag    日志标签
     * @param format 日志内容
     */
    public static void d(String tag, String format) {
        Log.d(tag, format);
    }

    /**
     * 以 VERBOSE 级别输出日志。
     *
     * @param tag    日志标签
     * @param format 日志内容
     */
    public static void v(String tag, String format) {
        Log.v(tag, format);
    }

    /**
     * 以 ERROR 级别输出日志。
     *
     * @param tag    日志标签
     * @param format 日志内容
     */
    public static void e(String tag, String format) {
        Log.e(tag, format);
    }

    /**
     * 以 WARN 级别输出日志。
     *
     * @param tag    日志标签
     * @param format 日志内容
     */
    public static void w(String tag, String format) {
        Log.w(tag, format);
    }

    /**
     * 以 INFO 级别输出日志。
     *
     * @param tag    日志标签
     * @param format 日志内容
     */
    public static void i(String tag, String format) {
        Log.i(tag, format);
    }

    /**
     * 以 ERROR 级别输出格式化日志。
     *
     * @param tag    日志标签
     * @param msg    格式化模板，包含 {@code %s}、{@code %d} 等占位符
     * @param format 格式化参数
     */
    public static void e(String tag, String msg, Object... format) {
        Log.e(tag, String.format(msg, format));
    }

    /**
     * 以 WARN 级别输出格式化日志。
     *
     * @param tag    日志标签
     * @param msg    格式化模板
     * @param format 格式化参数
     */
    public static void w(String tag, String msg, Object... format) {
        Log.w(tag, String.format(msg, format));
    }

    /**
     * 以 INFO 级别输出格式化日志。
     *
     * @param tag    日志标签
     * @param msg    格式化模板
     * @param format 格式化参数
     */
    public static void i(String tag, String msg, Object... format) {
        Log.i(tag, String.format(msg, format));
    }

    /**
     * 以 DEBUG 级别输出格式化日志。
     *
     * @param tag    日志标签
     * @param msg    格式化模板
     * @param format 格式化参数
     */
    public static void d(String tag, String msg, Object... format) {
        Log.d(tag, String.format(msg, format));
    }

    /**
     * 以 VERBOSE 级别输出格式化日志。
     *
     * @param tag    日志标签
     * @param msg    格式化模板
     * @param format 格式化参数
     */
    public static void v(String tag, String msg, Object... format) {
        Log.v(tag, String.format(msg, format));
    }

    /**
     * 判断当前是否可输出日志。
     * <p>
     * 当全局调试开关 {@link Features#DEBUG} 为 {@code true} 时返回 {@code true}，
     * 否则返回 {@code false}。
     * </p>
     *
     * @param tag     日志标签（当前未使用，保留供扩展）
     * @param verbose 日志级别（当前未使用，保留供扩展）
     * @return 如果允许输出日志则返回 {@code true}，否则返回 {@code false}
     */
    public static boolean iSloggable(String tag, int verbose) {
        if (Features.DEBUG) {
            return true;
        }
        return false;
    }
}
