package com.skylin.uav.drawforterrain.setting_channel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.blankj.utilcode.util.ThreadPoolUtils;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.HttpUrlTool;
import com.skylin.uav.drawforterrain.adapter.BarrierAdapter;
import com.skylin.uav.drawforterrain.adapter.BoundaryAdapter;
import com.skylin.uav.drawforterrain.adapter.MPagerAdapter;
import com.skylin.uav.drawforterrain.dialog.Choicedialog;
import com.skylin.uav.drawforterrain.dialog.Basedialog;
import com.skylin.uav.drawforterrain.fragment.OsmFragment;
import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.drawforterrain.service.BluetoothLeService;
import com.skylin.uav.drawforterrain.setting_channel.db.DbBan;
import com.skylin.uav.drawforterrain.setting_channel.db.DbSQL;
import com.skylin.uav.drawforterrain.util.Area;
import com.skylin.uav.drawforterrain.util.DateUtil;
import com.skylin.uav.drawforterrain.util.LineUtil;
import com.skylin.uav.drawforterrain.util.OBSMatch;
import com.skylin.uav.drawforterrain.util.OrderUtils;
import com.skylin.uav.drawforterrain.util.ThreadPoolUtil;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.skylin.uav.drawforterrain.views.MarqueeTextView;
import com.skylin.uav.drawforterrain.views.ViewPager;
import com.skylin.uav.R;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import sjj.alog.Log;
import task.model.Pair;


/**
 * Created by Moon on 2018/2/26.
 */

public class DotActivity extends BaseActivity implements View.OnClickListener {
    public static DotActivity dotActivity = null;

    //    private MapFragment mapFragment;
    private OsmFragment mapFragment;

    private ViewPager viewPager;
    private TextView tv_boundary, tv_obstacle;    // table  边界点  障碍点
    private View group_boundary, group_obstacle;
    private LinearLayout ll_boundary, ll_obstacle;
    private final int MODE_BOUNDARY = 0;   //状态  边界点
    private final int MODE_OBSTACLE = 1;    //状态  障碍点
    private int MODE = MODE_BOUNDARY;             //状态默认为边界点


    private LinearLayout fragment_commit, fragment_record, fragment_detail;
    private QMUIRoundButton commit_out;
    private QMUIRoundButton commit_save;
    private EditText commit_name;

    private DownMappingBan.DataBean.InfoBean infoBean;

    private QMUIRoundButton but_backout;    //撤销按钮
    private ArrayList<Pair<String, Point>> backout_list = new ArrayList();

    private QMUIRoundButton but_commit;    //完成按钮
    private TextView dot_tv_area;          //面积
    private TextView dot_tv_area_unit;
    private LinearLayout ll_map_dot;       //定位  地图模式  按钮父布局
    private LinearLayout ll_area;          //面积父布局

    private DrawerLayout drawerlayout;
    private LinearLayout nav_home, nav_connect, nav_his, nav_seting;
    private QMUIRoundButton abort;
    private TextView versionNmae;
    private ImageView top_show;
    private LinearLayout top_icon;
    private LinearLayout ll_top_show;
    private LinearLayout ll_top;
    private MarqueeTextView top_name;
    private LinearLayout top_channle;
    private ImageView top_channle_im;
    private TextView top_channle_tv;

    private ImageView top_rtk_im;
    private ImageView top_rtk_im_icon;
    private ImageView top_elec_im;
    private TextView top_elec_tv;
    private MarqueeTextView head_tv_name;
    private TextView head_tv_time;
    private MarqueeTextView head_tv_tid;

    private Button warming_popu_massage;
    private QMUIRoundButton details_options; // 更多按钮
    private QMUIRoundButton details_back;

    private Choicedialog choicedialog;            //更多的dialog
    private Basedialog makeSureDeletePopu;      //确定删除的popu
    private Basedialog dotgiveUpPopu;                //放弃本次测绘的popu
    private Basedialog continuePopu;                  //已经有任务  是否新开的popu

    private GGABan ggaBan;
    private GGABan old_ggaBan = new GGABan();

    private Button record_point;
    private ArrayList<Point> boundary_List = new ArrayList<>();
    private BoundaryAdapter boundaryAdapter;
    private RecyclerView recycle_view_boundary;
    private TextView null_view_boundary;

    private RecyclerView recycle_view_obstacie;
    private TextView null_view_obstacie;

    private RecyclerView obs_recycler;
    private CommonAdapter<ObsMasterBan> obsAdapter;
    private ArrayList<ObsMasterBan> obs_list = new ArrayList<>();
    private String last_obs = "-1";                 //上一次点击的障碍点
    private int last_bounary = -1;                 //上一次点击的边界点
    private int insert_position = -1;           //  插入障碍点  内 list  的 position
    private int insert_index = -1;             //   插入障碍点  外 list  的 position

    private boolean is_detial = false;
    private boolean is_recover = false;
    private ExecutorService threadPool;
    private Future<?> submit;
    private DbSQL sql;
    private QMUITipDialog tipDialog;
    private Thread firstThread;      //第一次进入时画地图

    public static void startDotActivity(Activity activity, boolean recover, boolean is_detiak, DownMappingBan.DataBean.InfoBean infoBean) {
        Intent intent = new Intent(activity, DotActivity.class);
        intent.putExtra(BlueToochActivity.RECOVER, recover);   //控制是否展示之前数据
        intent.putExtra(BlueToochActivity.DETIAL, is_detiak);   // 是否是展示细节
        intent.putExtra(BlueToochActivity.INFOBEAN, infoBean);  //  细节展示需要的数据
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dot);
        sql = new DbSQL(getBaseContext(), "teamid" + BaseActivity.teamid, null, 1);
        if (sql == null) {
            ToastUtil.show(getString(R.string.toast_spl_un));
            finish();
        }

        dotActivity = this;
        registerReceiver(mGattUpdateReceiver, BlueToochActivity.makeGattUpdateIntentFilter());
        dot_tv_area = findViewById(R.id.dot_tv_area);
        dot_tv_area_unit = findViewById(R.id.dot_tv_area_unit);

        threadPool = Executors.newSingleThreadExecutor();

        Intent intent = getIntent();
        is_recover = intent.getBooleanExtra(BlueToochActivity.RECOVER, false);
        is_detial = intent.getBooleanExtra(BlueToochActivity.DETIAL, false);
        infoBean = (DownMappingBan.DataBean.InfoBean) intent.getSerializableExtra(BlueToochActivity.INFOBEAN);


