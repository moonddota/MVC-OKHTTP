package com.skylin.uav.drawforterrain.select;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.andview.refreshview.XRefreshView;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.HttpUrlTool;
import com.skylin.uav.drawforterrain.adapter.MPagerAdapter;
import com.skylin.uav.drawforterrain.adapter.RecordAdapter;
import com.skylin.uav.drawforterrain.checksupdata.JsonGenericsSerializator;
import com.skylin.uav.drawforterrain.select.gps_hand.GpsHandActivity;
import com.skylin.uav.drawforterrain.select.hand.HandSelect_Activity;
import com.skylin.uav.drawforterrain.select.gps_uav.GpsUavActivity;
import com.skylin.uav.drawforterrain.setting_channel.DownMappingBan;
import com.skylin.uav.drawforterrain.setting_channel.OnClick;
import com.skylin.uav.drawforterrain.setting_channel.ParticularsActivity;
import com.skylin.uav.drawforterrain.setting_channel.db.DbBan;
import com.skylin.uav.drawforterrain.setting_channel.db.DbSQL;
import com.skylin.uav.drawforterrain.util.DateUtil;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.skylin.uav.drawforterrain.views.ViewPager;
import com.skylin.uav.R;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.GenericsCallback;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import sjj.alog.Log;

public class StartActivity extends BaseActivity implements View.OnClickListener {

    private Button but_start_mapping;
    private ViewPager start_viewpager;
    private TextView start_tv_rtkhand, start_tv_hand, start_tv_gpsuav, start_tv_gpshand;
    private QMUIRoundButton start_view_rtkhand, start_view_hand, start_view_gpsuav, start_view_gpshand;
    private DbSQL sql;

