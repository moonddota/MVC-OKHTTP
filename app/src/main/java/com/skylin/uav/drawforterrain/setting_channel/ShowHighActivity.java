package com.skylin.uav.drawforterrain.setting_channel;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.drawforterrain.service.BluetoothLeService;
import com.skylin.uav.drawforterrain.util.OrderUtils;
import com.skylin.uav.R;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;

public class ShowHighActivity extends BaseActivity {

    private RecyclerView rc_high;
    private CommonAdapter<Point> commonAdapter;
    private ArrayList<Point> list = new ArrayList<>();
    private FrameLayout lift_topbar;
    private TextView middle_topbar;
    private Button high_but;

    private GGABan ggaBan = new GGABan();
    private Handler handler = new Handler();
    private TextView high_gga;


    public static void startShowHighActivity(Activity activity, ArrayList<Point> boundary_List) {
        Intent intent = new Intent(activity, ShowHighActivity.class);
        intent.putExtra("boundary_List", boundary_List);
        activity.startActivity(intent);
    }

    public static void startShowHighActivity(Activity activity) {
        Intent intent = new Intent(activity, ShowHighActivity.class);
        activity.startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fullScreen(this);
        setContentView(R.layout.activity_showhigh);

        registerReceiver(mGattUpdateReceiver, BlueToochActivity.makeGattUpdateIntentFilter());

        handler.postDelayed(runnable, 1000);

//        Intent intent = getIntent();
//        intent.getParcelableArrayListExtra("boundary_List");
//        list =  (ArrayList<Point>) getIntent().getSerializableExtra("boundary_List");

        commonAdapter = new CommonAdapter<Point>(this, R.layout.item_high, list) {
            @Override
            protected void convert(ViewHolder holder, Point point, int position) {
                holder.setText(R.id.high_index, position + 1 + "");
                holder.setText(R.id.high_lat, point.getLatitude() + "");
                holder.setText(R.id.high_lon, point.getLongitude() + "");
                holder.setText(R.id.high_alt, point.getAltitude() + "");
            }
        };
        rc_high = findViewById(R.id.rc_high);
        rc_high.setLayoutManager(new LinearLayoutManager(this));
        rc_high.setHasFixedSize(true);
        rc_high.setAdapter(commonAdapter);

        middle_topbar = findViewById(R.id.middle_topbar);
        middle_topbar.setText("高度测量");

        lift_topbar = findViewById(R.id.lift_topbar);
        lift_topbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        high_but = findViewById(R.id.high_but);
        high_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ggaBan.isEmpty()) return;
                if (!ggaBan.getRtk().equals("4")) return;
                list.add(new Point(ggaBan.getLon(), ggaBan.getLat(), ggaBan.getAlt()));
                commonAdapter.notifyDataSetChanged();
            }
        });
        high_gga = findViewById(R.id.high_gga);
    }

    private Runnable runnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            if (BlueToochActivity.mConnected)
                OrderUtils.requestGGA();

            high_gga.setText(" 当前：纬度" + ggaBan.getLat() + "经度" + ggaBan.getLon() + "高"
                    + ggaBan.getAlt() + "卫星" + ggaBan.getSatellites()
                    + "rtk" + ggaBan.getRtk() + "hdop" + ggaBan.getHdop());

            handler.postDelayed(this, 1000);
        }
    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        handler.removeCallbacks(runnable);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(intent.getAction())) { //连接一个GATT服务
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(intent.getAction())) {  //从GATT服务中断开连接

                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(intent.getAction())) {  //发现有可支持的服务
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.getAction())) {   //从服务中接受数据
                    ggaBan = (GGABan) intent.getSerializableExtra(BluetoothLeService.EXTRA_DATA);
                }
            } catch (Exception e) {
            }
        }
    };
}
