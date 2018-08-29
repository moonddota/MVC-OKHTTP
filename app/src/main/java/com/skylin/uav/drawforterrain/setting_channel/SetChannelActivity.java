package com.skylin.uav.drawforterrain.setting_channel;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.service.BluetoothLeService;
import com.skylin.uav.drawforterrain.util.OrderUtils;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.skylin.uav.R;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;

import sjj.alog.Log;

/**
 * Created by Moon on 2018/2/24.
 */

public class SetChannelActivity extends BaseActivity {

    private TextView number_dq;
    private RecyclerView setting_rc;
    private CommonAdapter<Integer> channelAdapter;
    private int index = -1;
    private QMUITipDialog tipDialog;
    private Button rorw_back;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rorw);
        registerReceiver(mGattUpdateReceiver, BlueToochActivity.makeGattUpdateIntentFilter());
        initRecycle();
        number_dq = (TextView) findViewById(R.id.number_dq);

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.SetChannelActivity_tv3))
                .create();
        OrderUtils.requestChannel();
        handler.postDelayed(runnable, 1000);

        rorw_back=findViewById(R.id.rorw_back);
        rorw_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
//                overridePendingTransition(R.anim.slide_still, R.anim.slide_out_to_bottom);
            }
        });
    }

    private void initRecycle() {
        ArrayList<Integer> channel_list = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            channel_list.add(i);
        }

        channelAdapter = new CommonAdapter<Integer>(this, R.layout.item_channel, channel_list) {
            @Override
            protected void convert(ViewHolder holder, Integer integer, int position) {
                holder.setText(R.id.tv_channnel_item,""+position);
                LinearLayout linearLayout = holder.getView(R.id.item_channnel_layout);
                if (index == position) {
                    linearLayout.setBackgroundResource(R.drawable.editsharp9);
                    holder.setTextColor(R.id.tv_channnel_item,getResources().getColor(R.color.white));
                } else {
                    linearLayout.setBackgroundResource(R.drawable.editsharp8);
                    holder.setTextColor(R.id.tv_channnel_item,getResources().getColor(R.color.text_gray3));
                }
            }
        };

        setting_rc = findViewById(R.id.setting_rc);
        setting_rc.setLayoutManager(new GridLayoutManager(this, 4));
        setting_rc.setAdapter(channelAdapter);
        setting_rc.setHasFixedSize(true);

        channelAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (index == position) {
                    return;
                } else {
                    OrderUtils.setChnnel((position + "").getBytes());
                    checkChannel(position);
                    tipDialog.show();
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });
    }

    private void checkChannel(final int p) {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void run() {
                long timeOut = SystemClock.uptimeMillis() + 20000;
                while (p != index) {
                    if (SystemClock.uptimeMillis() > timeOut) {
                        handler.sendEmptyMessage(4);
                        ToastUtil.show(getString(R.string.toast_6));
                        return;
                    }
                }
                if (p == index) {
                    ToastUtil.show(getString(R.string.toast_7));
                } else if (p != index) {
                    ToastUtil.show(getString(R.string.toast_6));
                }
                handler.sendEmptyMessage(4);
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 4:
                    tipDialog.dismiss();
                    break;
            }
        }
    };

    private Runnable runnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            OrderUtils.requestChannel();
            handler.postDelayed(this, 1000);
            number_dq.setText(BaseActivity.channle == -1? getString(R.string.SetChannelActivity_tv1)+"-":getString(R.string.SetChannelActivity_tv1)+" "+BaseActivity.channle);
            index = BaseActivity.channle;
            channelAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//当keycode等于退出事件值时
            finish();
//            overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
        handler.removeCallbacks(runnable);
    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(intent.getAction())) { //连接一个GATT服务
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 1000);

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(intent.getAction())) {  //从GATT服务中断开连接
                index = -1;
                channelAdapter.notifyDataSetChanged();
                handler.removeCallbacks(runnable);
                if (tipDialog.isShowing()) tipDialog.dismiss();
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.getAction())) {   //从服务中接受数据
            }
        }
    };

}
