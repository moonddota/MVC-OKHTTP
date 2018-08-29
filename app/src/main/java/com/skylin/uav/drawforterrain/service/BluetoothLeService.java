/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skylin.uav.drawforterrain.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.SampleGattAttributes;
import com.skylin.uav.drawforterrain.checksupdata.JsonGenericsSerializator;
import com.skylin.uav.drawforterrain.select.home.HomeActivity;
import com.skylin.uav.drawforterrain.setting_channel.GGABan;
import com.skylin.uav.drawforterrain.setting_channel.ListBan;
import com.skylin.uav.drawforterrain.util.AnalysisGGAUtil;
import com.skylin.uav.drawforterrain.util.Int2Byte;
import com.skylin.uav.drawforterrain.util.LogUtils;
import com.skylin.uav.drawforterrain.util.ToastUtil;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;

import static com.blankj.utilcode.util.AppUtils.launchApp;

/**
 * TEXTBluetoothService for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
@SuppressLint("NewApi")
public class BluetoothLeService extends Service {
    private boolean is_test = false;  //是否模拟数据测试
    private byte[] bDatas = new byte[1024];
    private int bNo = 0;

    private final static String TAG = BluetoothLeService.class.getSimpleName();
    public static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    public static int mConnectionState = STATE_DISCONNECTED;

    public static final String AACTION_VERSIONCODE = "aaction_versioncode";

    public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    // 蓝牙的数据读取和状态改变都会回调这个函数。
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        //当连接上设备或者失去连接时会回调该函数
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) { //连接成功
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                LogUtils.e("保持连接服务：GATT协议.");
                // 连接成功后试图发现服务。
                LogUtils.e("尝试开始发现服务" + mBluetoothGatt.discoverServices());
                gatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {  //连接失败
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                LogUtils.e("关闭服务：GATT协议.");
                broadcastUpdate(intentAction);

                SystemClock.sleep(1000);
                close();
            }
        }

        //当设备是否找到服务时，会回调该函数
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) { // status == 0  GATT协议操作完成成功 其他则失败
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //在这里可以对服务进行解析，寻找到你需要的服务
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                LogUtils.e("onServicesDiscovered received: " + status);
//                ToastUtil.show("服务发现失败：  " + status);
            }
        }

        //当读取设备时会回调该函数
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) { // status == 0  GATT协议操作完成成功 其他则失败
            //读取到值，在这里读数据
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //读取到的数据存在characteristic当中，可以通过characteristic.getValue();函数取出。然后再进行解析操作。
                LogUtils.e(" characteristic.getValue();" + characteristic.getValue());

                //int charaProp = characteristic.getProperties();if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)表示可发出通知。  判断该Characteristic属性
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override //当向设备Descriptor中写数据时，会回调该函数
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.e("debug", "发出onDescriptorWriteonDescriptorWrite = " + status + ", descriptor =" + descriptor.getUuid().toString());
//            UUID uuid = descriptor.getCharacteristic().getUuid();
//            if (uuid.equals(UUID.fromString("0000cd01-0000-1000-8000-00805f9b34fb"))) {
//                broadcastUpdate(ACTION_CD01NOTIDIED);
//            } else if (uuid.equals(UUID.fromString("0000cd02-0000-1000-8000-00805f9b34fb"))) {
//                broadcastUpdate(ACTION_CD02NOTIDIED);
//            } else if (uuid.equals(UUID.fromString("0000cd03-0000-1000-8000-00805f9b34fb"))) {
//                broadcastUpdate(ACTION_CD03NOTIDIED);
//            } else if (uuid.equals(UUID.fromString("0000cd04-0000-1000-8000-00805f9b34fb"))) {
//                broadcastUpdate(ACTION_CD04NOTIDIED);
//            }
        }

        //当连接成功将回调该方法     传递参数
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//            LogUtils.e("接收gatt = " + gatt + ", characteristic =" + characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            // 动态获取 rssi 值
        }

        @Override //当向Characteristic写数据时会回调该函数
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            LogUtils.e("--------write success----- status:" + status);
        }

    };


    private void broadcastUpdate(final String action) {
        if (APP.is_Front) {
            final Intent intent = new Intent(action);
            sendBroadcast(intent);
        }
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                LogUtils.e("Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                LogUtils.e("Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            LogUtils.e(String.format("Received heart rate: %d", heartRate));
//            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX. 对于所有的文件，写入十六进制格式的文件
            //这里读取到数据
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data) {
//                    //以十六进制的形式输出
//                    stringBuilder.append(String.format("%02X ", byteChar));
//                }
                String s = new String(data);
//                sjj.alog.Log.e("sss  "+s);
                if (HomeActivity.statrc_list.INSTANCE.getHomeBan().getMode() == 1) {

//                LogUtils.e(datas+"");
                    try {
                        for (int i = 0; i < data.length; i++, bNo++) {
                            if (data[i] == 36) {
                                bDatas = new byte[1024];
                                bNo = 0;
                                bDatas[bNo] = data[i];
                            } else if (data[i] == 42) {
                                bDatas[bNo] = data[i];
                                HashMap<String, String> hashMap = AnalysisGGAUtil.analysisGGA(new String(Arrays.copyOf(bDatas, bNo)));
                                Intent intent11 = new Intent(action);
                                if (APP.is_Front) {
                                    if (hashMap != null) {
//                                        sjj.alog.Log.e(hashMap.toString() + "");
                                        ggaBan.setDatas(hashMap);
                                    } else {
//                                        sjj.alog.Log.e("null  ndasd");
                                        ggaBan.setEmpty();
                                    }
                                    intent11.putExtra(EXTRA_DATA,ggaBan );
                                    sendBroadcast(intent11);
                                    sendBroadcast(intent11);
                                }
                            } else {
                                bDatas[bNo] = data[i];
                            }
                        }
                    } catch (Exception e) {
                        bNo = 0;
                        bDatas = new byte[1024];
//                        LogUtils.e("error" + e);
                    }
                } else {
                    try {
                        for (int i = 0; i < data.length; i++, bNo++) {
                            if (data[i] == 35) {
                                bDatas = new byte[1024];
                                bNo = 0;
                                bDatas[bNo] = data[i];
                            } else if (data[i] == 64) {
                                bDatas[bNo] = data[i];
                                judgePattern(bDatas, bNo, action);
                            } else {
                                bDatas[bNo] = data[i];
                            }
                        }
                    } catch (Exception e) {
//                    sjj.alog.Log.e("", e);
                        bNo = 0;
                        bDatas = new byte[1024];
                    }
                }
//                if (APP.is_Front) {
//                intent.putExtra(EXTRA_DATA, data);   //新设备
//                sendBroadcast(intent);
//                }

            }
        }
    }

    private GGABan ggaBan = new GGABan();

    private void judgePattern(byte[] bytes, int lengh, String action) {
        byte[] datas = Arrays.copyOf(bytes, lengh + 1);
//        sjj.alog.Log.e(Arrays.toString(datas)  +"\n"+new String(datas));
        if (datas[0] == 35 && datas[datas.length - 1] == 64) {
            byte[] byt321 = Arrays.copyOfRange(datas, 1, 5);
            byte[] byte322 = Arrays.copyOfRange(datas, 5, 9);
            int int321 = Int2Byte.byteArrayToInt(byt321);
            int int322 = Int2Byte.byteArrayToInt(byte322);

            int w1 = int321 & 0x3;
            int w2 = int321 >> 2;

            byte[] buf = Arrays.copyOfRange(datas, 13, datas.length - 1);
            byte[] bytes1 = Int2Byte.translateDatas(buf);
            String s = new String(bytes1, 0, bytes1.length);
//            sjj.alog.Log.e(w1+"   "+w2);
            if (is_test) {
                /*
                 * 测试专用方法
                 * */
                byte[] cs_byt = Arrays.copyOfRange(datas, 22, datas.length);
