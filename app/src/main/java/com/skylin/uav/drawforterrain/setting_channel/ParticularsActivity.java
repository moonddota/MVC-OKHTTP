package com.skylin.uav.drawforterrain.setting_channel;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.HttpUrlTool;
import com.skylin.uav.drawforterrain.checksupdata.JsonGenericsSerializator;
import com.skylin.uav.drawforterrain.dialog.Basedialog;
import com.skylin.uav.drawforterrain.fragment.OsmFragment;
import com.skylin.uav.drawforterrain.nofly.CivilAirport;
import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.drawforterrain.service.BluetoothLeService;
import com.skylin.uav.drawforterrain.setting_channel.db.DbBan;
import com.skylin.uav.drawforterrain.setting_channel.db.DbSQL;
import com.skylin.uav.drawforterrain.util.DateUtil;
import com.skylin.uav.drawforterrain.util.OrderUtils;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.skylin.uav.drawforterrain.views.MarqueeTextView;
import com.skylin.uav.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.GenericsCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.Call;

import static com.blankj.utilcode.util.AppUtils.launchApp;

/**
 * Created by Moon on 2018/2/22.
 */

public class ParticularsActivity extends BlueToochActivity implements View.OnClickListener {

    //    private MapFragment mapFragment;
    private OsmFragment mapFragment;

    private DrawerLayout drawerlayout;
    private LinearLayout nav_home, nav_connect, nav_his, nav_seting;
    private TextView versionNmae_particulars;
    private QMUIRoundButton abort;
    private ImageView top_show;
    private LinearLayout ll_top;
    private LinearLayout ll_top_show;
    private MarqueeTextView top_name;
    private LinearLayout top_icon;
    private LinearLayout top_channle;
    private ImageView top_channle_im;
    private TextView top_channle_tv;
    private ImageView top_rtk_im;
    private ImageView top_rtk_im_icon;
    private ImageView top_elec_im;
    private TextView top_elec_tv;
    private TextView head_tv_name;
    private TextView head_tv_time;
    private TextView head_tv_tid;

    private GGABan ggaBan;
    private static ArrayList<DownMappingBan.DataBean.InfoBean> point_list = new ArrayList<>();

    private Basedialog continuePopu;
    private Button warming_massage;


    public DbSQL sql;

    private ExecutorService threadPool;
    private Future<?> submit;

    private boolean readVersion = true;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BlueToochActivity.fullScreen(this);
        sql = new DbSQL(getBaseContext(), "teamid" + BaseActivity.teamid, null, 1);
        if (sql == null) {
            ToastUtil.show(getString(R.string.toast_spl_un));
            finish();
        }

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        setContentView(R.layout.activity_particulars);

        initMap();       //地图及定位
        initTopbar();    //顶部状态栏
        initSideslip();  //侧滑

