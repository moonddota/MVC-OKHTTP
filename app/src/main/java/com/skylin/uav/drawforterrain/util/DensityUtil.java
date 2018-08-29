package com.skylin.uav.drawforterrain.util;

import com.skylin.uav.drawforterrain.APP;

/**
 * Created by sjj on 2017/5/9.
 */

public class DensityUtil {
    public static int dp2Px(double value) {
        final float scale = APP.getContext().getResources().getDisplayMetrics().density;
        return (int) Math.round(value * scale);
    }
    public static int sp2px(float value) {
        final float fontScale =APP.getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) Math.round(value * fontScale);

    }


    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     */
    public static int px2dip(float pxValue) {
        final float scale = APP.getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    public static int dip2px(float dipValue) {
        final float scale = APP.getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static int px2sp(float pxValue) {
        final float fontScale = APP.getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    /**
     * 将sp值转换为px值，保证文字大小不变
     */
//    public static int sp2px(float spValue) {
//        context = APP.getContext();
//        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
//        return (int) (spValue * fontScale + 0.5f);
//    }

}