//            Log.e(Arrays.toString(cs_byt));
                if (cs_byt[0] == 36 && cs_byt[cs_byt.length - 1] == 64) {
                    getGGan(cs_byt, action);
                }
            } else {
                /*
                 * 正式方法
                 * */
                if (s.contains("\":{\"ModuleName\"")) {
                    String[] qwe = s.split(":");
                    String[] qwe1 = qwe[4].split(",");
                    String[] qwe2 = qwe1[0].split("\"");
                    String[] qwe3 = qwe2[1].split("\\.");
                    int x = Integer.parseInt(qwe3[0]);
                    int y = Integer.parseInt(qwe3[1]);
                    int z = Integer.parseInt(qwe3[2]);
                    getVList(x, y, z);
                    return;
                }


                if (w1 == 0) {    //请求模式
                    switch (w2) {
                        case 1:     //id
                            break;
                        case 2:      //gga
                            if (int322 == s.length()) {
                                HashMap<String, String> HashMap = AnalysisGGAUtil.analysisGGA(s);
                                if (HashMap == null) {
//                                    sjj.alog.Log.e("HashMap    null");
                                    ggaBan.setGGAEmpty();
                                } else {
//                                    sjj.alog.Log.e("HashMap    " + HashMap.toString());
                                    ggaBan.setDatas(HashMap);
                                }

                                Intent intent = new Intent(action);
                                intent.putExtra(EXTRA_DATA, ggaBan);
                                if (APP.is_Front)
                                    sendBroadcast(intent);
                            }
                            break;
                        case 4:    //信道
                            if (int322 == s.length() && s.length() == 1) {
//                                ggaBan.setChannel(bytes1[0]);
                                BaseActivity.channle = bytes1[0];
                            }
                            break;
                        case 8:    // 系统状态
                            int b = bytes1[0] & 0xFF |
                                    (bytes1[1] & 0xFF) << 8 |
                                    (bytes1[2] & 0xFF) << 16 |
                                    (bytes1[3] & 0xFF) << 24;

                            int q = b & 0x3;          //工作模式
                            int w = (b >> 2) & 0x3;   //工作状态
                            int e = (b >> 4) & 0x63;   //信道
                            int r = (b >> 10) & 0x3;   //基站的校准模式 0：自校准 1：手动设置 2：千寻校准  其他预留 eBaseStationCalibrationMode

//                            sjj.alog.Log.e("r="+r+" b= " + b + " ,q = " + q + " ,w= " + w + " ,e=" + e);
                            switch (q) {
                                case 0:     //手持杖
//                                    ggaBan.setWorkmoder("手持杖");
                                    BaseActivity.workmoder = "手持杖";
                                    break;
                                case 1:    //基站
//                                    ggaBan.setWorkmoder("基站");
                                    BaseActivity.workmoder = "基站";
                                    break;
                                case 2:       //切换中
//                                    ggaBan.setWorkmoder("切换中");
                                    BaseActivity.workmoder = "切换中";
                                    break;
                                case 3:        //切换中
//                                    ggaBan.setWorkmoder("无状态");
                                    BaseActivity.workmoder = "无状态";
                                    break;
                                default:
                                    break;
                            }
                            switch (w) {
                                case 0:      //已经正常工作
//                                    ggaBan.setWorkstate("正常");
                                    break;
                                case 1:        //初始化中
//                                    ggaBan.setWorkstate("初始化中");
                                    break;
                                case 2:     //无
//                                    ggaBan.setWorkstate("无");
                                    break;

                                case 3:    //无
//                                    ggaBan.setWorkstate("无");
                                    break;
                                default:
                                    break;
                            }
                            switch (r) {
                                default:
                                    break;
                                case 0:    //自校准
//                                sjj.alog.Log.e("自校准");
                                    BaseActivity.locationModer = "自校准";
                                    break;
                                case 1:    //手动设置
//                                sjj.alog.Log.e("手动设置");
                                    BaseActivity.locationModer = "手动设置";
                                    break;
                                case 2:    //千寻校准
//                                sjj.alog.Log.e("千寻校准");
                                    BaseActivity.locationModer = "千寻校准";
                                    break;
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    /*
     * 测试专用方法
     * */
    private void getGGan(byte[] bytes1, String action) {
        String gga_s = new String(bytes1, 0, bytes1.length);
        HashMap<String, String> HashMap = AnalysisGGAUtil.analysisGGA(gga_s);
        if (HashMap == null) {
//            sjj.alog.Log.e("HashMap    null");
            ggaBan.setGGAEmpty();
        } else {
            ggaBan.setDatas(HashMap);
        }
        Intent intent = new Intent(action);
        intent.putExtra(EXTRA_DATA, ggaBan);
        if (APP.is_Front)
            sendBroadcast(intent);
    }


    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the TEXTBluetoothService.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                LogUtils.e("Unable to initialize BluetoothManager.");
//                ToastUtil.show("无法初始化BluetoothManager");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogUtils.e("Unable to obtain a BluetoothAdapter.");
//            ToastUtil.show("无法获得BluetoothAdapter。");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            LogUtils.e("BluetoothAdapter没有初始化或未指明的地址。");
//            ToastUtil.show("请重启应用或重启一次蓝牙");
            return false;
        }
        // Previously connected device.  Try to reconnect.(先前连接的设备。 尝试重新连接)
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            LogUtils.e("尝试使用一个现有mBluetoothGatt连接");
//            ToastUtil.show("尝试使用一个现有mBluetoothGatt连接");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
//            ToastUtil.show("设备没有找到。无法连接");
            LogUtils.e("设备没有找到。无法连接\"");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        LogUtils.e("尝试创建一个新的连接.");
//        ToastUtil.show("尝试创建一个新的连接");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            if (mBluetoothAdapter == null) {
//                ToastUtil.show("BluetoothAdapter没有初始化");
            } else if (mBluetoothGatt == null) {
//                ToastUtil.show("mBluetoothGatt没有初始化");
            }
//            LogUtils.e("BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    //读取蓝牙中数据。
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtils.e("BluetoothAdapter not initialized");
//            ToastUtil.show("蓝牙没有初始化");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    //向蓝牙中写入数据。
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            LogUtils.e("BluetoothAdapter not initialized");
//            ToastUtil.show("蓝牙没有初始化");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }


    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        Log.e(TAG, "setCharacteristicNotification: ");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }


        //000
        try {
            if (enabled) {
                Log.i(TAG, "Enable Notification");
                mBluetoothGatt.setCharacteristicNotification(characteristic, true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            } else {
                Log.i(TAG, "Disable Notification");
                mBluetoothGatt.setCharacteristicNotification(characteristic, false);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        } catch (Exception e) {
            BlueToochActivity.disConnect();
        }

        //  22

//        mBluetoothGatt.readCharacteristic(characteristic);
//        if (characteristic.getDescriptors().size() != 0) {
//            BluetoothGattDescriptor localBluetoothGattDescriptor = (BluetoothGattDescriptor) characteristic.getDescriptors().get(0);
//            localBluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(localBluetoothGattDescriptor);
//        }
//
//        int i = characteristic.getProperties();
//        if ((i & 0x8) > 0)
//            mBluetoothGatt.setCharacteristicNotification(characteristic, false);
//        if ((i & 0x4) > 0)
//            mBluetoothGatt.setCharacteristicNotification(characteristic, false);
//        if ((i & 0x2) > 0)
//            mBluetoothGatt.setCharacteristicNotification(characteristic, false);
//        if ((i & 0x10) > 0)
//            mBluetoothGatt.setCharacteristicNotification(characteristic, true);


        //  11
//
//        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
//
//        // This is specific to Heart Rate Measurement.
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
    }

    //    设备连接成功并回调BluetoothGattCallback接口里面的onConnectionStateChange函数，
//    然后调用mBluetoothGatt.discoverServices();去发现服务。
//    发现服务后会回调BluetoothGattCallback接口里面的 onServicesDiscovered函数，在里面我们可以获取服务列表。
    public List<BluetoothGattService> getSupportedGattServices(UUID uuid) {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices(); //此处返回获取到的服务列表
    }

    public void getVList(final int x, final int y, final int z) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = APP.url + "/work2.0/Public/firmware/?service=Firmware.getFirmwareVersionList&firmwareId=" + "basestation"; //  获取固件版本列表
                try {
                    OkHttpUtils
                            .post()//
                            .url(url)//
                            .build()//
                            .execute(new GenericsCallback<ListBan>(new JsonGenericsSerializator()) {
                                @Override
                                public void onError(Call call, Exception e, int id) {
                                    sjj.alog.Log.e("call " + call.toString() + " e " + e.toString() + " id " + id, e);
                                }

                                @Override
                                public void onResponse(ListBan response, int id) {
                                    android.util.Log.e("tag", response.toString());
                                    if (response.getRet() == 200) {
                                        for (int i = 0; i < response.getData().size(); i++) {
                                            for (int j = 0; j < response.getData().get(i).size(); j++) {
                                                if (response.getData().get(i).get(j).isRelease()) {
                                                    String versionCode = response.getData().get(i).get(j).getVersionCode();
                                                    String[] split = versionCode.split("\\.");
                                                    int x_down = Integer.parseInt(split[0]);
                                                    int y_down = Integer.parseInt(split[1]);
                                                    int z_down = Integer.parseInt(split[2]);

                                                    final Intent intent = new Intent(AACTION_VERSIONCODE);
                                                    if (x_down > x) {

                                                        sendBroadcast(intent);
                                                        ToastUtil.showLong("您的设备不是最新版本，请去固件中心升级");
                                                    } else if (x_down == x) {
                                                        if (y_down > y) {
                                                            sendBroadcast(intent);
                                                            ToastUtil.showLong("您的设备不是最新版本，请去固件中心升级");
                                                        } else if (y_down == y) {
                                                            if (z_down > z) {
                                                                sendBroadcast(intent);
                                                                ToastUtil.showLong("您的设备不是最新版本，请去固件中心升级");
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                    sjj.alog.Log.e("", e);
                }
            }
        }).start();
    }
}
