package com.skylin.uav.drawforterrain.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import java.util.Locale;

public class LanguageUtil {

    /**
     * 设置语言
     *
     * @param lauType
     */
    public static  void set(Context context,String lauType) {
        // 本地语言设置
        Locale myLocale = new Locale(lauType);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

    }

    public static Locale get(){
        String lang = Locale.getDefault().getLanguage();
        if (lang.equals(Locale.ENGLISH.getLanguage())){
            lang = Locale.ENGLISH.getLanguage();
        }else {
            lang = Locale.SIMPLIFIED_CHINESE.getLanguage();
        }
        Locale locale = new Locale(lang);
        return locale;
    }
}
