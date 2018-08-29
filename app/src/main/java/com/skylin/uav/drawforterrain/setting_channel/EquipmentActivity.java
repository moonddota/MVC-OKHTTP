package com.skylin.uav.drawforterrain.setting_channel;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.service.BluetoothLeService;
import com.skylin.uav.drawforterrain.util.OrderUtils;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.skylin.uav.R;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.util.ArrayList;

/**
 * Created by Moon on 2018/2/23.
 */

public class EquipmentActivity extends BaseActivity implements View.OnClickListener {

    private Button chnnel_but;
    private FrameLayout lift_topbar;
    private TextView middle_topbar;
    private RecyclerView device_rv;
    private QMUIPullRefreshLayout refreshLayout;
    private CommonAdapter<BluetoothDevice> commonAdapter;
    private ArrayList<BluetoothDevice> mLeDevices = new ArrayList<>();
    private boolean mScanning;
    private BluetoothAdapter mBluetoothAdapter;
    private int search_position = -1;
    private QMUITipDialog tipDialog;
    private BluetoothDevice mdevice;
    private Button connect_but;


    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);
        BlueToochActivity.fullScreen(this);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        registerReceiver(mGattUpdateReceiver, BlueToochActivity.makeGattUpdateIntentFilter());

        connect_but = findViewById(R.id.connect_but);
        connect_but.setOnClickListener(this);

        chnnel_but = findViewById(R.id.chnnel_but);
        chnnel_but.setOnClickListener(this);

        lift_topbar = findViewById(R.id.lift_topbar);
        lift_topbar.setOnClickListener(this);
        middle_topbar = findViewById(R.id.middle_topbar);
        middle_topbar.setText(getString(R.string.EquipmentActivity_tv1));

        initAdapter();
        initPullRefreshLayout();

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(getString(R.string.EquipmentActivity_tv2))
                .create();
    }

    private void initPullRefreshLayout() {
        refreshLayout = findViewById(R.id.device_refresh);
        refreshLayout.setOnPullListener(new QMUIPullRefreshLayout.OnPullListener() {
            @Override
            public void onMoveTarget(int offset) {

            }

            @Override
            public void onMoveRefreshView(int offset) {

            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onRefresh() {
                scanLeDevice(true);
            }
        });
    }


    private void initAdapter() {
        commonAdapter = new CommonAdapter<BluetoothDevice>(this, R.layout.item_device, mLeDevices) {
            @Override
            protected void convert(ViewHolder holder, BluetoothDevice bluetoothDevice, final int position) {

                if (bluetoothDevice != null) {
                    String deviceName = bluetoothDevice.getName();
                    TextView textView = holder.getView(R.id.device_isconnect);

                    TextView name = holder.getView(R.id.device_name);
                    if (deviceName != null && deviceName.length() > 0) {
                        name.setText(deviceName);
                    } else {
                        name.setText("Unknown device");
                    }

                    CheckBox checkBox = holder.getView(R.id.device_check);
                    if (search_position == position) {
                        checkBox.setChecked(true);
                    } else {
                        checkBox.setChecked(false);
                    }
                    if (BlueToochActivity.mConnected) {
                        if (bluetoothDevice.getAddress().equals(BlueToochActivity.device.getAddress())) {
                            checkBox.setVisibility(View.GONE);
                            textView.setVisibility(View.VISIBLE);
                            textView.setText(getString(R.string.EquipmentActivity_tv3));
                            name.setTextColor(getResources().getColor(R.color.green3));
                            textView.setTextColor(getResources().getColor(R.color.green3));
                        } else {
                            name.setTextColor(getResources().getColor(R.color.black));
                            textView.setVisibility(View.GONE);
                            checkBox.setVisibility(View.VISIBLE);
                        }
                    } else {
                        name.setTextColor(getResources().getColor(R.color.black));
                        textView.setVisibility(View.GONE);
                        checkBox.setVisibility(View.VISIBLE);
                    }


                    FrameLayout frameLayout = holder.getView(R.id.device_cardview);
                    frameLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            BluetoothDevice device = mLeDevices.get(position);
                            if (device == null) {
                                return;
                            }
                            mdevice = device;
                            search_position = position;
                            connect_but.setClickable(true);
                            connect_but.setBackgroundResource(R.color.green3);
                            commonAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        };

        device_rv = findViewById(R.id.device_rv);
        device_rv.setLayoutManager(new LinearLayoutManager(this));
        device_rv.setAdapter(commonAdapter);
        device_rv.setHasFixedSize(true);
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onStart() {
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        scanLeDevice(true);
        connect_but.setText(BlueToochActivity.mConnected ? getString(R.string.EquipmentActivity_tv4) : getString(R.string.EquipmentActivity_tv5));
        connect_but.setBackgroundResource(BlueToochActivity.mConnected ? R.color.green3 : R.color.bbc2bb);
        connect_but.setClickable(BlueToochActivity.mConnected ? true : false);
        if (BlueToochActivity.mConnected) {
            mHandler.removeCallbacks(runnable);
            mHandler.postDelayed(runnable, 500);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chnnel_but:
                if (!BlueToochActivity.mConnected) {
                    ToastUtil.show(getString(R.string.toast_3));
                    return;
                }
                if (!BaseActivity.workmoder.equals("手持杖")) {
                    ToastUtil.show(getString(R.string.toast_2));
                    return;
                }
                startActivity(new Intent(getBaseContext(), SetChannelActivity.class));
                overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_still);
                break;
            case R.id.lift_topbar:
                onFinish();
                break;
            case R.id.connect_but:
                if (BlueToochActivity.mConnected) {
                    BlueToochActivity.disConnect();
                    viewsvisivleT();
                } else {

                    if (mdevice == null) {
                        ToastUtil.show(getString(R.string.toast_3));
                        return;
                    }
                    BlueToochActivity.device = mdevice;
                    if (mScanning) {
                        viewsvisivleF();
                    }

                    BlueToochActivity.select();
                    tipDialog.show();
                    checkConnect();
                }
                break;
                default:
                    break;
        }
    }

    //设备扫描回调。
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //	device  搜索到的设备
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    String s = device.getName();
//                    if (!TextUtils.isEmpty(s)) {
//                        if (s.startsWith("401") && s.length() == 15) {
                            if (!mLeDevices.contains(device)) {
                                mLeDevices.add(device);
                                commonAdapter.notifyDataSetChanged();
                            }
//                        }
//                    }
                }
            });
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                @Override
                public void run() {
                    viewsvisivleF();
                }
            }, 30000);
            viewsvisivleT();
        } else {
            viewsvisivleF();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void viewsvisivleF() {
        refreshLayout.finishRefresh();
        mScanning = false;
        mBluetoothAdapter.stopLeScan(mLeScanCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void viewsvisivleT() {

        mLeDevices.clear();
        search_position = -1;
        if (BlueToochActivity.mConnected) {
            mLeDevices.add(BlueToochActivity.device);
        }
        mScanning = true;
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case 0:
                        tipDialog.dismiss();
                        for (int i = 0; i < mLeDevices.size(); i++) {
                            if (mLeDevices.get(i).getAddress().equals(BlueToochActivity.device.getAddress())) {
                                mLeDevices.remove(i);
                                mLeDevices.add(0, BlueToochActivity.device);
                            }
                        }
                        search_position = -1;
                        commonAdapter.notifyDataSetChanged();
                        break;
                    case 1:
                        tipDialog.dismiss();
                        BlueToochActivity.disConnect();
                        commonAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {

            }
        }
    };

    private Runnable runnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void run() {
            OrderUtils.requestChannel();
            SystemClock.sleep(10);
            OrderUtils.requestSystems();
            mHandler.postDelayed(this, 500);

            chnnel_but.setText(BaseActivity.channle == -1 ? getString(R.string.EquipmentActivity_tv6) + " -" : getString(R.string.EquipmentActivity_tv6) + BaseActivity.channle);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(runnable);
    }

    private void checkConnect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long timeOut = SystemClock.uptimeMillis() + 10000;
                    while (!BlueToochActivity.mConnected) {
                        if (SystemClock.uptimeMillis() > timeOut) {
                            ToastUtil.show(getString(R.string.toast_5));
                            mHandler.sendEmptyMessage(1);
                            return;
                        }
                    }
                    Thread.sleep(1000);
                    if (BlueToochActivity.mConnected) {
                        mHandler.sendEmptyMessage(0);
                    } else {
                        mHandler.sendEmptyMessage(1);
                        ToastUtil.show(getString(R.string.toast_5));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(intent.getAction())) { //连接一个GATT服务
                connect_but.setText(getString(R.string.EquipmentActivity_tv4));
                chnnel_but.setText(getString(R.string.EquipmentActivity_tv6) + " -");
                mHandler.removeCallbacks(runnable);
                mHandler.postDelayed(runnable, 500);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(intent.getAction())) {  //从GATT服务中断开连接
                mHandler.removeCallbacks(runnable);
                connect_but.setText(getString(R.string.EquipmentActivity_tv5));
                connect_but.setBackgroundResource(R.color.bbc2bb);
                connect_but.setClickable(false);
                chnnel_but.setText(getString(R.string.EquipmentActivity_tv6) + " -");
                mLeDevices.clear();
                commonAdapter.notifyDataSetChanged();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(intent.getAction())) {  //发现有可支持的服务
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.getAction())) {   //从服务中接受数据
            }
        }
    };

    @SuppressLint("NewApi")
    private void onFinish() {
        finish();
        overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//当keycode等于退出事件值时
            onFinish();
            return super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
