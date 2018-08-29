package com.skylin.uav.drawforterrain.login;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.blankj.utilcode.util.AppUtils;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.HttpUrlTool;
import com.skylin.uav.drawforterrain.checksupdata.Ban;
import com.skylin.uav.drawforterrain.checksupdata.JsonGenericsSerializator;
import com.skylin.uav.drawforterrain.nofly.CivilAirport;
import com.skylin.uav.drawforterrain.nofly.NoFlyZoneLocalDataSource;
import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.drawforterrain.select.home.HomeActivity;
import com.skylin.uav.drawforterrain.setting_channel.ObsMasterBan;
import com.skylin.uav.drawforterrain.setting_channel.ParticularsActivity;
import com.skylin.uav.drawforterrain.util.GsonUtil;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.FileCallBack;
import com.zhy.http.okhttp.callback.GenericsCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import sjj.alog.Log;
import task.model.Pair;
import test.xuan.liu.com.skylinserverauth.AuthUtil;
import test.xuan.liu.com.skylinserverauth.ReqeustAuth;

import static com.blankj.utilcode.util.AppUtils.launchApp;

/**
 * Created by wh on 2017/4/08
 */
public class LoginActivity extends BaseActivity {

    private LinearLayout ll_down;
    private QMUITipDialog downWit;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }

        BlueToochActivity.fullScreen(this);
        setContentView(R.layout.activity_login);

        downWit = new QMUITipDialog.Builder(LoginActivity.this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.LoginActivity_tv1))
                .create();

        ll_down = findViewById(R.id.ll_down);


        getAirport();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onPause() {
        super.onPause();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        LogUtils.e("loginactivity onDestroy");
    }


    public void downloadFile(View v) {

        downWit.show();

        String url = APP.url + "/work2.0/Public/store/?service=App.downApp&id=10";
        String patch = "sdcard/Android/data/com.skylin.uav.nongtiancehui/cache/download/";
        OkHttpUtils
                .get()
                .url(url)
                .build()
//                .execute(new FileCallBack(Environment.getExternalStorageDirectory().getAbsolutePath(), "天麒服务.apk") {
                .execute(new FileCallBack(patch, "天麒服务.apk") {

                    @Override
                    public void onBefore(Request request, int id) {
//                        sjj.alog.Log.e("onBefore");
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
//                        mProgressBar.setProgress((int) progress*-1);
//                        Log.e("ok", "inProgress :" + (progress * 100) + "  " + total);
                        float f = progress * 100;
                        DecimalFormat df = new DecimalFormat("#0.0");
                        String s = df.format(f) + "%";
//                        animPopu.setProgress(s);
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
//                        sjj.alog.Log.e("", e);
                    }

                    @Override
                    public void onResponse(final File file, int id) {
                        try {
                            downWit.dismiss();
                            new QMUIDialog.MessageDialogBuilder(LoginActivity.this)
                                    .setTitle(getString(R.string.LoginActivity_tv2))
                                    .setMessage(getString(R.string.LoginActivity_tv3))
                                    .addAction(getString(R.string.continuePopu_tv9), new QMUIDialogAction.ActionListener() {
                                        @Override
                                        public void onClick(QMUIDialog dialog, int index) {
                                            file.canRead();
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            Uri data;
                                            // 判断版本大于等于7.0
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                                // 是在清单文件中配置的authorities
                                                data = FileProvider.getUriForFile(getBaseContext(), "com.skylin.uav.nongtiancehui.fileprovider", file);
                                                // 给目标应用一个临时授权
                                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                            } else {
                                                data = Uri.fromFile(file);
                                            }
                                            intent.setDataAndType(data, "application/vnd.android.package-archive");
                                            startActivity(intent);
                                        }
                                    })
                                    .show();
//                            Log.e("ok", "onResponse :" + file.getAbsolutePath() + "    " + file.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private AuthUtil util;

    private void startNextActivity() {
        util = new AuthUtil(null, this, new AuthUtil.Iwork() {

            @Override
            public void authSuccess(@Nullable List<? extends ReqeustAuth> macList) {

                handler.sendEmptyMessage(1);
            }

            @Override
            public void requestFaiure(@Nullable Throwable t) {
                if (t != null) {
                    if (t.getMessage().equals("没有授权")) {
                        launchApp("app.com.skylinservice");
                        handler.sendEmptyMessage(0);
                        return;
                    }

                    if (t.getMessage().equals("授权失败")) {
                        launchApp("app.com.skylinservice");
                        handler.sendEmptyMessage(0);
                        return;
                    }
                }

                if (AppUtils.isInstallApp("app.com.skylinservice")) {
//                    new QMUIDialog.MessageDialogBuilder(LoginActivity.this)
//                            .setTitle("错误")
//                            .setMessage("错误 ：" + t + ",请去天麒服务处理后再回来，10秒后退出")
//                            .addAction("知道了", new QMUIDialogAction.ActionListener() {
//                                @Override
//                                public void onClick(QMUIDialog dialog, int index) {
//                                    dialog.dismiss();
//                                }
//                            })
//                            .show();
//                    handler.sendEmptyMessageDelayed(0, 10000);
                    handler.sendEmptyMessage(1);
                } else {
                    ll_down.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void saveTeam(long teamid, String teamName) {
//                sjj.alog.Log.e("teamid =" + teamid + "   teamName  =" + teamName);

                if (teamid == 0 | TextUtils.isEmpty(teamName)) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setTitle(getString(R.string.LoginActivity_tv4))
                            .setMessage(getString(R.string.LoginActivity_tv5))
                            .setPositiveButton(getString(R.string.LoginActivity_tv6), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    launchApp("app.com.skylinservice");
                                    handler.sendEmptyMessage(0);
                                }
                            });
                    builder.setCancelable(false); //设置点击对话框外部区域，关闭对话框，默认
                    builder.create();
                    builder.show();
                } else {
                    BaseActivity.teamid = (int) teamid;
                    BaseActivity.teamName = teamName;
                }
            }

            @Override
            public void saveLogintTable(@NotNull test.xuan.liu.com.skylinserverauth.LoginTable logintable) {
//                sjj.alog.Log.e(logintable.toString() + "\n" + logintable.getToken());
                if (TextUtils.isEmpty(logintable.getToken()) | logintable.getUserId() == 0) {
                    launchApp("app.com.skylinservice");
                    handler.sendEmptyMessage(0);
                } else {
                    if (TextUtils.isEmpty(logintable.getUsername())) {
                        BaseActivity.username = "默认昵称";
                    } else {
                        BaseActivity.username = logintable.getUsername();
                    }
                    BaseActivity.TOKEN = logintable.getToken();
                    BaseActivity.userId = logintable.getUserId();

                }
            }
        });
        util.beginAuth();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        util.checkuOnactivityResult(data);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    System.exit(0);
                    break;
                case 1:
                    if (TextUtils.isEmpty(BaseActivity.TOKEN) | TextUtils.isEmpty(BaseActivity.username)
                            | TextUtils.isEmpty(BaseActivity.teamName) | BaseActivity.teamid == 0) {
                        ToastUtil.show(getString(R.string.LoginActivity_tv7));
                        SystemClock.sleep(1000);
                        finish();
                        return;
                    }

                    try {
//
                        analysisAriport();

                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
//                        startActivity(new Intent(LoginActivity.this, StartActivity.class));
//                        startActivity(new Intent(LoginActivity.this, ParticularsActivity.class));

                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();

                        Log.e(e);
                        ToastUtil.show(getString(R.string.LoginActivity_tv8));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public void getUser() {
//        String url = APP.url+"/appEdition.php?act=edition&appName=植保测绘";
        String url = APP.url + "/work2.0/Public/store/?service=App.getAppInfo&id=2";
        OkHttpUtils
                .post()
                .url(url)
//                .addParams("name","植保测绘")
                .build()
                .execute(new GenericsCallback<Ban>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        startNextActivity();
                    }

                    @Override
                    public void onResponse(Ban response, int id) {
                        if (response.getRet() == 200) {
//                            LogUtils.e(response);
                            for (int i = 0; i < response.getData().size(); i++) {
                                String versionCode = response.getData().get(i).getVersionCode();
                                if (hasUpdate(Integer.parseInt(versionCode))) {
                                    try {

                                        AlertDialog dialog = new AlertDialog.Builder(LoginActivity.this)
                                                .setTitle(getString(R.string.LoginActivity_tv9))
                                                .setMessage(getString(R.string.LoginActivity_tv10))
                                                .setPositiveButton(getString(R.string.continuePopu_tv9), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        downFile();
                                                    }
                                                })
                                                .setNegativeButton(getString(R.string.DotActivity_tv9), new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        startNextActivity();
                                                    }
                                                })
                                                .create();
                                        dialog.show();
                                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.qmui_config_color_blue));
                                        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.qmui_config_color_blue));

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        startNextActivity();
                                    }
                                    return;
                                } else if (i == response.getData().size() - 1) {
                                    startNextActivity();
                                    return;
                                }
                            }
                            startNextActivity();
                        } else {
                            startNextActivity();
                        }
                    }
                });
    }

    public boolean hasUpdate(int version) {
        try {
            PackageManager manager = APP.getContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(APP.getContext().getPackageName(), 0);
            return version > info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public void downFile() {
        downWit.show();
        String url = APP.url + "/work2.0/Public/store/?service=App.downApp&id=2";
        String patch = "sdcard/Android/data/com.skylin.uav.nongtiancehui/cache/download/";
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new FileCallBack(patch, "农田测绘.apk") {

                    @Override
                    public void onBefore(Request request, int id) {
                    }

                    @Override
                    public void inProgress(float progress, long total, int id) {
//                        mProgressBar.setProgress((int) progress*-1);
//                        Log.e("ok", "inProgress :" + (progress * 100) + "  " + total);
                        float f = progress * 100;
                        DecimalFormat df = new DecimalFormat("#0.0");
                        String s = df.format(f) + "%";
                    }

                    @Override
                    public void onError(Call call, Exception e, int id) {
//                        sjj.alog.Log.e("error  " + e.getMessage());
                    }

                    @Override
                    public void onResponse(final File file, int id) {
                        try {
                            downWit.dismiss();
                            file.canRead();
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri data;
                            // 判断版本大于等于7.0
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                // 是在清单文件中配置的authorities
                                data = FileProvider.getUriForFile(getBaseContext(), "com.skylin.uav.nongtiancehui.fileprovider", file);
                                // 给目标应用一个临时授权
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } else {
                                data = Uri.fromFile(file);
                            }
                            intent.setDataAndType(data, "application/vnd.android.package-archive");
                            startActivity(intent);
                            System.exit(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    public void getAirport() {

        final String url = APP.url + "/v3/api/work/no-fly-zones";

        OkHttpUtils
                .get()
                .url(url)
//                .addParams("name","植保测绘")
                .build()
                .execute(new GenericsCallback<AirportBan>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {

//                        startNextActivity();
                        Log.e(e);

                        getUser();
                    }

                    @Override
                    public void onResponse(AirportBan it, int id) {
                        try {
//                            Log.e(it);
                            if (it.getRet() != 200) {

                            } else {

                                List<List<Point>> airportList = new ArrayList<>();
                                JSONArray lists = new JSONArray();

                                for (AirportBan.DataBean bean : it.getData()) {

                                    List<Point> airPort = new ArrayList<>();
                                    AirportBan.DataBean.ZonesBean zones = bean.getZones();

                                    List<Double> a11 = zones.getA1();
                                    Point a1 = new Point(a11.get(1), a11.get(0));

                                    List<Double> a21 = zones.getA2();
                                    Point a2 = new Point(a21.get(1), a21.get(0));

                                    List<Double> a31 = zones.getA3();
                                    Point a3 = new Point(a31.get(1), a31.get(0));

                                    List<Double> a41 = zones.getA4();
                                    Point a4 = new Point(a41.get(1), a41.get(0));

                                    List<Double> b11 = zones.getB1();
                                    Point b1 = new Point(b11.get(1), b11.get(0));

                                    List<Double> b21 = zones.getB2();
                                    Point b2 = new Point(b21.get(1), b21.get(0));

                                    List<Double> b31 = zones.getB3();
                                    Point b3 = new Point(b31.get(1), b31.get(0));

                                    List<Double> b41 = zones.getB4();
                                    Point b4 = new Point(b41.get(1), b41.get(0));

                                    List<Double> c11 = zones.getC1();
                                    Point c1 = new Point(c11.get(1), c11.get(0));

                                    List<Double> c21 = zones.getC2();
                                    Point c2 = new Point(c21.get(1), c21.get(0));

                                    List<Double> c31 = zones.getC3();
                                    Point c3 = new Point(c31.get(1), c31.get(0));

                                    List<Double> c41 = zones.getC4();
                                    Point c4 = new Point(c41.get(1), c41.get(0));

                                    airPort.add(a1);
                                    airPort.add(a2);
                                    airPort.add(a3);
                                    airPort.add(a4);
                                    airPort.add(b1);
                                    airPort.add(b2);
                                    airPort.add(b3);
                                    airPort.add(b4);
                                    airPort.add(c1);
                                    airPort.add(c2);
                                    airPort.add(c3);
                                    airPort.add(c4);
                                    airportList.add(airPort);

                                    JSONArray barrier1 = new JSONArray();
                                    for (Point point : airPort) {
                                        barrier1.put(new JSONArray().put(point.getLatitude()).put(point.getLongitude()));
                                    }
                                    lists.put(barrier1);
                                }

                                saveSid("ariport", lists.toString());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            getUser();
                        }
                        getUser();
                    }
                });
    }

    public void analysisAriport() throws Exception {
//        NoFlyZoneLocalDataSource flyZoneRepository = new NoFlyZoneLocalDataSource();
//        Pair<List<CivilAirport>, List<List<Point>>> call = flyZoneRepository.call();
//        BaseActivity.civilAirports = call.first;     //取得机场禁飞区列表
//        BaseActivity.second = call.second;

        String ariport = getSid("ariport");
//        Log.e("122   " + ariport + "  /n" + TextUtils.isEmpty(ariport));
        if (!TextUtils.isEmpty(ariport)) {
//            Log.e("32323");
            List<List<Point>> airportList = new ArrayList<>();
            JSONArray jsonArray = new JSONArray(ariport);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray jsonArray1 = jsonArray.getJSONArray(i);
                List<Point> list = new ArrayList<>();
                for (int j = 0; j < jsonArray1.length(); j++) {
                    JSONArray jsonArray2 = jsonArray1.getJSONArray(j);
                    double lat = jsonArray2.getDouble(0);
                    double lon = jsonArray2.getDouble(1);
                    list.add(new Point(lon,lat));
                }
                airportList.add(list);
            }
            BaseActivity.second = airportList;
        }
    }

}