    private ArrayList<DownMappingBan.DataBean.InfoBean> list = new ArrayList<>();
    private ArrayList<DownMappingBan.DataBean.InfoBean> hand_list = new ArrayList<>();
    private ArrayList<DownMappingBan.DataBean.InfoBean> rtkhand_list = new ArrayList<>();
    private ArrayList<DownMappingBan.DataBean.InfoBean> gpshand_list = new ArrayList<>();
    private ArrayList<DownMappingBan.DataBean.InfoBean> gpsuav_list = new ArrayList<>();
    private RecyclerView bound_recycle;
    private RecordAdapter bound_adapter;
    private RecyclerView hand_recycle;
    private RecordAdapter hand_adapter;
    private RecyclerView gpshan_recycle;
    private RecordAdapter gpshand_adapter;
    private RecyclerView gpsuav_recycle;
    private RecordAdapter gpsuav_adapter;
    private XRefreshView hand_refresh;
    private XRefreshView bound_refresh;
    private XRefreshView gpshand_refresh;
    private XRefreshView gpsuav_refresh;
    private int page = 0;                      //  数据分页 从0开始
    private SelectDialog selectDialog;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.actiivty_start);

        sql = new DbSQL(getBaseContext(), "teamid" + BaseActivity.teamid, null, 1);
        if (sql == null) {
            ToastUtil.show(getString(R.string.toast_spl_un));
            finish();
        }

        selectDialog = new SelectDialog(StartActivity.this);
        selectDialog.setCanceledOnTouchOutside(true);
        selectDialog.setCancelable(true);
        selectDialog.setPopuCloseListener(new OnClick() {
            @Override
            public void onPopuClick(View view, int id) {
                Intent intent = null;
                switch (id) {
                    case R.id.sdialog_tv1:
                        intent = new Intent(StartActivity.this, ParticularsActivity.class);
                        break;
                    case R.id.sdialog_tv2:
                        intent = new Intent(StartActivity.this, GpsHandActivity.class);

                        break;
                    case R.id.sdialog_tv3:
                        intent = new Intent(StartActivity.this, GpsUavActivity.class);

                        break;
                    case R.id.sdialog_tv4:
                        intent = new Intent(StartActivity.this, HandSelect_Activity.class);
                        break;
                    default:
                        break;
                }
                selectDialog.dismiss();
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
            }
        });

        but_start_mapping = findViewById(R.id.but_start_mapping);
        but_start_mapping.setOnClickListener(this);

        start_tv_hand = findViewById(R.id.start_tv_hand);
        start_tv_hand.setOnClickListener(this);
        start_tv_rtkhand = findViewById(R.id.start_tv_rtkhand);
        start_tv_rtkhand.setOnClickListener(this);
        start_tv_gpsuav = findViewById(R.id.start_tv_gpsuav);
        start_tv_gpsuav.setOnClickListener(this);
        start_tv_gpshand = findViewById(R.id.start_tv_gpshand);
        start_tv_gpshand.setOnClickListener(this);
        start_view_rtkhand = findViewById(R.id.start_view_rtkhand);
        start_view_hand = findViewById(R.id.start_view_hand);
        start_view_gpsuav = findViewById(R.id.start_view_gpsuav);
        start_view_gpshand = findViewById(R.id.start_view_gpshand);

        start_viewpager = findViewById(R.id.start_viewpager);
        View view_boundary = LayoutInflater.from(this).inflate(R.layout.view_rc_bound, null);
        View view_gpshan = LayoutInflater.from(this).inflate(R.layout.view_rc_bound, null);
        View view_gpsuav = LayoutInflater.from(this).inflate(R.layout.view_rc_bound, null);
        View view_hand = LayoutInflater.from(this).inflate(R.layout.view_rc_hand, null);
        ArrayList<View> mViewList = new ArrayList<>();//页卡视图集合
        mViewList.add(view_boundary);
        mViewList.add(view_gpshan);
        mViewList.add(view_gpsuav);
        mViewList.add(view_hand);
        start_viewpager.setAdapter(new MPagerAdapter(mViewList));
        start_viewpager.setOnPageChangeListener(new android.support.v4.view.ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        setTextBackground(start_tv_rtkhand, start_view_rtkhand);
                        break;
                    case 1:
                        setTextBackground(start_tv_gpshand, start_view_gpshand);
                        break;
                    case 2:
                        setTextBackground(start_tv_gpsuav, start_view_gpsuav);
                        break;
                    case 3:
                        setTextBackground(start_tv_hand, start_view_hand);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        bound_recycle = view_boundary.findViewById(R.id.bound_recycle);
        bound_recycle.setLayoutManager(new LinearLayoutManager(this));
        bound_recycle.setHasFixedSize(true);
        bound_adapter = new RecordAdapter(this, rtkhand_list);
        bound_recycle.setAdapter(bound_adapter);

        gpshan_recycle = view_gpshan.findViewById(R.id.bound_recycle);
        gpshan_recycle.setLayoutManager(new LinearLayoutManager(this));
        gpshan_recycle.setHasFixedSize(true);
        gpshand_adapter = new RecordAdapter(this, gpshand_list);
        gpshan_recycle.setAdapter(gpshand_adapter);

        gpsuav_recycle = view_gpsuav.findViewById(R.id.bound_recycle);
        gpsuav_recycle.setLayoutManager(new LinearLayoutManager(this));
        gpsuav_recycle.setHasFixedSize(true);
        gpsuav_adapter = new RecordAdapter(this, gpsuav_list);
        gpsuav_recycle.setAdapter(gpsuav_adapter);

        hand_recycle = view_hand.findViewById(R.id.hand_recycle);
        hand_recycle.setLayoutManager(new LinearLayoutManager(this));
        hand_recycle.setHasFixedSize(true);
        hand_adapter = new RecordAdapter(this, hand_list);
        hand_recycle.setAdapter(hand_adapter);

        hand_refresh = view_hand.findViewById(R.id.hand_refresh);
        inRefresh(hand_refresh);
        bound_refresh = view_boundary.findViewById(R.id.bound_refresh);
        inRefresh(bound_refresh);
        gpshand_refresh = view_gpshan.findViewById(R.id.bound_refresh);
        inRefresh(gpshand_refresh);
        gpsuav_refresh = view_gpshan.findViewById(R.id.bound_refresh);
        inRefresh(gpshand_refresh);
    }


    private void setTextBackground(TextView tv, QMUIRoundButton qb) {
        start_tv_hand.setTextColor(getResources().getColor(R.color.text_gray4));
        start_tv_gpshand.setTextColor(getResources().getColor(R.color.text_gray4));
        start_tv_rtkhand.setTextColor(getResources().getColor(R.color.text_gray4));
        start_tv_gpsuav.setTextColor(getResources().getColor(R.color.text_gray4));
        start_view_rtkhand.setVisibility(View.INVISIBLE);
        start_view_hand.setVisibility(View.INVISIBLE);
        start_view_gpsuav.setVisibility(View.INVISIBLE);
        start_view_gpshand.setVisibility(View.INVISIBLE);

        tv.setTextColor(getResources().getColor(R.color.green3));
        qb.setVisibility(View.VISIBLE);
    }

    private void inRefresh(XRefreshView refreshView) {
        refreshView.setSilenceLoadMore();
        //设置刷新完成以后，headerview固定的时间
        refreshView.setPinnedTime(1000);
        refreshView.setMoveForHorizontal(true);
        refreshView.setPullLoadEnable(true);
        refreshView.setAutoLoadMore(false);
//        adapter.setCustomLoadMoreView(new XRefreshViewFooter(this));
        refreshView.enableReleaseToLoadMore(true);
        refreshView.enableRecyclerViewPullUp(true);
        refreshView.enablePullUpWhenLoadCompleted(true);
        //设置静默加载时提前加载的item个数
//        xefreshView1.setPreLoadCount(4);
        refreshView.setXRefreshViewListener(new XRefreshView.SimpleXRefreshListener() {

            @Override
            public void onRefresh(boolean isPullDown) {
                upMappingList();
            }

            @Override
            public void onLoadMore(boolean isSilence) {

                getMappingList(true, String.valueOf(page + 1));
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
        }
        page = 0;
        upMappingList();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void sort() {
        list.clear();
        ArrayList<DbBan> dbBans = sql.queryAll();   // 查询所有的DbBan
        for (DbBan dbBan : dbBans) {
            list.add(new DownMappingBan.DataBean.InfoBean(dbBan));
        }

        hand_list.clear();
        rtkhand_list.clear();
        gpshand_list.clear();
        gpsuav_list.clear();
        for (DownMappingBan.DataBean.InfoBean infoBean : list) {
            String type = infoBean.getType();
            switch (type) {
                case "hand":
                    hand_list.add(infoBean);
                    break;
                case "border":
                    rtkhand_list.add(infoBean);
                    break;
                case "gps_hand":
                    gpshand_list.add(infoBean);
                    break;
                case "gps_uav":
                    gpsuav_list.add(infoBean);
                    break;
                default:
                    break;
            }
        }

        listsort(hand_list);
        listsort(rtkhand_list);
        listsort(gpshand_list);
        listsort(gpsuav_list);

        hand_adapter.notifyDataSetChanged();
        bound_adapter.notifyDataSetChanged();
        gpshand_adapter.notifyDataSetChanged();
        gpsuav_adapter.notifyDataSetChanged();
    }

    private void listsort(ArrayList<DownMappingBan.DataBean.InfoBean> list) {
        try {
            for (int i = 0; i < list.size() - 1; i++) {
                for (int j = i + 1; j < list.size(); j++) {
                    int a1 = Integer.parseInt(list.get(i).getId());
                    int a2 = Integer.parseInt(list.get(j).getId());
                    if (a1 < a2) {//如果队前日期靠前，调换顺序  
                        DownMappingBan.DataBean.InfoBean infoBean = list.get(i);
                        list.set(i, list.get(j));
                        list.set(j, infoBean);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("", e);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.but_start_mapping:
                selectDialog.show();
                break;
            case R.id.start_tv_rtkhand:
                start_viewpager.setCurrentItem(0, true);
                break;
            case R.id.start_tv_gpshand:
                start_viewpager.setCurrentItem(1, true);
                break;
            case R.id.start_tv_gpsuav:
                start_viewpager.setCurrentItem(2, true);
                break;
            case R.id.start_tv_hand:
                start_viewpager.setCurrentItem(3, true);
                break;
            default:
                break;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//当keycode等于退出事件值时
            toQuitTheApp();
            return false;
        }
        return super.onKeyDown(keyCode, event);
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

    private Handler handler = new Handler();

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
//                            Log.e("pp " + result);
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

    private void getMappingList(final boolean is_up, final String page_tag) {
        try {
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
                            Log.e("联网失败 " + call.toString() + " e " + e.toString() + " id " + id);
                            stopRefresh();
                            if (!is_up) sort();
                        }

                        @Override
                        public void onResponse(DownMappingBan ban, int id) {
                            Log.e(ban);
                            List<DownMappingBan.DataBean.InfoBean> info = ban.getData().getInfo();
                            if (ban.getRet() == 200) {
                                if (is_up) {
                                    if (info.size() == 20)
                                        page++;
                                } else {
                                    page = 1;
                                }

                                for (int i = 0; i < info.size(); i++) {
                                    DownMappingBan.DataBean.InfoBean infoBean = info.get(i);
                                    infoBean.setSync(true);
                                    DbBan n_dbBan1 = sql.queryPointById(infoBean.getId());
                                    if (n_dbBan1 == null) {
                                        sql.addBan(new DbBan(infoBean, DateUtil.getCurDate("yyyy-MM-dd HH:mm:ss")));
                                    } else {
                                        if (!n_dbBan1.getUpdate_time().equals(infoBean.getUpdate_time()))
                                            sql.updateBan(new DbBan(infoBean, n_dbBan1.getInsertTime()));
                                    }
                                }
                            }
                            stopRefresh();
                            sort();
                        }
                    });
        } catch (Exception e) {
            Log.e("", e);
        }
    }

    private void stopRefresh() {
        hand_refresh.stopRefresh();
        hand_refresh.stopLoadMore();

        bound_refresh.stopRefresh();
        bound_refresh.stopLoadMore();

        gpshand_refresh.stopRefresh();
        gpshand_refresh.stopLoadMore();

        gpsuav_refresh.stopRefresh();
        gpsuav_refresh.stopLoadMore();

    }
}
