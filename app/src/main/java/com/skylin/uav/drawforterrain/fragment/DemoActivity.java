package com.skylin.uav.drawforterrain.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.nofly.CivilAirport;
import com.skylin.uav.drawforterrain.nofly.NoFlyZoneLocalDataSource;
import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.drawforterrain.setting_channel.DownMappingBan;

import com.skylin.uav.drawforterrain.setting_channel.ParticularsActivity;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.skylin.uav.R;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

import sjj.alog.Log;
import task.model.Pair;
import task.model.Polygon;


/**
 * Created by Moon on 2018/3/28.
 */

public class DemoActivity extends AppCompatActivity implements View.OnClickListener {
    //    private MapFragment mapFragment;
    private OsmFragment mapFragment;


    public static void startDemoActivity(Activity activity, DownMappingBan.DataBean.InfoBean infoBean) {
        Intent intent = new Intent(activity, DemoActivity.class);
        intent.putExtra(BlueToochActivity.INFOBEAN, infoBean);  //  细节展示需要的数据
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_still);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BlueToochActivity.fullScreen(this);
        setContentView(R.layout.activity_demo);

        mapFragment = new OsmFragment();

        FragmentManager FM = getFragmentManager();
        FragmentTransaction ft = FM.beginTransaction();
        ft.replace(R.id.replace, mapFragment);
        ft.commit();

        findViewById(R.id.demo_location_dot).setOnClickListener(this);
        findViewById(R.id.demo_map_model).setOnClickListener(this);
        findViewById(R.id.alter).setOnClickListener(this);
        findViewById(R.id.clear).setOnClickListener(this);


        mapFragment.setMarkerClickListener(new OsmFragment.MarkerClickListener() {
            @Override
            public void onMarkerClickListener(Marker marker, MapView mapView) {
                ToastUtil.show(marker.getId());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            NoFlyZoneLocalDataSource flyZoneRepository = new NoFlyZoneLocalDataSource();
            Pair<List<CivilAirport>, List<List<Point>>> call = null;
            call = flyZoneRepository.call();
//            BaseActivity.civilAirports = call.first;     //取得机场禁飞区列表
//            BaseActivity.second = call.second;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.demo_location_dot:
                mapFragment.setLoaction(null);
                break;
            case R.id.demo_map_model:
                int i = mapFragment.setMapMopType();
                if (i == 1) {
                    ToastUtil.show("卫星地图");
                } else {
                    ToastUtil.show("一般地图");
                }
            case R.id.alter:

                ArrayList<Point> points = new ArrayList<>();
                points.add(new Point(104.1176718517, 30.5082683633));
                points.add(new Point(104.1176718517, 31.5082683633));
                points.add(new Point(105.1176718517, 31.5082683633));
//                points.add(new Point(105.1176718517, 30.5082683633));


                Polygon polygon = new Polygon(points,500);
                polygon.init(0);
                ArrayList<Point> safeFramePoint = (ArrayList<Point>) polygon.getSafeFramePoint();
                mapFragment.addBoundary(safeFramePoint,0);


                ArrayList<ArrayList<Point>> lists = new ArrayList<>();
//                lists.add(safeFramePoint);
                lists.add(points);

                for (Point point: safeFramePoint ) {
                    Log.e(point.toString());
                }

                mapFragment.drawList(lists);

                break;
            case R.id.clear:
                mapFragment.clearMap();
                break;
        }
    }


}
