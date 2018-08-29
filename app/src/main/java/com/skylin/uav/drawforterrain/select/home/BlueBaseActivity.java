package com.skylin.uav.drawforterrain.select.home;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.skylin.mavlink.MAVLinkClient;
import com.skylin.mavlink.MavLinkUavClient;
import com.skylin.mavlink.model.UAV;
import com.skylin.mavlink.model.UavAttribute;
import com.skylin.mavlink.model.UsbConnectionParameter;
import com.skylin.uav.R;
import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.drawforterrain.select.gps_uav.GpsUavActivity;
import com.skylin.uav.drawforterrain.service.BluetoothLeService;
import com.skylin.uav.drawforterrain.setting_channel.GGABan;
import com.skylin.uav.drawforterrain.setting_channel.ObsMasterBan;
import com.skylin.uav.drawforterrain.util.LogUtils;
import com.skylin.uav.drawforterrain.util.ToastUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import task.model.Pair;


/**
 * Created by moon on 2017/6/8.
 */

public class BlueBaseActivity extends BaseActivity {


    private boolean is_bound = false;   //是否已经启动服务
    public static BluetoothDevice device;
    public static BluetoothLeService mBluetoothLeService;
    public static boolean mConnected = false;   //是否连接
    //蓝牙Aaapter
    private BluetoothManager bluetoothManager;
    public static BluetoothAdapter mBluetoothAdapter;
    //    private static ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    public static BluetoothGattCharacteristic mCharacteristic;
    // 管理service的生命周期
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                LogUtils.e("Unable to initialize Bluetooth");
                ToastUtil.show(getString(R.string.BlueBase_tv1));
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initBLUETOOTH();//蓝牙初始化
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        if (!is_bound) {
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            is_bound = true;
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initBLUETOOTH() {
        /***
         * 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
         *如果mBluetoothAdapter == null，说明设备不支持蓝牙
         */
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        //  BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //  弹出是否启用蓝牙的对话框
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        //        判断是否支持蓝牙ble
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            ToastUtil.show(getString(R.string.BlueBase_tv2));
        }

    }

    private boolean is_register = true;

    @Override
    public void onResume() {
        super.onResume();
        if (is_register) {
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            is_register = false;
        }
//        if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(device.getAddress());
//        }
    }

    public static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);   // GATT状态：连接
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);// GATT状态：断连
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);//  查找GATT服务
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);// 从服务中接受数据
        intentFilter.addAction(BluetoothLeService.AACTION_VERSIONCODE);// 读取固件版本
        return intentFilter;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }


    public static void connectBlueFour() {
        if (!mConnected) {
            if (mBluetoothLeService == null | device == null) {
                return;
            }
            mBluetoothLeService.connect(device.getAddress());
        }
    }

    public static void disConnectBlueFour() {
        mBluetoothLeService.disconnect();
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(intent.getAction())) { //连接一个GATT服务
                if (APP.is_Front) {
                    mConnected = true;
                }
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(intent.getAction())) {  //从GATT服务中断开连接
                mConnected = false;
                ToastUtil.show(getString(R.string.hand_mode8));
                mCharacteristic = null;
                channle = -1;  //设备信道
                workmoder = "";  //设备工作模式
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(intent.getAction())) {  //发现有可支持的服务
                displayGattServices(mBluetoothLeService.getSupportedGattServices(UUID.fromString("00001111-0000-1000-8000-00805f9b34fb")));
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(intent.getAction())) {   //从服务中接受数据
            }
        }
    };

    //    获取到服务列表后自然就是要对服务进行解析。解析出有哪些服务，服务里有哪些Characteristic，哪些Characteristic可读可写可发通知等等。
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void displayGattServices(final List<BluetoothGattService> gattServices) {
        if (gattServices == null) {
            return;
        }

        for (int i = 0; i < gattServices.size(); i++) {
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();
            List<BluetoothGattCharacteristic> characteristics = gattServices.get(i).getCharacteristics();
            for (int j = 0; j < characteristics.size(); j++) {
                charas.add(characteristics.get(j));
                String C_uuid = String.valueOf(characteristics.get(j).getUuid());
                if (C_uuid.startsWith("00001111")  | C_uuid.startsWith("0000fff6")) {
                    mCharacteristic = characteristics.get(j);
                    mBluetoothLeService.setCharacteristicNotification(mCharacteristic, true);
                    return;
                }
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static void writeBlue(byte[] bytes) {
        if (!mConnected) {
            return;
        }
        if (BlueBaseActivity.mCharacteristic == null) {
            return;
        }
        try {
            int charaProp = mCharacteristic.getProperties();
            if (charaProp != 0 && ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)) {
//                byte[] bytes = s.getBytes();
                if (bytes.length > 20) {
                    for (int k = 0; k < bytes.length; k += 20) {
                        int i1 = k + 20;
                        byte[] value = Arrays.copyOfRange(bytes, k, i1 <= bytes.length ? i1 : bytes.length);
                        mCharacteristic.setValue(value);
                        BlueBaseActivity.mBluetoothLeService.writeCharacteristic(mCharacteristic);
                        Thread.sleep(10);
                    }
                } else {
                    mCharacteristic.setValue(bytes);
                    BlueBaseActivity.mBluetoothLeService.writeCharacteristic(mCharacteristic);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}