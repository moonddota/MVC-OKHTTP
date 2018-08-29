package com.skylin.uav.drawforterrain.select.hand;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.blankj.utilcode.util.ThreadPoolUtils;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton;
import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.HttpUrlTool;
import com.skylin.uav.drawforterrain.adapter.BarrierAdapter;
import com.skylin.uav.drawforterrain.adapter.BoundaryAdapter;
import com.skylin.uav.drawforterrain.adapter.MPagerAdapter;
import com.skylin.uav.drawforterrain.fragment.MapEventsOverlay;
import com.skylin.uav.drawforterrain.fragment.OsmFragment;
import com.skylin.uav.drawforterrain.login.LoginActivity;
import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.drawforterrain.select.gps_uav.GpsUavActivity;
import com.skylin.uav.drawforterrain.setting_channel.ObsMasterBan;
import com.skylin.uav.drawforterrain.setting_channel.ParticularsActivity;
import com.skylin.uav.drawforterrain.setting_channel.db.DbBan;
import com.skylin.uav.drawforterrain.setting_channel.db.DbSQL;
import com.skylin.uav.drawforterrain.util.Area;
import com.skylin.uav.drawforterrain.util.DateUtil;
import com.skylin.uav.drawforterrain.util.LineUtil;
import com.skylin.uav.drawforterrain.util.OBSMatch;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.skylin.uav.drawforterrain.views.ViewPager;
import com.skylin.uav.R;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
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

public class HandSelect_Activity extends BaseActivity implements View.OnClickListener {

    private FrameLayout map_hand;
    private OsmFragment mapFragment;
    private QMUIRoundButton record_point;
    private LinearLayout ll_area;          //面积父布局
    private TextView dot_tv_area;          //面积

    private LinearLayout ll_map_dot;
    private ImageButton location_dot;

    private QMUIRoundButton but_commit;
    private LinearLayout fragment_commit, fragment_record;
    private QMUIRoundButton commit_out;
    private QMUIRoundButton commit_save;
    private EditText commit_name;

    private ViewPager viewPager;
    private TextView tv_boundary, tv_obstacle;    // table  边界点  障碍点
    private View group_boundary, group_obstacle;
    private LinearLayout ll_boundary, ll_obstacle;
    private final int MODE_BOUNDARY = 0;   //状态  边界点
    private final int MODE_OBSTACLE = 1;    //状态  障碍点
    private int MODE = MODE_BOUNDARY;

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

    private DbSQL sql;

    private QMUIRoundButton but_backout;    //撤销按钮
    private ArrayList<Pair<String, Point>> backout_list = new ArrayList();

    private ExecutorService threadPool;
    private Future<?> record_submit;

    private QMUITipDialog tipDialog;

