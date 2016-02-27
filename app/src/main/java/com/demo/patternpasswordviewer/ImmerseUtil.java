package com.demo.patternpasswordviewer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.reflect.Method;

/**
 * Created by wuyr on 2/21/16 4:00 PM.
 */
public class ImmerseUtil {

    /**
     * 默认的SystemBar背景色
     */
    private static final int DEFAULT_COLOR = Color.parseColor("#009688");
    private static final String STATUS_BAR_HEIGHT = "status_bar_height",
            NAVIGATION_BAT_HEIGHT = "navigation_bar_height",
            DIMEN = "dimen", ANDROID = "android";

    private ImmerseUtil() {
    }

    /**
     * 用默认的颜色作为SystemBar的背景色
     * @param activity 要实现沉浸的activity
     */
    public static void setImmerseBar(Activity activity) {
        setImmerseBar(activity, DEFAULT_COLOR);
    }

    /**
     * 使用指定的颜色作为SystemBar的背景色
     * @param activity 要实现沉浸的activity
     * @param color 指定的颜色
     */
    public static void setImmerseBar(Activity activity, int color) {
        //设置透明SystemBar
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        ViewGroup vg = (ViewGroup) activity.getWindow().getDecorView();

        //初始化顶部
        View statusBar = new View(activity);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, getStatusBarHeight(activity));
        lp.gravity = Gravity.TOP;
        statusBar.setBackgroundColor(color);
        statusBar.setLayoutParams(lp);
        vg.addView(statusBar);

        //初始化底部
        if (isHasNavigationBar(activity)) {
            View navigationBar = new View(activity);
            FrameLayout.LayoutParams lp2 = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, getNavigationBarHeight(activity));
            lp2.gravity = Gravity.BOTTOM;
            navigationBar.setBackgroundColor(color);
            navigationBar.setLayoutParams(lp2);
            vg.addView(navigationBar);
        }
    }

    /**
     * 判断设备是否拥有底部导航栏
     * @param context 上下文
     * @return 拥有则返回true， 反之亦然
     */
    public static boolean isHasNavigationBar(Context context) {
        boolean isHasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", ANDROID);
        if (id > 0)
            isHasNavigationBar = rs.getBoolean(id);
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                isHasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                isHasNavigationBar = true;
            }
        } catch (Exception e) {
            Log.w(ImmerseUtil.class.getSimpleName(), e.toString(), e);
        }
        return isHasNavigationBar;
    }

    /**
     * 获取底部导航栏高度
     * @param context 上下文
     * @return 导航栏的高度
     */
    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = 0;
        Resources rs = context.getResources();
        int id = rs.getIdentifier(NAVIGATION_BAT_HEIGHT, DIMEN, ANDROID);
        if (id > 0 && isHasNavigationBar(context))
            navigationBarHeight = rs.getDimensionPixelSize(id);
        return navigationBarHeight;
    }

    /**
     * 获取状态栏高度
     * @param context 上下文
     * @return 状态栏的高度
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier(STATUS_BAR_HEIGHT, DIMEN, ANDROID);
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
