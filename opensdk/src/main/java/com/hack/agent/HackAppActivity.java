package com.hack.agent;

import android.app.Activity;

/**
 * Hack 代理 Activity 基类。
 * <p>
 * 作为各种显示模式代理 Activity 的基类，通过多个静态内部类提供不同方向和透明度的 Activity 变体。
 * 这些内部类用于在 AndroidManifest 中声明，以支持不同场景下的 Activity 代理需求。
 * </p>
 * <p>
 * 内部类命名规则：
 * <ul>
 *   <li><b>P</b> - 竖屏（Portrait）普通模式</li>
 *   <li><b>PT</b> - 竖屏透明（Translucent）模式</li>
 *   <li><b>L</b> - 横屏（Landscape）普通模式</li>
 *   <li><b>LT</b> - 横屏透明模式</li>
 *   <li><b>B</b> - 背景（Behind）普通模式</li>
 *   <li><b>BT</b> - 背景透明模式</li>
 * </ul>
 * </p>
 */
public class HackAppActivity extends Activity {

    /** 竖屏普通模式代理 Activity */
    public static class P extends HackAppActivity {
    }

    /** 竖屏透明模式代理 Activity */
    public static class PT extends HackAppActivity {
    }

    /** 横屏普通模式代理 Activity */
    public static class L extends HackAppActivity {
    }

    /** 横屏透明模式代理 Activity */
    public static class LT extends HackAppActivity {
    }

    /** 背景普通模式代理 Activity */
    public static class B extends HackAppActivity {
    }

    /** 背景透明模式代理 Activity */
    public static class BT extends HackAppActivity {
    }
}