        threadPool = Executors.newSingleThreadExecutor();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mapFragment.setLoaction(ggaBan);
            }
        }, 3000);

    }

    private void initSideslip() {
        versionNmae_particulars = findViewById(R.id.versionNmae_particulars);
        try {
            PackageManager manager = APP.getContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(APP.getContext().getPackageName(), 0);
            versionNmae_particulars.setText(getString(R.string.app_name) + info.versionName);
//            versionNmae_particulars.setText(getString(R.string.app_name) + " V1.0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        drawerlayout = findViewById(R.id.drawerlayout);
        nav_home = findViewById(R.id.nav_home);
        nav_home.setOnClickListener(this);
        nav_connect = findViewById(R.id.nav_connect);
        nav_connect.setOnClickListener(this);
        nav_his = findViewById(R.id.nav_his);
        nav_his.setOnClickListener(this);
        nav_seting = findViewById(R.id.nav_seting);
        nav_seting.setOnClickListener(this);
        abort = findViewById(R.id.abort);
        abort.setVisibility(View.INVISIBLE);
        head_tv_tid = findViewById(R.id.head_tv_tid);
        head_tv_tid.setText(BaseActivity.teamName);
        head_tv_time = findViewById(R.id.head_tv_time);
        head_tv_name = findViewById(R.id.head_tv_name);
        head_tv_name.setText(BaseActivity.username);

        drawerlayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                int hh = Integer.parseInt(DateUtil.getCurDate("HH"));
                if (hh > 6 && hh <= 8) {
                    head_tv_time.setText(getString(R.string.drawerlayout_tv1));
                } else if (hh > 8 && hh <= 12) {
                    head_tv_time.setText(getString(R.string.drawerlayout_tv2));
                } else if (hh > 12 && hh <= 19) {
                    head_tv_time.setText(getString(R.string.drawerlayout_tv3));
                } else {
                    head_tv_time.setText(getString(R.string.drawerlayout_tv4));
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void initTopbar() {

        top_name = (MarqueeTextView) findViewById(R.id.top_name);
//        top_name.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if (mConnected)
//                    ShowHighActivity.startShowHighActivity(ParticularsActivity.this);
//                else ToastUtil.show("蓝牙未连接");
//                return false;
//            }
//        });

        top_show = findViewById(R.id.top_show);
        ll_top_show = findViewById(R.id.ll_top_show);
        ll_top_show.setOnClickListener(this);

        ll_top = findViewById(R.id.ll_top);

        top_icon = findViewById(R.id.top_icon);
        top_channle = findViewById(R.id.top_channle);
        top_channle.setOnClickListener(this);
        top_channle_im = findViewById(R.id.top_channle_im);
        top_channle_tv = findViewById(R.id.top_channle_tv);
        top_rtk_im = findViewById(R.id.top_rtk_im);
        top_rtk_im_icon = findViewById(R.id.top_rtk_im_icon);
        top_elec_im = findViewById(R.id.top_elec_im);
        top_elec_tv = findViewById(R.id.top_elec_tv);

        setTopWhite();

        warming_massage = findViewById(R.id.warming_massage);
    }

    private void setTopWhite() {
        QMUIStatusBarHelper.setStatusBarDarkMode(this);  //状态栏白色

        top_name.setTextColor(getResources().getColor(R.color.white));
        top_channle_tv.setTextColor(getResources().getColor(R.color.white));
        top_elec_tv.setTextColor(getResources().getColor(R.color.white));

        top_show.setBackgroundResource(R.mipmap.icon_actionbar);
        top_channle_im.setBackgroundResource(R.mipmap.icon_path);
        top_rtk_im.setBackgroundResource(R.mipmap.icon_rtk);
        top_elec_im.setBackgroundResource(R.mipmap.icon_elec);
        ll_top.setBackgroundResource(R.drawable.shape_gradient);
    }

    private void setTopBlack() {

        QMUIStatusBarHelper.setStatusBarLightMode(this); //状态栏褐色

        top_name.setTextColor(getResources().getColor(R.color.text_gray3));
        top_channle_tv.setTextColor(getResources().getColor(R.color.green3));
        top_elec_tv.setTextColor(getResources().getColor(R.color.green3));

        top_show.setBackgroundResource(R.mipmap.icon_actionbar_black);
        top_channle_im.setBackgroundResource(R.mipmap.icon_path_black);
        top_rtk_im.setBackgroundResource(R.mipmap.icon_rtk_black);
        top_elec_im.setBackgroundResource(R.mipmap.icon_elec_black);
        ll_top.setBackgroundResource(R.color.tm);
    }

    public static ArrayList<DownMappingBan.DataBean.InfoBean> getList() {
        return point_list;
    }

    private void initMap() {
        findViewById(R.id.location_particulars).setOnClickListener(this);
        findViewById(R.id.map_model_particulars).setOnClickListener(this);

        mapFragment = new OsmFragment();
        FragmentManager FM = getFragmentManager();
        FragmentTransaction ft = FM.beginTransaction();
        ft.replace(R.id.map_replace, mapFragment);
        ft.commit();

        continuePopu = new Basedialog(this
                , R.style.MDialog
                , getString(R.string.continuePopu_tv1)
                , getString(R.string.continuePopu_tv2)
                , getString(R.string.continuePopu_tv3)
                , getString(R.string.continuePopu_tv4));
        continuePopu.setCanceledOnTouchOutside(true);
        continuePopu.setCancelable(true);
        continuePopu.setPopuCloseListener(new OnClick() {
            @Override
            public void onPopuClick(View view, int id) {
                switch (id) {
                    case R.id.tv_single_operation_dialog_confirm:
                        continuePopu.dismiss();
                        BlueToochActivity.boundaryList = null;
                        BlueToochActivity.obsList = null;
                        BlueToochActivity.backout_list = backout_list;
                        BlueToochActivity.title = null;
                        BlueToochActivity.Id = null;
                        BlueToochActivity.insertTime = null;
                        if (DotActivity.dotActivity != null) DotActivity.dotActivity.finish();
                        DotActivity.startDotActivity(ParticularsActivity.this, false, false, null);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                        break;
                    case R.id.tv_single_cancel:
                        continuePopu.dismiss();
                        if (DotActivity.dotActivity != null) DotActivity.dotActivity.finish();
                        DotActivity.startDotActivity(ParticularsActivity.this, true, false, null);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                        break;
                    default:
                        break;
                }
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.top_channle:
                if (!BlueToochActivity.mConnected) {
                    ToastUtil.show(getString(R.string.toast_1));
                    return;
                }
                if (!BaseActivity.workmoder.equals("手持杖")) {
                    ToastUtil.show(getString(R.string.toast_2));
                    return;
                }
                startActivity(new Intent(getBaseContext(), SetChannelActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                break;
            case R.id.nav_home:
                if (drawerlayout.isDrawerOpen(Gravity.LEFT)) {
                    drawerlayout.closeDrawer(Gravity.LEFT);
                }
                break;
            case R.id.nav_connect:
                permissionsing();
                startActivity(new Intent(getBaseContext(), EquipmentActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                break;
            case R.id.nav_his:
                if (MappingRecordActivity.activity != null) {
                    MappingRecordActivity.activity.finish();
                    MappingRecordActivity.activity = null;
                }
                startActivity(new Intent(getBaseContext(), MappingRecordActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                break;
            case R.id.nav_seting:
                if (!BlueToochActivity.mConnected) {
                    ToastUtil.show(getString(R.string.toast_3));
                    return;
                }
                if (!BaseActivity.workmoder.equals("手持杖")) {
                    ToastUtil.show(getString(R.string.toast_2));
                    return;
                }
                startActivity(new Intent(getBaseContext(), SetChannelActivity.class));
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                break;
            case R.id.location_particulars:
                mapFragment.setLoaction(ggaBan);
                break;
            case R.id.map_model_particulars:
                int i = mapFragment.setMapMopType();
                if (i == 1) {
                    setTopBlack();
                } else {
                    setTopWhite();
                }
                break;
            case R.id.ll_top_show:
                if (!drawerlayout.isDrawerOpen(Gravity.LEFT))
                    drawerlayout.openDrawer(Gravity.LEFT);
                break;
            default:
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            }
        }
    };



    private Runnable OrderRun = new Runnable() {
        @SuppressLint("NewApi")
        @Override
        public void run() {
            if (BlueToochActivity.mConnected) {
                OrderUtils.requestChannel();
                SystemClock.sleep(10);
                OrderUtils.requestSystems();
                SystemClock.sleep(10);
                OrderUtils.requestGGA();

                if (TextUtils.isEmpty(BaseActivity.workmoder)) {
                    warming_massage.setText(getString(R.string.ParticularsActivity_tv1));
                    warming_massage.setVisibility(View.VISIBLE);
                } else if (!BaseActivity.workmoder.equals("手持杖")) {
                    warming_massage.setText(getString(R.string.ParticularsActivity_tv2));
                    warming_massage.setVisibility(View.VISIBLE);
                } else {
                    warming_massage.setVisibility(View.INVISIBLE);
                }

                if (top_icon.getVisibility() == View.INVISIBLE)
                    top_icon.setVisibility(View.VISIBLE);

                if (BlueToochActivity.device != null)
                    if (!top_name.getText().toString().equals(BlueToochActivity.device.getName()))
                        top_name.setText(BlueToochActivity.device.getName());

                if (ggaBan != null) {
                    if (BaseActivity.channle < 8 && BaseActivity.channle >= 0)
                        top_channle_tv.setText(BaseActivity.channle + "");
                    if (ggaBan.getHdop() <= 1.3 && ggaBan.getRtk().equals("4")) {
                        top_rtk_im_icon.setBackgroundResource(R.mipmap.icon_rtk_work);
                    } else if (ggaBan.isEmpty()) {
                        top_rtk_im_icon.setBackgroundResource(R.mipmap.icon_rtk_null);
                    } else {
                        top_rtk_im_icon.setBackgroundResource(R.mipmap.icon_rtk_nowork);
                    }
                }
            } else {
                top_name.setText(getString(R.string.ParticularsActivity_tv3));
            }


            handler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        mapFragment.setMapCanClick(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onResume() {
        super.onResume();
        upMappingList();
        handler.removeCallbacks(OrderRun);
        handler.postDelayed(OrderRun, 2000);
        if (mConnected && readVersion) {
            readVersion();
            readVersion = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(OrderRun);

        if (submit != null)
            submit.cancel(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        handler.removeCallbacks(OrderRun);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//当keycode等于退出事件值时
            if (drawerlayout.isDrawerOpen(Gravity.LEFT)) {
                drawerlayout.closeDrawer(Gravity.LEFT);
            } else {
                toQuitTheApp();
            }
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    private boolean isExit = false;

    private void toQuitTheApp() {
        if (isExit) {
            System.exit(0);// 使虚拟机停止运行并退出程序
        } else {
            isExit = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isExit = false;
                }
            }, 2000);// 2秒后发送消息
            ToastUtil.show(getString(R.string.LoginActivity_tv14));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startDot(View view) {
        permissionsing();

        if (!BlueToochActivity.mConnected) {
            ToastUtil.show(getString(R.string.toast_3));
            return;
        }
        if (!BaseActivity.workmoder.equals("手持杖")) {
            ToastUtil.show(getString(R.string.toast_4));
            return;
        }

        if (boundaryList != null | obsList != null | backout_list != null) {
            continuePopu.show();
        } else {
            BlueToochActivity.boundaryList = null;
            BlueToochActivity.obsList = null;
            BlueToochActivity.backout_list = null;
            BlueToochActivity.title = null;
            BlueToochActivity.insertTime = null;
            BlueToochActivity.Id = null;
            DotActivity.startDotActivity(ParticularsActivity.this, false, false, null);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
        }
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(intent.getAction())) { //连接一个GATT服务
                    handler.removeCallbacks(OrderRun);
                    handler.postDelayed(OrderRun, 2000);
                    top_icon.setVisibility(View.VISIBLE);
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(intent.getAction())) {  //从GATT服务中断开连接
                    handler.removeCallbacks(OrderRun);
                    ggaBan = null;
                    top_name.setText(getString(R.string.ParticularsActivity_tv3));

                    warming_massage.setVisibility(View.INVISIBLE);
                    top_rtk_im_icon.setBackgroundResource(R.mipmap.icon_rtk_null);
                    top_channle_tv.setText("");
                    top_icon.setVisibility(View.INVISIBLE);

                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(intent.getAction())) {  //发现有可支持的服务
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.getAction())) {   //从服务中接受数据
                    ggaBan = (GGABan) intent.getSerializableExtra(BluetoothLeService.EXTRA_DATA);
                } else if (BluetoothLeService.AACTION_VERSIONCODE.equals(intent.getAction())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ParticularsActivity.this);
                    builder.setTitle(getString(R.string.continuePopu_tv1))
                            .setMessage(getString(R.string.continuePopu_tv5))
                            .setPositiveButton(getString(R.string.continuePopu_tv6), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    builder.setCancelable(false); //设置点击对话框外部区域，关闭对话框，默认
                    builder.create();
                    builder.show();
                }
            } catch (Exception e) {
            }
        }
    };


    private void upMappingList() {
        final String url = APP.url + "/work2.0/Public/?service=Mapping.upMapping&token=" + BaseActivity.TOKEN;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<DbBan> dbBans = sql.queryAll();
                    for (int i = 0; i < dbBans.size(); i++) {
                        DbBan dbBan = dbBans.get(i);
                        if (dbBan.getSync().equals("false")) {
                            Map<String, String> params = new HashMap<>();
                            if (!TextUtils.isEmpty(dbBan.getId())) {
                                params.put("Id", dbBan.getId());
                            }
                            params.put("title", dbBan.getTitle());
                            params.put("tid", dbBan.getTid());
                            params.put("province", dbBan.getProvince());
                            params.put("city", dbBan.getCity());
                            params.put("district", dbBan.getDistrict());
                            params.put("mappingBorder", dbBan.getMappingBorder());
                            params.put("obstacleBorder", dbBan.getObstacleBorder());
                            params.put("startBorder", dbBan.getStartBorder());
                            params.put("area", dbBan.getArea());
                            params.put("parentId", dbBan.getParentId());
                            params.put("type", dbBan.getType());
                            String result = HttpUrlTool.submitPostData(url, params, "utf-8");
                            JSONObject json = null;
//                            Log.e("ss "+ result);
                            json = new JSONObject(result);
                            String msg = json.getString("msg");
                            String ret = json.getString("ret");
                            String data = json.getString("data");
//                            Log.e(msg + "   " + ret + "  " + data);
                            if (ret.equals("200")) {
                                if (data.equals("[1]")) {
//                                    Log.e("修改成功");
                                } else if (data.equals("[0]")) {
//                                    Log.e("修改失败");
                                } else {
//                                    Log.e("上传成功  Id=" + data);
                                    String[] split = data.split("\"");
                                    dbBan.setId(split[1]);
                                }
                                dbBan.setSync("true");
                                sql.updateBan(dbBan);
                            } else {
                                if (ret.equals("409")) {
                                    BaseActivity.TOKEN = null;
                                } else if (ret.equals("408")) {
//                                    Log.e("尚未登陆");
                                }
                            }
                        }
                    }
                    getMappingList(false, "1");
                } catch (Exception e) {
                    getMappingList(false, "1");
                }
            }
        }).start();
    }


    private void getMappingList(final boolean is_up, String page_tag) {
        final String url = APP.url + "/work2.0/Public/?service=Mapping.getMappingList&token=" + BaseActivity.TOKEN;
        OkHttpUtils
                .post()//
                .url(url)//
                .addParams("type", "all")  //all  self   团队 自己
                .addParams("page", page_tag)
                .addParams("tid", BaseActivity.teamid + "")
                .build()//
                .execute(new GenericsCallback<DownMappingBan>(new JsonGenericsSerializator()) {
                    @Override
                    public void onError(Call call, Exception e, int id) {
//                        Log.e("联网失败 " + call.toString() + " e " + e.toString() + " id " + id);
                        readSQL();
                    }

                    @Override
                    public void onResponse(DownMappingBan ban, int id) {
                        List<DownMappingBan.DataBean.InfoBean> info = ban.getData().getInfo();
                        if (ban.getRet() == 200) {
                            for (int i = 0; i < info.size(); i++) {
                                DownMappingBan.DataBean.InfoBean infoBean = info.get(i);
                                infoBean.setSync(true);
                                DbBan n_dbBan1 = sql.queryPointById(infoBean.getId());
                                if (n_dbBan1 == null) {
                                    sql.addBan(new DbBan(infoBean, DateUtil.getCurDate("yyyy-MM-dd HH:mm:ss")));
                                } else {
                                    if (!n_dbBan1.getUpdate_time().equals(infoBean.getUpdate_time())) {
                                        sql.updateBan(new DbBan(infoBean, n_dbBan1.getInsertTime()));
                                    }
                                }
                            }
                        }
                        readSQL();
                    }
                });
    }

    private void readSQL() {
        if (submit != null) {
            submit.cancel(true);
        }
        submit = threadPool.submit(thread);
    }

    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                point_list.clear();
                ArrayList<DbBan> dbBans = sql.queryAll();   // 查询所有的DbBan
                ArrayList<ArrayList<Point>> draw_list = new ArrayList<>();
                for (int i = 0; i < dbBans.size(); i++) {
                    if (dbBans.get(i).getType().equals("border")) {
                        ArrayList<Point> boundary_List = new ArrayList<>();
                        DownMappingBan.DataBean.InfoBean infoBean = new DownMappingBan.DataBean.InfoBean(dbBans.get(i));
                        if (!infoBean.getSync()) return;
                        List<List<Double>> mappingBorder = infoBean.getMappingBorder();
                        for (int j = 0; j < mappingBorder.size(); j++) {
                            boundary_List.add(new Point(
                                    mappingBorder.get(j).get(1),
                                    mappingBorder.get(j).get(0)));
                        }

                        draw_list.add(boundary_List);
                        point_list.add(infoBean);
                    }
                }
                mapFragment.drawList(draw_list);
            } catch (Exception e) {
//                Log.e("", e);
            }
        }
    });

}