        initTables();
        initMap();
        initSideslip();
        initViews();
        initDetails();
        initTop();

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.DotActivity_tv14))
                .create();

        record_point = findViewById(R.id.record_point);
        record_point.setOnClickListener(this);

        ll_map_dot = findViewById(R.id.ll_map_dot);
        ll_area = findViewById(R.id.ll_area);

        if (is_recover) {

            commit_name.setText(BlueToochActivity.title);
            if (!TextUtils.isEmpty(commit_name.getText().toString())) {
                commit_name.setFocusable(false);
                commit_name.setFocusableInTouchMode(false);
            }

            if (BlueToochActivity.boundaryList != null) {
                for (Point point : BlueToochActivity.boundaryList) {
                    boundary_List.add(point);
                }
            }

            if (BlueToochActivity.obsList != null) {
                for (int i = 0; i < BlueToochActivity.obsList.size(); i++) {
                    ArrayList<Point> list = new ArrayList<>();
                    for (int j = 0; j < BlueToochActivity.obsList.get(i).getList().size(); j++) {
                        list.add(BlueToochActivity.obsList.get(i).getList().get(j));
                    }
                    obs_list.add(new ObsMasterBan(list, i, getBaseContext()));
                }
                obsAdapter.notifyDataSetChanged();
            }

            if (BlueToochActivity.backout_list != null) {
                for (Pair<String, Point> pair : BlueToochActivity.backout_list) {
                    backout_list.add(pair);
                }
            }

            if (boundary_List.size() == 0) {
                null_view_boundary.setVisibility(View.VISIBLE);
                recycle_view_boundary.setVisibility(View.GONE);
            } else {
                null_view_boundary.setVisibility(View.GONE);
                recycle_view_boundary.setVisibility(View.VISIBLE);
            }


            if (obs_list.size() == 1 && obs_list.get(0).getList().size() == 0) {
                null_view_obstacie.setVisibility(View.VISIBLE);
                recycle_view_obstacie.setVisibility(View.GONE);
            } else if (obs_list.size() == 0) {
                null_view_obstacie.setVisibility(View.VISIBLE);
                recycle_view_obstacie.setVisibility(View.GONE);
            } else {
                null_view_obstacie.setVisibility(View.GONE);
                recycle_view_obstacie.setVisibility(View.VISIBLE);
            }

        } else if (is_detial) {

            fragment_record.setVisibility(View.GONE);
            fragment_commit.setVisibility(View.GONE);
            fragment_detail.setVisibility(View.VISIBLE);

            abort.setVisibility(View.INVISIBLE);

            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.RIGHT);
            layoutParams.setMargins(0, 0, 18, 600);
            ll_map_dot.setLayoutParams(layoutParams);
            ll_area.setVisibility(View.GONE);
            but_commit.setVisibility(View.GONE);

            DecimalFormat df = new DecimalFormat("#0.00");
            String format = df.format(Double.valueOf(infoBean.getArea()));
            TextView area_detail = findViewById(R.id.area_detail);
            area_detail.setText(format);
            FrameLayout sync_detail = findViewById(R.id.sync_detail);
            if (infoBean.getSync())
                sync_detail.setVisibility(View.INVISIBLE);
            else
                sync_detail.setVisibility(View.VISIBLE);
            ArrayList<String> list = new ArrayList<>();
            list.add(infoBean.getTitle());
            list.add(infoBean.getProvince() + " " + infoBean.getCity() + " " + infoBean.getDistrict());
            list.add(infoBean.getCreate_time());
            list.add(infoBean.getCreate_name());
            list.add(infoBean.getUpdate_name() + " " + infoBean.getUpdate_time());

            CommonAdapter details_adapter = new CommonAdapter<String>(this, R.layout.item_details, list) {
                @Override
                protected void convert(ViewHolder holder, String ss, int position) {
                    MarqueeTextView content = holder.getView(R.id.details_content);
                    LinearLayout linearLayout = holder.getView(R.id.details_layout);
                    switch (position) {
                        default:
                            break;
                        case 0:
//                        title.setText("测绘名称");
                            content.setText(ss);
                            content.setTextColor(getResources().getColor(R.color.text_gray3));
                            content.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                            break;
                        case 1:
//                        title.setText("地区");
                            content.setText(ss);
                            break;
                        case 2:
//                        title.setText("测绘时间");
                            content.setText(ss);
                            Drawable drawable = getResources().getDrawable(R.mipmap.icon_time);
                            drawable.setBounds(0, 0, 30, 30);
                            content.setCompoundDrawables(drawable, null, null, null);
                            break;
                        case 3:
//                        title.setText("测绘人");
                            content.setText(ss);
                            Drawable drawable1 = getResources().getDrawable(R.mipmap.icon_per);
                            drawable1.setBounds(0, 0, 30, 30);
                            content.setCompoundDrawables(drawable1, null, null, null);
                            break;
                        case 4:
                            if (TextUtils.isEmpty(ss) | ss.equals(" ")) {
                                linearLayout.setVisibility(View.GONE);
                            } else {
//                            title.setText("调整");
                                content.setText(ss + getString(R.string.DotActivity_tv15));
                                Drawable drawable2 = getResources().getDrawable(R.mipmap.icon_per);
                                drawable2.setBounds(0, 0, 30, 30);
                                content.setCompoundDrawables(drawable2, null, null, null);
                            }
                            break;
                    }
                }
            };

            RecyclerView details_recycle = findViewById(R.id.details_recycle);
            details_recycle.setLayoutManager(new LinearLayoutManager(this));
            details_recycle.setAdapter(details_adapter);
            details_recycle.setHasFixedSize(true);

            for (List<Double> list1 : infoBean.getMappingBorder()) {
                boundary_List.add(new Point(list1.get(1), list1.get(0)));
            }

            for (int i = 0; i < infoBean.getObstacleBorder().size(); i++) {
                ArrayList<Point> arrayList = new ArrayList<>();

                for (List<Double> lists : infoBean.getObstacleBorder().get(i)) {
                    arrayList.add(new Point(lists.get(1), lists.get(0)));
                }
                obs_list.add(new ObsMasterBan(arrayList, i, getBaseContext()));
            }
        }

    }

    private void initTop() {
        but_commit = findViewById(R.id.but_commit);
        but_commit.setOnClickListener(this);

        but_backout = findViewById(R.id.but_backout);
        but_backout.setOnClickListener(this);

        top_icon = findViewById(R.id.top_icon);

        top_name = (MarqueeTextView) findViewById(R.id.top_name);

        top_show = findViewById(R.id.top_show);
        ll_top_show = findViewById(R.id.ll_top_show);
        ll_top_show.setOnClickListener(this);

        ll_top = findViewById(R.id.ll_top);

        top_channle = findViewById(R.id.top_channle);
        top_channle.setOnClickListener(this);
        top_channle_im = findViewById(R.id.top_channle_im);
        top_channle_tv = findViewById(R.id.top_channle_tv);
        top_rtk_im = findViewById(R.id.top_rtk_im);
        top_rtk_im_icon = findViewById(R.id.top_rtk_im_icon);
        top_elec_im = findViewById(R.id.top_elec_im);
        top_elec_tv = findViewById(R.id.top_elec_tv);

        setTopWhite();

    }

    private void setTopWhite() {
        QMUIStatusBarHelper.setStatusBarDarkMode(this);  //状态栏白色

        top_name.setTextColor(getResources().getColor(R.color.white));
        top_channle_tv.setTextColor(getResources().getColor(R.color.white));
        top_elec_tv.setTextColor(getResources().getColor(R.color.white));
        dot_tv_area.setTextColor(getResources().getColor(R.color.white));
        dot_tv_area_unit.setTextColor(getResources().getColor(R.color.white));

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
        dot_tv_area.setTextColor(getResources().getColor(R.color.darkblue1));
        dot_tv_area_unit.setTextColor(getResources().getColor(R.color.darkblue1));

        top_show.setBackgroundResource(R.mipmap.icon_actionbar_black);
        top_channle_im.setBackgroundResource(R.mipmap.icon_path_black);
        top_rtk_im.setBackgroundResource(R.mipmap.icon_rtk_black);
        top_elec_im.setBackgroundResource(R.mipmap.icon_elec_black);
        ll_top.setBackgroundResource(R.color.tm);

    }

    private void initDetails() {
        fragment_detail = findViewById(R.id.fragment_detail);
        details_back = findViewById(R.id.details_back);
        details_back.setOnClickListener(this);
        details_options = findViewById(R.id.details_options);
        details_options.setOnClickListener(this);

        warming_popu_massage = findViewById(R.id.warming_popu_massage);
        warming_popu_massage.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                OrderUtils.requestSystems();
            }
        });

        choicedialog = new Choicedialog(this, R.style.Dialog);
        choicedialog.setCanceledOnTouchOutside(true);
        choicedialog.setCancelable(true); //设置点击对话框外部区域，关闭对话框，默认
        choicedialog.setPopuCloseListener(new OnClick() {
            @Override
            public void onPopuClick(View view, int id) {
                switch (id) {
                    case R.id.btn_open_camera:
                        choicedialog.dismiss();
                        if (!BlueToochActivity.mConnected) {
                            ToastUtil.show(getString(R.string.toast_3));
                            return;
                        }
                        if (!BaseActivity.workmoder.equals("手持杖")) {
                            ToastUtil.show(getString(R.string.toast_4));
                            return;
                        }
                        if (BlueToochActivity.boundaryList != null | BlueToochActivity.obsList != null | BlueToochActivity.backout_list != null) {
                            continuePopu.show();
                        } else {
                            BlueToochActivity.boundaryList = boundary_List;
                            BlueToochActivity.obsList = obs_list;
                            BlueToochActivity.backout_list = backout_list;
                            BlueToochActivity.title = infoBean.getTitle();
                            BlueToochActivity.Id = TextUtils.isEmpty(infoBean.getId()) ? null : infoBean.getId();
                            BlueToochActivity.insertTime = TextUtils.isEmpty(infoBean.getInsertTime()) ? null : infoBean.getInsertTime();
                            DotActivity.startDotActivity(DotActivity.this, true, false, null);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                            onFinish();
                        }
                        break;
                    case R.id.btn_choose_img:
                        choicedialog.dismiss();
                        makeSureDeletePopu.show();
                        break;
                    case R.id.btn_cancel:
                        choicedialog.dismiss();
                        break;
                    default:
                        break;

                }
            }
        });

        makeSureDeletePopu = new Basedialog(this
                , R.style.MDialog
                , getString(R.string.continuePopu_tv1)
                , getString(R.string.continuePopu_tv7)
                , getString(R.string.continuePopu_tv8)
                , getString(R.string.continuePopu_tv9));
        makeSureDeletePopu.setCanceledOnTouchOutside(true);
        makeSureDeletePopu.setCancelable(true);
        makeSureDeletePopu.setPopuCloseListener(new OnClick() {
            @Override
            public void onPopuClick(View view, int id) {
                switch (id) {
                    case R.id.tv_single_operation_dialog_confirm:
                        makeSureDeletePopu.dismiss();
                        break;
                    case R.id.tv_single_cancel:
                        makeSureDeletePopu.dismiss();
                        delete();
                        break;
                    default:
                        break;
                }
            }
        });

        dotgiveUpPopu = new Basedialog(this
                , R.style.MDialog
                , getString(R.string.continuePopu_tv10)
                , getString(R.string.continuePopu_tv11)
                , getString(R.string.continuePopu_tv12)
                , getString(R.string.continuePopu_tv9));
        dotgiveUpPopu.setCanceledOnTouchOutside(true);
        dotgiveUpPopu.setCancelable(true);
        dotgiveUpPopu.setPopuCloseListener(new OnClick() {
            @Override
            public void onPopuClick(View view, int id) {
                switch (id) {
                    case R.id.tv_single_operation_dialog_confirm:
                        dotgiveUpPopu.dismiss();
                        break;
                    case R.id.tv_single_cancel:
                        dotgiveUpPopu.dismiss();
                        BlueToochActivity.boundaryList = null;
                        BlueToochActivity.obsList = null;
                        BlueToochActivity.backout_list = null;
                        BlueToochActivity.Id = null;
                        BlueToochActivity.insertTime = null;
                        onFinish();
                        break;
                    default:
                        break;
                }
            }
        });

        continuePopu = new Basedialog(this
                , R.style.MDialog
                , getString(R.string.continuePopu_tv1)
                , getString(R.string.continuePopu_tv13)
                , getString(R.string.continuePopu_tv14)
                , getString(R.string.continuePopu_tv15));
        continuePopu.setCanceledOnTouchOutside(true);
        continuePopu.setCancelable(true);
        continuePopu.setPopuCloseListener(new OnClick() {
            @Override
            public void onPopuClick(View view, int id) {
                if (!BlueToochActivity.mConnected) {
                    ToastUtil.show(getString(R.string.toast_3));
                    return;
                }
                if (!BaseActivity.workmoder.equals("手持杖")) {
                    ToastUtil.show(getString(R.string.toast_4));
                    return;
                }
                switch (id) {
                    case R.id.tv_single_operation_dialog_confirm:
                        continuePopu.dismiss();
                        DotActivity.startDotActivity(DotActivity.this, true, false, null);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                        onFinish();
                        break;
                    case R.id.tv_single_cancel:
                        continuePopu.dismiss();
                        BlueToochActivity.boundaryList = boundary_List;
                        BlueToochActivity.obsList = obs_list;
                        BlueToochActivity.backout_list = backout_list;
                        BlueToochActivity.title = infoBean.getTitle();
                        BlueToochActivity.Id = TextUtils.isEmpty(infoBean.getId()) ? null : infoBean.getId();
                        BlueToochActivity.insertTime = TextUtils.isEmpty(infoBean.getInsertTime()) ? null : infoBean.getInsertTime();
                        DotActivity.startDotActivity(DotActivity.this, true, false, null);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                        onFinish();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private void initViews() {
        fragment_commit = findViewById(R.id.fragment_commit);
        fragment_commit.setVisibility(View.GONE);
        fragment_record = findViewById(R.id.fragment_record);
        fragment_record.setVisibility(View.VISIBLE);
        commit_out = findViewById(R.id.commit_out);
        commit_out.setOnClickListener(this);
        commit_save = findViewById(R.id.commit_save);
        commit_save.setOnClickListener(this);

        commit_name = findViewById(R.id.commit_name);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mapFragment.setMapCanClick(false);

        firstThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    drawMap(-1, -1, -1);
//                    mapFragment.DrawNoFly();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        firstThread.start();
    }

    private void drawMap(final int b_index, final int obs_index, final int obs_position) {
        if (submit != null) {
            submit.cancel(true);
        }
        submit = threadPool.submit(new Runnable() {
            @Override
            public void run() {
                mapFragment.DrawMapping(
                        boundary_List, b_index,
                        obs_list, obs_index, obs_position);
            }
        });
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void onResume() {
        super.onResume();
//        Log.e("onResume");
        if (boundary_List.size() > 0) {
            if (boundary_List.size() > 2) {
                Point centerPoint = Point.getCenterPoint(boundary_List);
                mapFragment.setLoaction(centerPoint.getLatitude(), centerPoint.getLongitude(), 0);
                if (is_detial)
                    mapFragment.setZoom(boundary_List, centerPoint);
            } else {
                mapFragment.setLoaction(boundary_List.get(0).getLatitude(), boundary_List.get(0).getLongitude(), 0);
            }
        } else if (obs_list.size() > 0) {
            if (obs_list.get(0).getList().size() > 0) {
                mapFragment.setLoaction(obs_list.get(0).getList().get(0).getLatitude(), obs_list.get(0).getList().get(0).getLongitude(), 0);
            } else {
                handler.sendEmptyMessageDelayed(1, 3000);
            }
        } else {
            handler.sendEmptyMessageDelayed(1, 3000);
        }

        if (BlueToochActivity.mConnected) {
            handler.removeCallbacks(request_run);
            handler.postDelayed(request_run, 500);
        }

        boundaryAdapter.notifyDataSetChanged();
        obsAdapter.notifyDataSetChanged();
        for (ObsMasterBan ban : obs_list) {
            ban.getAdapter().notifyDataSetChanged();
        }
        matchArea(false);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Log.e("onDestroy");
        unregisterReceiver(mGattUpdateReceiver);
        handler.removeCallbacks(request_run);
        ThreadPoolUtil.destroyThread(firstThread);
    }

    private void initSideslip() {
        versionNmae = findViewById(R.id.versionNmae_particulars);
        try {
            PackageManager manager = APP.getContext().getPackageManager();
            PackageInfo info = manager.getPackageInfo(APP.getContext().getPackageName(), 0);
            versionNmae.setText(getString(R.string.app_name) + " " + info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        drawerlayout = findViewById(R.id.drawerlayout_dot);
        nav_home = findViewById(R.id.nav_home);
        nav_home.setOnClickListener(this);
        nav_connect = findViewById(R.id.nav_connect);
        nav_connect.setOnClickListener(this);
        nav_his = findViewById(R.id.nav_his);
        nav_his.setOnClickListener(this);
        nav_seting = findViewById(R.id.nav_seting);
        nav_seting.setOnClickListener(this);
        abort = findViewById(R.id.abort);
        abort.setOnClickListener(this);
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//当keycode等于退出事件值时
            if (fragment_commit.getVisibility() == View.VISIBLE) {
                fragment_commit.setVisibility(View.GONE);
                fragment_record.setVisibility(View.VISIBLE);
                but_commit.setVisibility(View.VISIBLE);
                return false;
            }
            if (drawerlayout.isDrawerOpen(Gravity.LEFT)) {
                drawerlayout.closeDrawer(Gravity.LEFT);
                return false;
            }
            if (!record_point.getText().toString().equals(getString(R.string.DotActivity_tv6))) {
                for (ObsMasterBan ban : obs_list) {
                    ban.getAdapter().setIndext(-1);
                    ban.getAdapter().setDelete(false);
                    ban.getAdapter().notifyDataSetChanged();
                }
                boundaryAdapter.setDeleat(false);
                boundaryAdapter.setIndext(-1);
                boundaryAdapter.notifyDataSetChanged();
                drawMap(-1, -1, -1);
                record_point.setText(getString(R.string.DotActivity_tv6));
                backout_state(false);
                return false;
            }
            if (!is_detial) {
                listSize();
            }
            onFinish();
            return super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void onFinish() {
        finish();
        overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
    }

    private void initMap() {
        mapFragment = new OsmFragment();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.dot_map_replace, mapFragment);
        ft.commit();

        findViewById(R.id.location_dot).setOnClickListener(this);
        findViewById(R.id.map_model).setOnClickListener(this);

        if (!is_detial) mapFragment.setMarkerClickListener(new OsmFragment.MarkerClickListener() {
            @Override
            public void onMarkerClickListener(Marker marker, MapView mapView) {
                String id = marker.getId();
                String[] split = id.split(",");
                if (id.startsWith("boundary")) {
                    int b = Integer.parseInt(split[1]);

                    if (b == last_bounary) {
                        record_point.setText(getString(R.string.DotActivity_tv6));
                        backout_state(false);
                        boundaryAdapter.setDeleat(false);
                        boundaryAdapter.setIndext(-1);
                        last_bounary = -1;
                        drawMap(-1, -1, -1);
                    } else {
                        last_bounary = b;
                        record_point.setText(getString(R.string.DotActivity_tv16));
                        boundaryAdapter.setDeleat(false);
                        boundaryAdapter.setIndext(b);
                        drawMap(b, -1, -1);
                        recycle_view_boundary.smoothScrollToPosition(b);
                        backout_state(true);
                    }
                    boundaryAdapter.notifyDataSetChanged();

                } else if (id.startsWith("obs")) {
                    String tag = split[1];
                    String position = split[2];

                    if (last_obs.equals(tag + "" + position)) {
                        for (ObsMasterBan ban : obs_list) {
                            ban.getAdapter().setIndext(-1);
                            ban.getAdapter().notifyDataSetChanged();
                        }
                        last_obs = -1 + "";
                        record_point.setText(getString(R.string.DotActivity_tv6));
                        backout_state(false);
                        drawMap(-1, -1, -1);
                    } else {
                        last_obs = tag + "" + position;
                        insert_position = Integer.parseInt(position);
                        insert_index = Integer.parseInt(tag);

                        for (int i = 0; i < obs_list.size(); i++) {
                            if (i == insert_index) {
                                obs_list.get(i).getAdapter().setIndext(insert_position);
                                obs_list.get(i).getAdapter().notifyDataSetChanged();
                            } else {
                                obs_list.get(i).getAdapter().setIndext(-1);
                                obs_list.get(i).getAdapter().notifyDataSetChanged();
                                Log.e(obs_list.get(i).getRecyclerView() + "  " + i + "  " + insert_position);
                            }
                        }
                        obs_recycler.smoothScrollToPosition(insert_index);
                        record_point.setText(getString(R.string.DotActivity_tv16));
                        drawMap(-1, insert_index, insert_position);
                        backout_state(true);
                    }
                }
            }
        });
    }

    private void initTables() {
        viewPager = findViewById(R.id.viewpager_dot);
        viewPager.setLayoutParams(new LinearLayout.LayoutParams(-1, 200, 1));

        group_boundary = findViewById(R.id.group_boundary);
        group_obstacle = findViewById(R.id.group_obstacle);
        tv_boundary = findViewById(R.id.tv_boundary);
        tv_obstacle = findViewById(R.id.tv_obstacle);
        ll_boundary = findViewById(R.id.ll_boundary);
        ll_boundary.setOnClickListener(this);
        ll_obstacle = findViewById(R.id.ll_obstacle);
        ll_obstacle.setOnClickListener(this);

        View view_boundary = LayoutInflater.from(this).inflate(R.layout.view_boundary, null);
        View view_obstacle = LayoutInflater.from(this).inflate(R.layout.view_obstacle, null);
        ArrayList<View> mViewList = new ArrayList<>();//页卡视图集合
        mViewList.add(view_boundary);
        mViewList.add(view_obstacle);
        viewPager.setAdapter(new MPagerAdapter(mViewList));
        viewPager.setOnPageChangeListener(new android.support.v4.view.ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        MODE = MODE_BOUNDARY;
                        viewPager.setLayoutParams(new LinearLayout.LayoutParams(-1, 200, 1));
                        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.RIGHT);
                        layoutParams.setMargins(0, 0, 18, 440);
                        ll_map_dot.setLayoutParams(layoutParams);
                        FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.LEFT);
                        layoutParams1.setMargins(18, 0, 0, 440);
                        ll_area.setLayoutParams(layoutParams1);
                        record_point.setText(getString(R.string.DotActivity_tv6));
                        backout_state(false);
                        tv_boundary.setTextColor(getResources().getColor(R.color.green3));
                        group_boundary.setBackgroundResource(R.color.green3);
                        tv_obstacle.setTextColor(getResources().getColor(R.color.text_gray));
                        group_obstacle.setBackgroundResource(R.color.white);
                        for (ObsMasterBan ban : obs_list) {
                            ban.getAdapter().setIndext(-1);
                            ban.getAdapter().setDelete(false);
                            ban.getAdapter().notifyDataSetChanged();
                        }
                        drawMap(-1, -1, -1);
                        break;
                    case 1:
                        MODE = MODE_OBSTACLE;
                        viewPager.setLayoutParams(new LinearLayout.LayoutParams(-1, 270, 1));
                        FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.RIGHT);
                        layoutParams2.setMargins(0, 0, 18, 510);
                        ll_map_dot.setLayoutParams(layoutParams2);
                        FrameLayout.LayoutParams layoutParams3 = new FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM | Gravity.LEFT);
                        layoutParams3.setMargins(18, 0, 0, 510);
                        ll_area.setLayoutParams(layoutParams3);
                        record_point.setText(getString(R.string.DotActivity_tv6));
                        backout_state(false);
                        tv_boundary.setTextColor(getResources().getColor(R.color.text_gray));
                        group_boundary.setBackgroundResource(R.color.white);
                        tv_obstacle.setTextColor(getResources().getColor(R.color.green3));
                        group_obstacle.setBackgroundResource(R.color.green3);
                        boundaryAdapter.setDeleat(false);
                        boundaryAdapter.setIndext(-1);
                        boundaryAdapter.notifyDataSetChanged();
                        drawMap(-1, -1, -1);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        initBoundaryViews(view_boundary);

        initObsViews(view_obstacle);
    }

    private void initBoundaryViews(View view_boundary) {
        boundaryAdapter = new BoundaryAdapter(boundary_List, this);
        null_view_boundary = view_boundary.findViewById(R.id.null_view_boundary);
        recycle_view_boundary = view_boundary.findViewById(R.id.recycle_view_boundary);
        recycle_view_boundary.setLayoutManager(new LinearLayoutManager(getBaseContext(), LinearLayoutManager.HORIZONTAL, false));
        recycle_view_boundary.setAdapter(boundaryAdapter);
        recycle_view_boundary.setHasFixedSize(true);
        boundaryAdapter.setItemClickListener(new BoundaryAdapter.ItemClickListener() {

            @Override
            public void onItemClicked(View view, int position) {
                switch (view.getId()) {
                    case R.id.boundary_item_delete:
                        backout_list.add(new Pair("bounary," + position, boundary_List.get(position)));
                        boundary_List.remove(position);
                        drawMap(-1, -1, -1);
                        boundaryAdapter.notifyDataSetChanged();
                        matchArea(false);
                        backout_state(true);
                        if (boundary_List.size() == 0) {
                            null_view_boundary.setVisibility(View.VISIBLE);
                            recycle_view_boundary.setVisibility(View.GONE);
                        }
                        break;
                    case R.id.boundary_item_number:
                        mapFragment.setLoaction(
                                boundary_List.get(position).getLatitude()
                                , boundary_List.get(position).getLongitude()
                                , 0);
                        if (!boundaryAdapter.isDeleat()) {
                            if (last_bounary == position) {
                                record_point.setText(getString(R.string.DotActivity_tv6));
                                backout_state(false);
                                boundaryAdapter.setDeleat(false);
                                boundaryAdapter.setIndext(-1);
                                last_bounary = -1;
                                drawMap(-1, -1, -1);
                            } else {
                                last_bounary = position;
                                record_point.setText(getString(R.string.DotActivity_tv16));
                                backout_state(true);
                                boundaryAdapter.setDeleat(false);
                                boundaryAdapter.setIndext(position);
                                drawMap(position, -1, -1);
                            }
                        } else {
                            boundaryAdapter.setDelete(position);
                            drawMap(position, -1, -1);
                        }
                        boundaryAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        });
        boundaryAdapter.setLongClickListener(new BoundaryAdapter.LongClickListener() {

            @Override
            public void onLongClicked(View view, int position) {
                mapFragment.setLoaction(
                        boundary_List.get(position).getLatitude()
                        , boundary_List.get(position).getLongitude()
                        , 0);
                if (last_bounary == position) {
                    boundaryAdapter.setDeleat(false);
                    last_bounary = -1;
                    drawMap(-1, -1, -1);
                    record_point.setText(getString(R.string.DotActivity_tv6));
                    backout_state(false);
                } else {
                    last_bounary = position;
                    boundaryAdapter.setDeleat(true);
                    boundaryAdapter.setDelete(position);
                    drawMap(position, -1, -1);
                    record_point.setText(getString(R.string.DotActivity_tv17));
                    backout_state(true);
                }
                boundaryAdapter.notifyDataSetChanged();
            }
        });
    }

    private void initObsViews(View view_obstacle) {

        null_view_obstacie = view_obstacle.findViewById(R.id.null_view_obstacie);
        recycle_view_obstacie = view_obstacle.findViewById(R.id.recycle_view_obstacie);
        recycle_view_obstacie.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        recycle_view_obstacie.setHasFixedSize(true);

        obsAdapter = new CommonAdapter<ObsMasterBan>(this, R.layout.item_obs_master, obs_list) {
            @Override
            protected void convert(ViewHolder holder, final ObsMasterBan obsMasterBan, final int position) {
                final String s = new String(new byte[]{(byte) (position + 65)}, 0, 1);
                TextView obs_hande = holder.getView(R.id.obs_hande);
                obs_hande.setText(s);
                TextView end_view = holder.getView(R.id.obs_end);
                end_view.setVisibility(
                        ((obsAdapter.getDatas().size() - 1) == position) && (obsMasterBan.getList().size() > 2)
                                ? View.VISIBLE
                                : View.INVISIBLE);
                end_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (ObsMasterBan ban : obs_list) {
                            ban.getAdapter().setDelete(false);
                            ban.getAdapter().setIndext(-1);
                            ban.getAdapter().notifyDataSetChanged();
                        }
                        record_point.setText(getString(R.string.DotActivity_tv6));
                        backout_state(false);
                        drawMap(-1, -1, -1);
                        if (obs_list.size() < 11) {
                            obs_list.add(new ObsMasterBan(new ArrayList<Point>(), obs_list.size(), getBaseContext()));
                            obsAdapter.notifyDataSetChanged();
                            recycle_view_obstacie.smoothScrollToPosition(obs_list.size() - 1);
                        } else {
                            ToastUtil.show(getString(R.string.toast_8));
                        }
                    }
                });

                obs_recycler = holder.getView(R.id.obs_recycyle);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext(), LinearLayoutManager.HORIZONTAL, false);
//                linearLayoutManager.setReverseLayout(true);
                linearLayoutManager.setStackFromEnd(true);
                obs_recycler.setLayoutManager(linearLayoutManager);
                obs_recycler.setHasFixedSize(true);
                BarrierAdapter adapter = obs_list.get(position).getAdapter();
                obs_recycler.setAdapter(adapter);
                obs_list.get(position).setRecyclerView(obs_recycler);

                adapter.setItemClickListener(new BarrierAdapter.ItemClickListener() {
                    @Override
                    public void onItemClicked(View view, int position) {
                        switch (view.getId()) {
                            case R.id.barrier_item_delete:
                                int deleat_barrier = (int) view.getTag();
//                        LogUtils.e(deleat_barrier + "  " + position);
                                Point point = obs_list.get(deleat_barrier).getList().get(position);
                                obs_list.get(deleat_barrier).getList().remove(position);
                                obs_list.get(deleat_barrier).getAdapter().notifyDataSetChanged();
                                if (obs_list.get(deleat_barrier).getList().size() == 0 && deleat_barrier != 0) {
                                    obs_list.remove(deleat_barrier);
                                    for (int i = 0; i < obs_list.size(); i++) {
                                        obs_list.get(i).getAdapter().setTag(i);
                                    }
                                    notifyDataSetChanged();
                                    backout_list.add(new Pair("obs,yes," + deleat_barrier + "," + position, point));
                                } else {
                                    backout_list.add(new Pair("obs,no," + deleat_barrier + "," + position, point));
                                }
                                drawMap(-1, -1, -1);
                                backout_state(true);
                                if (obs_list.size() == 1 && obs_list.get(0).getList().size() == 0) {
                                    null_view_obstacie.setVisibility(View.VISIBLE);
                                    recycle_view_obstacie.setVisibility(View.GONE);
                                }
                                notifyDataSetChanged();
                                break;
                            case R.id.barrier_item_number:
                                int tag = (int) view.getTag();
                                mapFragment.setLoaction(
                                        obs_list.get(tag).getList().get(position).getLatitude()
                                        , obs_list.get(tag).getList().get(position).getLongitude()
                                        , 0);
                                if (!obsMasterBan.getAdapter().getDelete()) {
                                    if (last_obs.equals(tag + "" + position)) {
                                        for (ObsMasterBan ban : obs_list) {
                                            ban.getAdapter().setIndext(-1);
                                            ban.getAdapter().notifyDataSetChanged();
                                        }
                                        last_obs = -1 + "";
                                        record_point.setText(getString(R.string.DotActivity_tv6));
                                        backout_state(false);
                                        drawMap(-1, -1, -1);
                                    } else {
                                        last_obs = tag + "" + position;
                                        for (ObsMasterBan ban : obs_list) {
                                            ban.getAdapter().setIndext(-1);
                                            ban.getAdapter().notifyDataSetChanged();
                                        }
                                        insert_position = position;
                                        insert_index = tag;

                                        record_point.setText(getString(R.string.DotActivity_tv16));
                                        backout_state(true);
                                        obsMasterBan.getAdapter().setIndext(position);
                                        obsMasterBan.getAdapter().notifyDataSetChanged();
                                        drawMap(-1, tag, position);
                                    }
                                } else {
                                    for (ObsMasterBan ban : obs_list) {
                                        ban.getAdapter().setIndext(-1);
                                        ban.getAdapter().notifyDataSetChanged();
                                    }
                                    obsMasterBan.getAdapter().setDeleat(position);
                                    obsMasterBan.getAdapter().notifyDataSetChanged();
                                    drawMap(-1, tag, position);
                                }
                                break;
                                default:break;
                        }
                    }
                });
                adapter.setLongClickListener(new BarrierAdapter.LongClickListener() {
                    @Override
                    public void onLongClicked(View view, int position) {
                        int tag = (int) view.getTag();
                        mapFragment.setLoaction(
                                obs_list.get(tag).getList().get(position).getLatitude()
                                , obs_list.get(tag).getList().get(position).getLongitude()
                                , 0);
                        if (last_obs.equals(tag + "" + position)) {
                            for (ObsMasterBan ban : obs_list) {
                                ban.getAdapter().setDelete(false);
                                ban.getAdapter().setIndext(-1);
                                ban.getAdapter().notifyDataSetChanged();
                            }
                            last_obs = -1 + "";
                            drawMap(-1, -1, -1);
                            record_point.setText(getString(R.string.DotActivity_tv6));
                            backout_state(false);
                        } else {
                            last_obs = tag + "" + position;
                            for (ObsMasterBan ban : obs_list) {
                                ban.getAdapter().setIndext(-1);
                                ban.getAdapter().setDeleat(-1);
                                ban.getAdapter().setDelete(true);
                                ban.getAdapter().notifyDataSetChanged();
                            }
                            obsMasterBan.getAdapter().setDeleat(position);
                            obsMasterBan.getAdapter().notifyDataSetChanged();
                            drawMap(-1, tag, -1);
                            record_point.setText(getString(R.string.DotActivity_tv17));
                            backout_state(true);
                        }
                    }
                });
            }
        };
        recycle_view_obstacie.setAdapter(obsAdapter);
    }

    private void backout_state(boolean is_show) {
        if (is_show) {
            if (backout_list.size() > 0)
                but_backout.setVisibility(View.VISIBLE);
            else
                but_backout.setVisibility(View.GONE);
        } else {
            but_backout.setVisibility(View.GONE);
        }
    }


    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    onFinish();
                    break;
                case 1:
                    try {
                        mapFragment.setLoaction(ggaBan);
                    } catch (Exception e) {
                    }
                    break;
                case 2:
                    tipDialog.dismiss();
                    break;
            }
        }
    };

    private Runnable request_run = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            if (BlueToochActivity.mConnected) {
                OrderUtils.requestGGA();
                SystemClock.sleep(10);
                OrderUtils.requestChannel();
                SystemClock.sleep(10);
                OrderUtils.requestSystems();

                if (TextUtils.isEmpty(BaseActivity.workmoder)) {
                    warming_popu_massage.setText(getString(R.string.ParticularsActivity_tv1));
                    warming_popu_massage.setVisibility(View.VISIBLE);
                } else if (!BaseActivity.workmoder.equals("手持杖")) {
                    warming_popu_massage.setText(getString(R.string.ParticularsActivity_tv2));
                    warming_popu_massage.setVisibility(View.VISIBLE);
                } else {
                    warming_popu_massage.setVisibility(View.INVISIBLE);
                }

                if (top_icon.getVisibility() == View.INVISIBLE)
                    top_icon.setVisibility(View.VISIBLE);

                if (BaseActivity.channle < 8 && BaseActivity.channle >= 0)
                    top_channle_tv.setText(BaseActivity.channle + "");

                if (!top_name.getText().toString().equals(BlueToochActivity.device.getName()))
                    top_name.setText(BlueToochActivity.device.getName());

                if (ggaBan != null) {
                    if (!ggaBan.isEmpty()) {
                        mapFragment.setLoaction(ggaBan.getLat(), ggaBan.getLon(), 1);
                    }
                    if (!TextUtils.isEmpty(ggaBan.getRtk())) {
                        if (!ggaBan.getRtk().equals("4")) {
                            top_name.setText(getString(R.string.DotActivity_tv18));
                        }
                    }
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
            }
//            Log.e(ggaBan.toString());
            handler.postDelayed(this, 500);
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_top_show:
                if (!drawerlayout.isDrawerOpen(Gravity.LEFT))
                    drawerlayout.openDrawer(Gravity.LEFT);
                break;
            case R.id.top_channle:
                if (!BlueToochActivity.mConnected) {
                    ToastUtil.show(getString(R.string.toast_3));
                    return;
                }
                if (!BaseActivity.workmoder.equals("手持杖")) {
                    ToastUtil.show(getString(R.string.toast_2));
                    return;
                }
                startActivity(new Intent(getBaseContext(), SetChannelActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                break;
            case R.id.details_back:
                onFinish();
                break;
            case R.id.details_options:
                choicedialog.show();
                break;
            case R.id.but_backout:
                int size = backout_list.size();
                if (size > 0) {
                    Pair<String, Point> pair = backout_list.get(size - 1);
                    String[] split = pair.first.split(",");
                    if (split[0].startsWith("bounary")) {
                        boundary_List.add(Integer.parseInt(split[1]), pair.second);
                        matchArea(false);
                        boundaryAdapter.notifyDataSetChanged();
                        if (boundary_List.size() != 0) {
                            null_view_boundary.setVisibility(View.GONE);
                            recycle_view_boundary.setVisibility(View.VISIBLE);
                        }
                    } else if (split[0].startsWith("obs")) {
                        if (split[1].equals("no")) {
                            obs_list.get(Integer.parseInt(split[2])).getList().add(Integer.parseInt(split[3]), pair.second);
                            obs_list.get(Integer.parseInt(split[2])).getAdapter().notifyDataSetChanged();
                        } else if (split[1].equals("yes")) {
                            ArrayList<Point> list = new ArrayList<Point>();
                            list.add(pair.second);
                            obs_list.add(Integer.parseInt(split[2]), new ObsMasterBan(list, Integer.parseInt(split[2]), getBaseContext()));
                            for (int i = 0; i < obs_list.size(); i++) {
                                obs_list.get(i).getAdapter().setTag(i);
                                obs_list.get(i).getAdapter().notifyDataSetChanged();
                            }
                        }

                        if (obs_list.size() == 1 && obs_list.get(0).getList().size() == 0) {
                            null_view_obstacie.setVisibility(View.VISIBLE);
                            recycle_view_obstacie.setVisibility(View.GONE);
                        } else if (obs_list.size() == 0) {
                            null_view_obstacie.setVisibility(View.VISIBLE);
                            recycle_view_obstacie.setVisibility(View.GONE);
                        } else {
                            null_view_obstacie.setVisibility(View.GONE);
                            recycle_view_obstacie.setVisibility(View.VISIBLE);
                        }


                        obsAdapter.notifyDataSetChanged();
                    }
                    backout_list.remove(size - 1);
                    backout_state(true);
                    if (backout_list.size() < 1) {
                        record_point.setText(getString(R.string.DotActivity_tv6));
                        for (ObsMasterBan ban : obs_list) {
                            ban.getAdapter().setIndext(-1);
                            ban.getAdapter().setDelete(false);
                            ban.getAdapter().notifyDataSetChanged();
                        }
                        boundaryAdapter.setDeleat(false);
                        boundaryAdapter.setIndext(-1);
                        boundaryAdapter.notifyDataSetChanged();
                    }
//                    mapFragment.setLoaction(pair.second.getAltitude(), pair.second.getLongitude(), 0);
                    drawMap(-1, -1, -1);
                }
                break;
            case R.id.but_commit:
                if (boundary_List.size() < 3) {
                    ToastUtil.show(getString(R.string.toast_9));
                    return;
                }
                if (obs_list.size() != 0) {
                    for (ObsMasterBan ban : obs_list) {
                        if (ban.getList().size() < 3 && ban.getList().size() > 0) {
                            ToastUtil.show(getString(R.string.toast_10));
                            return;
                        }
                    }
                }

                if (boundary_List.size() > 3) {
                    try {
                        LineUtil.judgeList(boundary_List);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("", e);
                        ToastUtil.show(e.getMessage());
                        return;
                    }
                }

                for (ObsMasterBan ban : obs_list) {
                    if (ban.getList() != null)
                        if (ban.getList().size() > 3) {
                            try {
                                LineUtil.judgeList(ban.getList());
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("", e);
                                ToastUtil.show(e.getMessage());
                                return;
                            }
                        }
                }

                fragment_commit.setVisibility(View.VISIBLE);
                fragment_record.setVisibility(View.GONE);
                but_commit.setVisibility(View.INVISIBLE);
                if (infoBean != null)
                    commit_name.setText(infoBean.getTitle());
                break;
            case R.id.commit_out:
                if (fragment_commit.getVisibility() == View.VISIBLE) {
                    fragment_commit.setVisibility(View.GONE);
                    fragment_record.setVisibility(View.VISIBLE);
                    but_commit.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.commit_save:
                upMapping();
                break;
            case R.id.abort:
                dotgiveUpPopu.show();
                break;
            case R.id.nav_home:
                listSize();
                if (MappingRecordActivity.activity != null) {
                    MappingRecordActivity.activity.finish();
                    MappingRecordActivity.activity = null;
                }
                onFinish();
                break;
            case R.id.nav_connect:
                permissionsing();
//                listSize();
                startActivity(new Intent(getBaseContext(), EquipmentActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                break;
            case R.id.nav_his:
                listSize();
                if (MappingRecordActivity.activity != null) {
                    MappingRecordActivity.activity.finish();
                    MappingRecordActivity.activity = null;
                }
                startActivity(new Intent(getBaseContext(), MappingRecordActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
                if (is_detial) finish();
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
//                listSize();
                startActivity(new Intent(DotActivity.this, SetChannelActivity.class));
//                overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_still);
                break;
            case R.id.ll_boundary:
                if (MODE == MODE_BOUNDARY) return;
                viewPager.setCurrentItem(0, true);//参数一是ViewPager的position,参数二为是否有滑动效果
                break;
            case R.id.ll_obstacle:
                if (MODE == MODE_OBSTACLE) return;
                viewPager.setCurrentItem(1, true);//参数一是ViewPager的position,参数二为是否有滑动效果
                break;
            case R.id.location_dot:
                if (is_detial) {
                    Point centerPoint = Point.getCenterPoint(boundary_List);
                    mapFragment.setZoom(boundary_List, centerPoint);
                    mapFragment.setLoaction(centerPoint.getLatitude(), centerPoint.getLongitude(), 0);
                } else {
                    mapFragment.setLoaction(ggaBan);
                }
                break;
            case R.id.map_model:
                int i = mapFragment.setMapMopType();
                if (i == 1) {
                    setTopBlack();
                } else {
                    setTopWhite();
                }
                break;
            case R.id.record_point:
                String s = record_point.getText().toString();
                if (s.equals(getString(R.string.DotActivity_tv17))) return;
                if (!BlueToochActivity.mConnected) {
                    ToastUtil.show(getString(R.string.toast_3));
                    return;
                }
                if (ggaBan == null) {
                    ToastUtil.show(getString(R.string.toast_11));
                    return;
                }
                if (ggaBan.isEmpty()) {
                    ToastUtil.show(getString(R.string.toast_11));
                    return;
                }
                if (!ggaBan.getRtk().equals("4") | ggaBan.getHdop() > 1.3) {
                    ToastUtil.show(getString(R.string.toast_12));
                    return;
                }
                try {
                    List<LatLng> latLngs = OBSMatch.judgeNoFly(ggaBan.getLat(), ggaBan.getLon(), BaseActivity.second);
                    if (latLngs != null) {
                        ToastUtil.show(getString(R.string.toast_13));
                        mapFragment.DrawNoFly(latLngs);
                        return;
                    }
                } catch (Exception e) {
                    ToastUtil.show(getString(R.string.toast_13));
                    Log.e("", e);
                    return;
                }

                if (DistanceUtil.getDistance(
                        new LatLng(old_ggaBan.getLat(), old_ggaBan.getLon()),
                        new LatLng(ggaBan.getLat(), ggaBan.getLon()))
                        > 0.2) {
                    ToastUtil.show(getString(R.string.toast_12));
                    return;
                }
                switch (MODE) {
                    case MODE_BOUNDARY:
                        recordBoundarys(s);
                        break;
                    case MODE_OBSTACLE:
                        recordObstacle(s);
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }


    private void listSize() {
        if (boundary_List.size() != 0 | obs_list.size() != 0) {
            BlueToochActivity.boundaryList = boundary_List;
            BlueToochActivity.obsList = obs_list;
            BlueToochActivity.backout_list = backout_list;
            BlueToochActivity.title = commit_name.getText().toString();
        } else {
            BlueToochActivity.boundaryList = null;
            BlueToochActivity.obsList = null;
            BlueToochActivity.backout_list = null;
            BlueToochActivity.title = null;
        }
    }

    private void recordBoundarys(String s) {
        //正常打点
        if (s.equals(getString(R.string.DotActivity_tv6))) {
            boundary_List.add(new Point(ggaBan.getLon(), ggaBan.getLat(), ggaBan.getAlt()));
            if (boundary_List.size() >= 2) {
                int b = boundary_List.size();
                if (DistanceUtil.getDistance(
                        new LatLng(boundary_List.get(b - 1).getLatitude(), boundary_List.get(b - 1).getLongitude()),
                        new LatLng(boundary_List.get(b - 2).getLatitude(), boundary_List.get(b - 2).getLongitude()))
                        <= 1) {
                    boundary_List.remove(b - 1);
                    ToastUtil.show(getString(R.string.toast_14));
                    return;
                }
            }

            if (boundary_List.size() != 0) {
                null_view_boundary.setVisibility(View.GONE);
                recycle_view_boundary.setVisibility(View.VISIBLE);
            }

            int i = boundary_List.size() - 1;
            recycle_view_boundary.smoothScrollToPosition(i);
            mapFragment.setLoaction(boundary_List.get(i).getLatitude(), boundary_List.get(i).getLongitude(), 0);
        }
        //插入点
        else if (s.equals(getString(R.string.DotActivity_tv16))) {
            int indext = boundaryAdapter.getIndext();
            Point point = new Point(ggaBan.getLon(), ggaBan.getLat(), ggaBan.getAlt());
            boundary_List.add(indext, point);

            if (indext == 0) {
                if (DistanceUtil.getDistance(
                        new LatLng(point.getLatitude(), point.getLongitude()),
                        new LatLng(boundary_List.get(1).getLatitude(), boundary_List.get(1).getLongitude()))
                        <= 1) {
                    boundary_List.remove(0);
                    ToastUtil.show(getString(R.string.toast_14));
                    return;
                }
            } else if (indext >= 1) {
                if (DistanceUtil.getDistance(
                        new LatLng(point.getLatitude(), point.getLongitude()),
                        new LatLng(boundary_List.get(indext - 1).getLatitude(), boundary_List.get(indext - 1).getLongitude()))
                        <= 1 | DistanceUtil.getDistance(
                        new LatLng(point.getLatitude(), point.getLongitude()),
                        new LatLng(boundary_List.get(indext + 1).getLatitude(), boundary_List.get(indext + 1).getLongitude()))
                        <= 1) {
                    boundary_List.remove(indext);
                    ToastUtil.show(getString(R.string.toast_14));
                    return;
                }
            }
            mapFragment.setLoaction(point.getLatitude(), point.getLongitude(), 0);
        }
        record_point.setText(getString(R.string.DotActivity_tv6));
        backout_state(false);
        boundaryAdapter.setIndext(-1);
        boundaryAdapter.notifyDataSetChanged();
        drawMap(-1, -1, -1);
        matchArea(false);
        backout_list.clear();
    }

    private void recordObstacle(String s) {
        //正常打点
        if (s.equals(getString(R.string.DotActivity_tv6))) {
            Point point = new Point(ggaBan.getLon(), ggaBan.getLat(), ggaBan.getAlt());
            if (obs_list.size() == 0) {
                obs_list.add(new ObsMasterBan(new ArrayList<Point>(), obs_list.size(), getBaseContext()));
                obsAdapter.notifyDataSetChanged();
                ObsMasterBan obsMasterBan = obs_list.get(obs_list.size() - 1);
                obsMasterBan.getList().add(point);
                obsMasterBan.getAdapter().notifyDataSetChanged();
                drawMap(-1, -1, -1);
                mapFragment.setLoaction(point.getLatitude(), point.getLongitude(), 0);

                null_view_obstacie.setVisibility(View.GONE);
                recycle_view_obstacie.setVisibility(View.VISIBLE);

                return;
            }

            ObsMasterBan obsMasterBan = obs_list.get(obs_list.size() - 1);
            ArrayList<Point> list = obsMasterBan.getList();
            list.add(point);

            if (list.size() >= 2) {
                int b = list.size();
                if (DistanceUtil.getDistance(
                        new LatLng(list.get(b - 1).getLatitude(), list.get(b - 1).getLongitude()),
                        new LatLng(list.get(b - 2).getLatitude(), list.get(b - 2).getLongitude()))
                        <= 0.1) {
                    list.remove(b - 1);
                    ToastUtil.show(getString(R.string.toast_14));
                    return;
                }
            }

            try {
                OBSMatch.match(obs_list, obs_list.size() - 1);
            } catch (Exception e) {
                list.remove(list.size() - 1);
                ToastUtil.show(getString(R.string.toast_15));
                return;
            }

            try {
                LineUtil.judge(obs_list, obs_list.size() - 1, list.size() - 1);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("", e);
                ToastUtil.show(getString(R.string.toast_16));
                list.remove(list.size() - 1);
                return;
            }


            null_view_obstacie.setVisibility(View.GONE);
            recycle_view_obstacie.setVisibility(View.VISIBLE);

            mapFragment.setLoaction(point.getLatitude(), point.getLongitude(), 0);
            obsAdapter.notifyDataSetChanged();
            obsMasterBan.getAdapter().notifyDataSetChanged();
            obsMasterBan.getRecyclerView().smoothScrollToPosition(list.size() - 1);
//            obsMasterBan.getRecyclerView().scrollToPosition(list.size() - 1);
//            ((LinearLayoutManager) obsMasterBan.getRecyclerView().getLayoutManager()).scrollToPositionWithOffset(list.size() - 1,list.size() - 1);
        }
        //插入点
        else if (s.equals(getString(R.string.DotActivity_tv16))) {
            Point point = new Point(ggaBan.getLon(), ggaBan.getLat(), ggaBan.getAlt());
            ObsMasterBan obsMasterBan = obs_list.get(insert_index);
            ArrayList<Point> list = obsMasterBan.getList();
            list.add(insert_position, point);


            if (insert_position == 0) {
                int b = list.size();
                if (DistanceUtil.getDistance(
                        new LatLng(list.get(b - 1).getLatitude(), list.get(b - 1).getLongitude()),
                        new LatLng(list.get(0).getLatitude(), list.get(0).getLongitude()))
                        <= 0.1 | DistanceUtil.getDistance(
                        new LatLng(list.get(1).getLatitude(), list.get(1).getLongitude()),
                        new LatLng(list.get(0).getLatitude(), list.get(0).getLongitude()))
                        <= 0.1) {
                    list.remove(insert_position);
                    ToastUtil.show(getString(R.string.toast_14));
                    return;
                }
            } else {
                int b = list.size();
                if (DistanceUtil.getDistance(
                        new LatLng(list.get(insert_position - 1).getLatitude(), list.get(insert_position - 1).getLongitude()),
                        new LatLng(list.get(insert_position).getLatitude(), list.get(insert_position).getLongitude()))
                        <= 0.1 | DistanceUtil.getDistance(
                        new LatLng(list.get(insert_position + 1).getLatitude(), list.get(insert_position + 1).getLongitude()),
                        new LatLng(list.get(insert_position).getLatitude(), list.get(insert_position).getLongitude()))
                        <= 0.1) {
                    list.remove(insert_position);
                    ToastUtil.show(getString(R.string.toast_14));
                    return;
                }
            }


            try {
                OBSMatch.match(obs_list, obs_list.size() - 1);
            } catch (Exception e) {
                list.remove(insert_position);
                ToastUtil.show(getString(R.string.toast_15));
                return;
            }

            try {
                LineUtil.judge(obs_list, obs_list.size() - 1, insert_position);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("", e);
                ToastUtil.show(getString(R.string.toast_16));
                list.remove(insert_position);
                return;
            }

            mapFragment.setLoaction(point.getLatitude(), point.getLongitude(), 0);
            record_point.setText(getString(R.string.DotActivity_tv6));
            backout_state(false);
            obsMasterBan.getAdapter().setIndext(-1);
            obsMasterBan.getAdapter().notifyDataSetChanged();
        }
        drawMap(-1, -1, -1);
        backout_list.clear();
    }

    private String matchArea(Boolean accuracy) {
        String s = Area.mathArea(boundary_List);
        if (accuracy) {
            return s;
        } else {
            DecimalFormat df = new DecimalFormat("#0.00");
            String format = df.format(Double.parseDouble(s));
            dot_tv_area.setText(format);
            return null;
        }
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(intent.getAction())) { //连接一个GATT服务
                handler.removeCallbacks(request_run);
                handler.postDelayed(request_run, 500);
                top_icon.setVisibility(View.VISIBLE);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(intent.getAction())) {  //从GATT服务中断开连接
                handler.removeCallbacks(request_run);
                ggaBan = null;
                top_name.setText(getString(R.string.ParticularsActivity_tv3));
                warming_popu_massage.setVisibility(View.INVISIBLE);
                top_rtk_im_icon.setBackgroundResource(R.mipmap.icon_rtk_nowork);
                top_channle_tv.setText("");
                top_icon.setVisibility(View.INVISIBLE);
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(intent.getAction())) {  //发现有可支持的服务
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.getAction())) {   //从服务中接受数据
                ggaBan = (GGABan) intent.getSerializableExtra(BluetoothLeService.EXTRA_DATA);
                if (!ggaBan.isEmpty() && ggaBan.getTime() == 3) {
                    old_ggaBan.setLat(ggaBan.getLat());
                    old_ggaBan.setLon(ggaBan.getLon());
                }
            }
        }
    };

    private void upMapping() {
        try {
            final String title = commit_name.getText().toString();
            if (TextUtils.isEmpty(title)) {
                ToastUtil.show(getString(R.string.toast_17));
                return;
            }

            commit_save.setClickable(false);
            tipDialog.show();

            final String area = matchArea(true);

            JSONArray bound = new JSONArray();
            JSONArray obs = new JSONArray();
            JSONArray start = new JSONArray();

            for (Point point : boundary_List) {
                bound.put(new JSONArray().put(point.getLatitude()).put(point.getLongitude()));
            }

            for (ObsMasterBan ban : obs_list) {
                JSONArray barrier1 = new JSONArray();
                for (Point point : ban.getList()) {
                    if (ban.getList().size() != 0) {
                        barrier1.put(new JSONArray().put(point.getLatitude()).put(point.getLongitude()));
                    }
                }
                if (barrier1.length() != 0) {
                    obs.put(barrier1);
                }
            }

            start.put(new JSONArray().put(boundary_List.get(0).getLatitude()).put(boundary_List.get(0).getLongitude()));

            final String bound_ss = bound.toString();
            final String obs_ss = obs.toString();
            final String start_ss = start.toString();

            new ThreadPoolUtils(ThreadPoolUtils.SingleThread, 0).execute(new Runnable() {
                @Override
                public void run() {
                    final String url = APP.url + "/work2.0/Public/?service=Mapping.upMapping&token=" + BaseActivity.TOKEN;
                    DbBan dbBan = new DbBan();

                    Map<String, String> params = new HashMap<>();
                    if (is_recover) {
                        if (!TextUtils.isEmpty(BlueToochActivity.Id)) {
                            params.put("Id", BlueToochActivity.Id);
                            dbBan.setId(BlueToochActivity.Id);
                        }
                    }

                    dbBan.setCreate_name(BaseActivity.username);
                    dbBan.setTitle(title);
                    dbBan.setTid(String.valueOf(BaseActivity.teamid));
                    dbBan.setMappingBorder(bound_ss);
                    dbBan.setObstacleBorder(obs_ss);
                    dbBan.setStartBorder(start_ss);
                    dbBan.setArea(area);
                    dbBan.setType("border");
                    dbBan.setParentId("0");

                    params.put("title", title);
                    params.put("tid", String.valueOf(BaseActivity.teamid));
                    params.put("mappingBorder", bound_ss);
                    params.put("obstacleBorder", obs_ss);
                    params.put("startBorder", start_ss);
                    params.put("area", area);
                    params.put("parentId", "0");
                    params.put("type", "border");
                    String result = HttpUrlTool.submitPostData(url, params, "utf-8");
//                    Log.e("after request");
                    JSONObject json = null;
//                    Log.e("doc  "+ result);
                    try {
                        json = new JSONObject(result);
                        String msg = json.getString("msg");
                        String ret = json.getString("ret");
                        String data = json.getString("data");
//                        Log.e(msg + "   " + ret + "  " + data);
                        if (ret.equals("200")) {
                            dbBan.setSync("true");
                            if (data.equals("[1]")) {
//                                Log.e("修改成功");
                            } else if (data.equals("[0]")) {
//                                Log.e("修改失败");
                            } else {
                                String[] split = data.split("\"");
//                                Log.e("上传成功  Id:" + split[1]);
                                dbBan.setId(split[1]);
                            }
                        } else {
                            dbBan.setSync("false");
                        }
                        upDB(dbBan);
                    } catch (Exception e) {
                        Log.e("", e);
                        handler.sendEmptyMessage(2);
                        commit_save.setClickable(true);
                        dbBan.setSync("false");
                        upDB(dbBan);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendEmptyMessage(2);
            ToastUtil.show(getString(R.string.toast_18));
            commit_save.setClickable(true);
        }
    }

    private void upDB(DbBan dbBan) {
        if (is_recover) {
            if (TextUtils.isEmpty(BlueToochActivity.insertTime)) {
                dbBan.setInsertTime(DateUtil.getCurDate("yyyy-MM-dd HH:mm:ss"));
                sql.addBan(dbBan);
            } else {
                dbBan.setInsertTime(BlueToochActivity.insertTime);
                sql.updateBan(dbBan);
            }
        } else {
            dbBan.setInsertTime(DateUtil.getCurDate("yyyy-MM-dd HH:mm:ss"));
            sql.addBan(dbBan);
        }
        if (dbBan.getSync().equals("true")) {
            handler.sendEmptyMessage(2);
            ToastUtil.show(getString(R.string.toast_19));
        } else {
            handler.sendEmptyMessage(2);
            ToastUtil.show(getString(R.string.toast_19));
        }

        BlueToochActivity.Id = null;
        BlueToochActivity.boundaryList = null;
        BlueToochActivity.obsList = null;
        BlueToochActivity.backout_list = null;
        BlueToochActivity.insertTime = null;
        onFinish();
    }

    private void delete() {

        if (infoBean.getSync()) new Thread(new Runnable() {
            @Override
            public void run() {
                String url = APP.url + "/work2.0/Public/?service=Mapping.editMappingStatus&token=" + BaseActivity.TOKEN;
                Map<String, String> params = new HashMap<>();
                params.put("Id", infoBean.getId());
                params.put("status", "0");
                String result = HttpUrlTool.submitPostData(url, params, "utf-8");
                JSONObject json = null;
//                Log.e(result);
                try {
                    json = new JSONObject(result);
                    String msg = json.getString("msg");
                    String ret = json.getString("ret");
                    String data = json.getString("data");
//                    Log.e(msg + "   " + ret + "  " + data);
                    if (ret.equals("200")) {
                        if (data.equals("[1]")) {
                            ToastUtil.show(getString(R.string.toast_20));
                            sql.deleteTime(infoBean.getInsertTime());
                            SystemClock.sleep(1000);
                            onFinish();
                        } else if (data.equals("[0]")) {
                            ToastUtil.show(getString(R.string.toast_21));
                        } else {
//                            Log.e("上传成功  Id=" + data);
                        }
                    }
                } catch (Exception e) {
//                    Log.e("", e);
                }
            }
        }).start();
        else {
            ToastUtil.show(getString(R.string.toast_20));
            sql.deleteTime(infoBean.getInsertTime());
            onFinish();
        }
    }

}

