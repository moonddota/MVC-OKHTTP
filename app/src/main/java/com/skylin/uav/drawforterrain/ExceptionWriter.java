package com.skylin.uav.drawforterrain;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import com.skylin.uav.drawforterrain.util.GsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sjj.alog.Log;

public class ExceptionWriter {
    private Throwable exception;
    private SimpleDateFormat simpleFormatter = new SimpleDateFormat("hh:mm:ss");

    public ExceptionWriter(Throwable ex) {
        this.exception = ex;
    }

    public void saveStackTraceToSD() {
        try {
            File file = new File(Environment.getExternalStorageDirectory(), "skylin/cehui/debug" + simpleFormatter.format(new Date()) + ".log");
            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            PrintStream out = new PrintStream(fileOutputStream);
            exception.printStackTrace(out);
            out.close();
            sendError();
        } catch (Exception excep) {
            excep.printStackTrace();
        }
    }

    private void sendError() {
        if (TextUtils.isEmpty(BaseActivity.TOKEN)) return;
        List<Object> infoList = makdeData(APP.getContext(), exception);
        final Map<String, String> map = new HashMap<>();
        map.put("data", Base64.encodeToString(GsonUtils.createGsonString(infoList).getBytes(), Base64.DEFAULT));
        final String url = APP.url + "/work2.0/Public/?service=Log.upDebug&token=" + BaseActivity.TOKEN;

        new Thread(new Runnable() {
            @Override
            public void run() {
                String result = HttpUrlTool.submitPostData(url, map, "utf-8");
                JSONObject json = null;
                try {
                    json = new JSONObject(result);
                    Log.e(json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).start();

//        ToastUtil.show("系统奔溃了,正在上传日志");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    // 将时间戳转为字符串
    public static String getStrTime(long cc_time) {
        String re_StrTime = null;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
// 例如：cc_time=1291778220
        long lcc_time = Long.valueOf(cc_time);
        re_StrTime = sdf.format(new Date(lcc_time * 1000L));


        return re_StrTime;
    }


    public static List<Object> makdeData(Context context, Throwable ex) {


        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("packageName",getPackageName(context));
            jsonObject.put("versionCode",getVersionCode(context));
            jsonObject.put("versionName",getVersionName(context));
            jsonObject.put("appName",getAppName(context));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String exinfo = android.util.Log.getStackTraceString(ex);
        List<Object> list = new ArrayList<>();
        String time = getStrTime(System.currentTimeMillis() / 1000);
        String appInfo = getPackageName(context)  + "  "+ getAppName(context) +"  "+ getVersionName(context) ;
        String pathInfo = ",\nSDK版本:" + Build.VERSION.SDK_INT + ",\n系统版本:" + android.os.Build.VERSION.RELEASE;
        String hardwareInfo = android.os.Build.MODEL;
        Map<String, String> map = new HashMap<>();

        map.put("uid", BaseActivity.userId+"");
        map.put("time", time);
        map.put("appInfo", jsonObject.toString());
        map.put("pathInfo", pathInfo);
        map.put("hardwareInfo", hardwareInfo);
        map.put("debugInfo", exinfo);
        list.add(map);
        return list;
    }

    private static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本号
     */
    public static String  getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.packageName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

}
