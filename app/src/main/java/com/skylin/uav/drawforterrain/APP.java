package com.skylin.uav.drawforterrain;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import com.baidu.mapapi.SDKInitializer;
import com.blankj.utilcode.util.Utils;
import com.skylin.uav.drawforterrain.language.MultiLanguageUtil;
import com.skylin.uav.drawforterrain.util.AppFrontBackHelper;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import sjj.alog.Config;
import sjj.alog.Log;
import test.xuan.liu.com.skylinserverauth.HttpApiService;

/**
 * Created by wh on 2017/2/7.
 */

public class APP extends Application {

    public static final String url = HttpApiService.url;  //开发接口
    public static List<Activity> activities = new ArrayList<>();
    public static boolean is_Front = true;   //判断是否在前台还是在后天

    private static Context context;
    public static boolean is_change = false;
    public static String language;

    private Thread.UncaughtExceptionHandler exceptionHandler;
    private final Thread.UncaughtExceptionHandler dpExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            Log.e("Exception", ex);
            new ExceptionWriter(ex).saveStackTraceToSD();
//            exceptionHandler.uncaughtException(thread, ex);

            for (Activity activity : activities) {
                activity.finish();
            }
            activities.clear();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        SDKInitializer.initialize(getApplicationContext());
        ToastUtil.init(getApplicationContext());

        Config config = new Config();
        config.hold = true;
        config.holdLev = config.ERROR;
        config.multiple = true;
        config.dirName = "NLog";
        Config.init(config);

        exceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(dpExceptionHandler);

        Utils.init(this);

        IntentFilter filter = new IntentFilter(RestartReceiver.DEFAUT_TEAM_ID);
        registerReceiver(mReceiver, filter);


        AppFrontBackHelper helper = new AppFrontBackHelper();
        helper.register(APP.this, new AppFrontBackHelper.OnAppStatusListener() {
            @Override
            public void onFront() {
                is_Front = true;

                //应用切到前台处理
                android.util.Log.e("tag", "应用切到前台处理");
                if (is_change) {
                    is_change = false;
                    //            //重启app代码
                    Intent intent = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage(getBaseContext().getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }

            @Override
            public void onBack() {

                is_Front = false;
                //应用切到后台处理
                android.util.Log.e("tag", "应用切到后台处理");
            }
        });


        MultiLanguageUtil.init(this);
        //        MultiLanguageUtil.getInstance().setConfiguration();
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
//                MultiLanguageUtil.getInstance().setConfiguration();
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

        initLanguage();
        initOkHttp();
    }

    private void initLanguage() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        String s = locale.getLanguage();

        if (s.equals("en")) {
            language = "en-US";
        } else {
            language = "zh-CN";
        }
    }

    private void initOkHttp() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request().newBuilder()
                                .addHeader("Content-Language", APP.language)
                                .build();

                        return chain.proceed(request);
                    }
                })
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
//        MultiLanguageUtil.getInstance().setConfiguration();
    }


    public static Context getContext() {
        return context;
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String stringExtra = intent.getStringExtra("defaut_team_id");

            Log.e(stringExtra + "\n" + intent.getStringExtra("defaut_team_id").equals(BaseActivity.teamid + ""));
            if (BaseActivity.teamid != -1 &&
                    !intent.getStringExtra("defaut_team_id").equals(BaseActivity.teamid + "")) {

                Log.e("dsad");
                is_change = true;
            }
        }
    };

}