    private int screenWidth;
    private int screenHeight;
    private boolean addPoint = false;
    private boolean is_marker = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handselect);

        threadPool = Executors.newSingleThreadExecutor();
        sql = new DbSQL(getBaseContext(), "teamid" + BaseActivity.teamid, null, 1);
        if (sql == null) {
            ToastUtil.show(getString(R.string.toast_spl_un));
            finish();
        }

        initViews();
        initMap();

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.DotActivity_tv14))
                .create();

        screenWidth = getWindowManager().getDefaultDisplay().getWidth(); // 屏幕宽（像素，如：480px）
        screenHeight = getWindowManager().getDefaultDisplay().getHeight(); // 屏幕高（像素，如：800p）

        findViewById(R.id.hand_point).setVisibility(View.VISIBLE);

        TextView top_name = findViewById(R.id.top_name);
        top_name.setText("");
        ImageView imageView = findViewById(R.id.top_show);
        imageView.setBackgroundResource(R.mipmap.ic_return);
        findViewById(R.id.ll_top_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = record_point.getText().toString();
                if (s.equals(getString(R.string.DotActivity_tv17)) | s.equals(getString(R.string.DotActivity_tv16))) {
                    record_point.setText(getString(R.string.DotActivity_tv6));
                    drawMap(-1, -1, -1);
                    backout_state(false);
                    boundaryAdapter.setDeleat(false);
                    boundaryAdapter.setIndext(-1);
                    boundaryAdapter.notifyDataSetChanged();
                    for (ObsMasterBan ban : obs_list) {
                        ban.getAdapter().setIndext(-1);
                        ban.getAdapter().setDelete(false);
                        ban.getAdapter().notifyDataSetChanged();
                    }
                } else {
                    new QMUIDialog.MessageDialogBuilder(HandSelect_Activity.this)
                            .setTitle(getString(R.string.continuePopu_tv1))
                            .setMessage(getString(R.string.drawerlayout_tv9))
                            .addAction(getString(R.string.continuePopu_tv12), new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .addAction(getString(R.string.continuePopu_tv9), new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                    onFinish();
                                    overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);

                                }
                            })
                            .show();
                }
            }
        });
    }

    private void initMap() {
        ll_map_dot = findViewById(R.id.ll_map_dot);
        location_dot = findViewById(R.id.location_dot);
        location_dot.setOnClickListener(this);

        ll_area = findViewById(R.id.ll_area);
        dot_tv_area = findViewById(R.id.dot_tv_area);

        mapFragment = new OsmFragment();
        FragmentManager FM = getFragmentManager();
        FragmentTransaction ft = FM.beginTransaction();
        ft.replace(R.id.map_hand, mapFragment);
        ft.commit();

        mapFragment.setMarkerClickListener(new OsmFragment.MarkerClickListener() {
            @Override
            public void onMarkerClickListener(Marker marker, MapView mapView) {
                if (is_marker) return;
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

    MapEventsOverlay mapEventOverlay = new MapEventsOverlay(this, 100, 100, new MapEventsReceiver() {

        @Override
        public boolean singleTapConfirmedHelper(final GeoPoint p) {
//            sjj.alog.Log.e(p.getLatitude() + "  " + p.getLongitude() + "  " + p.getAltitude());
            if (addPoint) {
                recordPoint(p);
                addPoint = false;
            }
            return false;
        }

        @Override
        public boolean longPressHelper(GeoPoint p) {
            return false;
        }

    });

    private void recordPoint(GeoPoint p) {
        try {
            List<LatLng> latLngs = OBSMatch.judgeNoFly(p.getLatitude(), p.getLongitude(), BaseActivity.second);
            if (latLngs != null) {
                ToastUtil.show(getString(R.string.toast_13));
                mapFragment.DrawNoFly(latLngs);
                return;
            }
        } catch (Exception e) {
            ToastUtil.show(e.getMessage());
            sjj.alog.Log.e("", e);
        }

        if (record_point.getText().toString().equals(getString(R.string.DotActivity_tv6))) {
            record_point.setText(getString(R.string.DotActivity_tv6));
            switch (MODE) {
                case 0:
                    boundary_List.add(new Point(p.getLongitude(), p.getLatitude(), p.getAltitude()));

                    if (boundary_List.size() != 0) {
                        null_view_boundary.setVisibility(View.GONE);
                        recycle_view_boundary.setVisibility(View.VISIBLE);
                    }
                    recycle_view_boundary.smoothScrollToPosition(boundary_List.size() - 1);
                    boundaryAdapter.setIndext(-1);
                    boundaryAdapter.notifyDataSetChanged();
                    drawMap(-1, -1, -1);
                    matchArea(false);
                    backout_state(false);
                    backout_list.clear();
                    break;
                case 1:
                    Point point = new Point(p.getLongitude(), p.getLatitude(), p.getAltitude());
                    if (obs_list.size() == 0) {
                        obs_list.add(new ObsMasterBan(new ArrayList<Point>(), obs_list.size(), getBaseContext()));
                        obsAdapter.notifyDataSetChanged();
                        ObsMasterBan obsMasterBan = obs_list.get(obs_list.size() - 1);
                        obsMasterBan.getList().add(point);
                        obsMasterBan.getAdapter().notifyDataSetChanged();
                        drawMap(-1, -1, -1);

                        null_view_obstacie.setVisibility(View.GONE);
                        recycle_view_obstacie.setVisibility(View.VISIBLE);
                        break;
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
                            break;
                        }
                    }

                    try {
                        OBSMatch.match(obs_list, obs_list.size() - 1);
                    } catch (Exception e) {
                        list.remove(list.size() - 1);
                        ToastUtil.show(getString(R.string.toast_14));
                        break;
                    }

                    try {
                        LineUtil.judge(obs_list, obs_list.size() - 1, list.size() - 1);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("", e);
                        ToastUtil.show(e.getMessage());
                        list.remove(list.size() - 1);
                        break;
                    }

                    null_view_obstacie.setVisibility(View.GONE);
                    recycle_view_obstacie.setVisibility(View.VISIBLE);

                    obsAdapter.notifyDataSetChanged();
                    obsMasterBan.getAdapter().notifyDataSetChanged();
                    obsMasterBan.getRecyclerView().smoothScrollToPosition(list.size() - 1);
                    drawMap(-1, -1, -1);
                    backout_state(false);
                    backout_list.clear();
                    break;
                    default:break;
            }
        } else if (record_point.getText().toString().equals(getString(R.string.DotActivity_tv16))) {
            record_point.setText(getString(R.string.DotActivity_tv6));
            switch (MODE) {
                case 0:
                    int indext = boundaryAdapter.getIndext();
                    Point point = new Point(p.getLongitude(), p.getLatitude(), p.getAltitude());
                    boundary_List.add(indext, point);

                    if (indext == 0) {
                        if (DistanceUtil.getDistance(
                                new LatLng(point.getLatitude(), point.getLongitude()),
                                new LatLng(boundary_List.get(1).getLatitude(), boundary_List.get(1).getLongitude()))
                                <= 1) {
                            boundary_List.remove(0);
                            ToastUtil.show(getString(R.string.toast_14));
                            break;
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
                            break;
                        }
                    }
                    boundaryAdapter.setIndext(-1);
                    boundaryAdapter.notifyDataSetChanged();
                    drawMap(-1, -1, -1);
                    matchArea(false);
                    backout_state(false);
                    backout_list.clear();
                    break;
                case 1:
                    Point point1 = new Point(p.getLongitude(), p.getLatitude(), p.getAltitude());
                    ObsMasterBan obsMasterBan = obs_list.get(insert_index);
                    ArrayList<Point> list = obsMasterBan.getList();
                    list.add(insert_position, point1);

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
                            break;
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
                            break;
                        }
                    }

                    try {
                        OBSMatch.match(obs_list, obs_list.size() - 1);
                    } catch (Exception e) {
                        list.remove(insert_position);
                        ToastUtil.show(getString(R.string.toast_15));
                        break;
                    }

                    try {
                        LineUtil.judge(obs_list, obs_list.size() - 1, insert_position);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("", e);
                        ToastUtil.show(e.getMessage());
                        list.remove(insert_position);
                        break;
                    }
                    obsAdapter.notifyDataSetChanged();
                    obsMasterBan.getAdapter().setIndext(-1);
                    obsMasterBan.getAdapter().notifyDataSetChanged();
                    drawMap(-1, -1, -1);
                    backout_state(false);
                    backout_list.clear();
                    break;
                    default:break;
            }
        }
    }

    private void initViews() {
        but_backout = findViewById(R.id.but_backout);
        but_backout.setOnClickListener(this);

        but_commit = findViewById(R.id.but_commit);
        but_commit.setOnClickListener(this);
        fragment_commit = findViewById(R.id.fragment_commit);
        fragment_commit.setVisibility(View.GONE);
        fragment_record = findViewById(R.id.fragment_record);
        fragment_record.setVisibility(View.VISIBLE);
        commit_out = findViewById(R.id.commit_out);
        commit_out.setOnClickListener(this);
        commit_save = findViewById(R.id.commit_save);
        commit_save.setOnClickListener(this);
        commit_name = findViewById(R.id.commit_name);

        record_point = findViewById(R.id.record_point);
        record_point.setOnClickListener(this);

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
                        default:break;
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
                            record_point.setText(getString(R.string.DotActivity_tv6));
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
                                boundaryAdapter.setDeleat(false);
                                boundaryAdapter.setIndext(-1);
                                last_bounary = -1;
                                backout_state(false);
                                drawMap(-1, -1, -1);
                            } else {
                                last_bounary = position;
                                record_point.setText(getString(R.string.DotActivity_tv16));
                                boundaryAdapter.setDeleat(false);
                                backout_state(true);
                                boundaryAdapter.setIndext(position);
                                drawMap(position, -1, -1);
                            }
                        } else {
                            boundaryAdapter.setDelete(position);
                            drawMap(position, -1, -1);
                        }
                        boundaryAdapter.notifyDataSetChanged();
                        break;
                        default:break;
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
                                }else {
                                    backout_list.add(new Pair("obs,no," + deleat_barrier + "," + position, point));
                                }
                                drawMap(-1, -1, -1);
                                backout_state(true);
                                if (obs_list.size() == 1 && obs_list.get(0).getList().size() == 0) {
                                    null_view_obstacie.setVisibility(View.VISIBLE);
                                    recycle_view_obstacie.setVisibility(View.GONE);
                                    record_point.setText(getString(R.string.DotActivity_tv6));
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

    private void drawMap(final int b_index, final int obs_index, final int obs_position) {
        if (record_submit != null) {
            record_submit.cancel(true);
        }
        record_submit = threadPool.submit(new Runnable() {
            @Override
            public void run() {
                mapFragment.DrawMapping(
                        boundary_List, b_index,
                        obs_list, obs_index, obs_position);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapFragment.getMap().getOverlays().add(mapEventOverlay);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            case R.id.record_point:
                addPoint = true;
                is_marker = true;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        is_marker = false;
                    }
                }, 1000);

                mapFragment.getMap().dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, screenWidth / 2, screenHeight / 2, 0));
                mapFragment.getMap().dispatchTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, screenWidth / 2, screenHeight / 2, 0));

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
                mapFragment.setLoaction(null);
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
                default:break;
        }
    }

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

                    dbBan.setCreate_name(BaseActivity.username);
                    dbBan.setTitle(title);
                    dbBan.setTid(String.valueOf(BaseActivity.teamid));
                    dbBan.setMappingBorder(bound_ss);
                    dbBan.setObstacleBorder(obs_ss);
                    dbBan.setStartBorder(start_ss);
                    dbBan.setArea(area);
                    dbBan.setType("hand");
                    dbBan.setParentId("0");

                    Map<String, String> params = new HashMap<>();
                    params.put("title", title);
                    params.put("tid", String.valueOf(BaseActivity.teamid));
                    params.put("mappingBorder", bound_ss);
                    params.put("obstacleBorder", obs_ss);
                    params.put("startBorder", start_ss);
                    params.put("area", area);
                    params.put("parentId", "0");
                    params.put("type", "hand");
                    String result = HttpUrlTool.submitPostData(url, params, "utf-8");
                    JSONObject json = null;
                    Log.e("doc  "+ result);
                    try {
                        json = new JSONObject(result);
                        String msg = json.getString("msg");
                        String ret = json.getString("ret");
                        String data = json.getString("data");
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
                            if (ret.equals("409")) {
                                BaseActivity.TOKEN = null;
                            } else if (ret.equals("408")) {
//                                Log.e("尚未登陆");
                            }
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

        dbBan.setInsertTime(DateUtil.getCurDate("yyyy-MM-dd HH:mm:ss"));
        sql.addBan(dbBan);


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

    private void onFinish() {
        finish();
        overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    tipDialog.dismiss();
                    break;
                    default:break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//当keycode等于退出事件值时
            String s = record_point.getText().toString();
            if (s.equals(getString(R.string.DotActivity_tv17)) | s.equals(getString(R.string.DotActivity_tv16))) {
                record_point.setText(getString(R.string.DotActivity_tv6));
                drawMap(-1, -1, -1);
                backout_state(false);
                boundaryAdapter.setDeleat(false);
                boundaryAdapter.setIndext(-1);
                boundaryAdapter.notifyDataSetChanged();
                for (ObsMasterBan ban : obs_list) {
                    ban.getAdapter().setIndext(-1);
                    ban.getAdapter().setDelete(false);
                    ban.getAdapter().notifyDataSetChanged();
                }
            } else {
                new QMUIDialog.MessageDialogBuilder(HandSelect_Activity.this)
                        .setTitle(getString(R.string.continuePopu_tv1))
                        .setMessage(getString(R.string.drawerlayout_tv9))
                        .addAction(getString(R.string.continuePopu_tv12), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction(getString(R.string.DeviceListActivity_tv9), new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                                onFinish();
                                overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);

                            }
                        })
                        .show();
            }

            return false;
        } else
            return super.onKeyDown(keyCode, event);
    }
}
