package com.skylin.uav.drawforterrain;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.skylin.mavlink.MAVLinkClient;
import com.skylin.mavlink.MavLinkUavClient;
import com.skylin.mavlink.model.UAV;
import com.skylin.mavlink.model.UavAttribute;
import com.skylin.mavlink.model.UsbConnectionParameter;
import com.skylin.uav.R;
import com.skylin.uav.drawforterrain.language.MultiLanguageUtil;
import com.skylin.uav.drawforterrain.nofly.CivilAirport;
import com.skylin.uav.drawforterrain.nofly.Point;
import com.skylin.uav.drawforterrain.select.home.BlueBaseActivity;
import com.skylin.uav.drawforterrain.select.home.HomeActivity;
import com.skylin.uav.drawforterrain.service.BluetoothLeService;
import com.skylin.uav.drawforterrain.setting_channel.Crc16;
import com.skylin.uav.drawforterrain.setting_channel.GGABan;
import com.skylin.uav.drawforterrain.util.ToastUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sjj.alog.Log;

/**
 * Created by Moon on 2018/3/29.
 */

public class BaseActivity extends AppCompatActivity {
    public static final double EARTH_RADIUS = 6378137;

    public static String TOKEN;
    public static String username;
    public static int userId = -1;
    public static String teamName;
    public static int teamid ;

    public static int channle = -1;  //设备信道
    public static String workmoder = "";  //设备工作模式  基站 or 手持杖
    public static String locationModer = "";  //基站定位转台   千寻  自校准  手动


    public static List<CivilAirport> civilAirports = new ArrayList<>();
    public static List<List<Point>> second = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionsing();//添加权限
        fullScreen(this);
        APP.activities .add(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(MultiLanguageUtil.attachBaseContext(newBase));
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        APP.activities .remove(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void permissionsing() {
        if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                | ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                | ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED
                | ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                | ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            String[] mPermissionList = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CALL_PHONE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS, Manifest.permission.INTERNET,
                    Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_PRIVILEGED,
                    Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_COARSE_LOCATION};
            this.requestPermissions(mPermissionList, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == 0) {
            }
        }
    }

    //使用共享参数保存数据    sid 名    value  值
    public static void saveSid(String sid, String value) {
        SharedPreferences settings = APP.getContext().getSharedPreferences(sid, 0);
        SharedPreferences.Editor localEditor = settings.edit();
        localEditor.putString(sid, value);
        localEditor.commit();
    }

    //使用共享参数提取数据    sid  名
    public static String getSid(String sid) {
        SharedPreferences settings = APP.getContext().getSharedPreferences(sid, 0);
        String str = settings.getString(sid, "");
        if (str != null && str.length() != 0) {
            return str;
        }
        return "";
    }

    public static void fullScreen(Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕长亮
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
                Window window = activity.getWindow();
                View decorView = window.getDecorView();
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(option);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                //导航栏颜色也可以正常设置
//                window.setNavigationBarColor(Color.TRANSPARENT);
            } else {
                Window window = activity.getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                attributes.flags |= flagTranslucentStatus;
//                attributes.flags |= flagTranslucentNavigation;
                window.setAttributes(attributes);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void readVersion() {
        try {
            int length = 1;
            byte[] byt_length = {(byte) ((length >> 8) & 0xFF), (byte) (length & 0xFF)};
            byte[] crc = Crc16.matchCRC(new byte[0]);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                out.write("#".getBytes());
                out.write(byt_length);
                out.write("G".getBytes());
                out.write(crc);
                out.write("@".getBytes());
            } catch (IOException ignored) {
                ToastUtil.show("错误");
            }
            BlueToochActivity.writeBlue(out.toByteArray());
            BlueBaseActivity.writeBlue(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }


    public static boolean isScreenOriatationPortrait(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        return height>width;
    }


    private MAVLinkClient client = new MAVLinkClient();
    private MavLinkUavClient uavClient  = client.getUavClient(new UsbConnectionParameter(57600), this);

    public  final String ACTION_USB_PERMISSION = "ACTION_USB_PERMISSION";
    public  void connectUav(){
        connect();
        uavClient.getUav().addAttributeChangeListener(listener);
    }


    public  void disConnectUav(){
        uavClient.disconnect();
        uavClient.getUav().removeAttributeChangeListener(listener);
    }

    private  void connect() {
        if (hasUsbDevicePermission()) {
            try {
                uavClient.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private  boolean hasUsbDevicePermission() {
        UsbManager manager = (UsbManager) APP.getContext().getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        if (deviceList.size() == 0) {
            notFoundUsb();
            return false;
        }
        BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                            ToastUtil.show(APP.getContext().getString(R.string.toast_28));
                            connect();
                        } else {
                            disConnectUav();
                            if (HomeActivity.statrc_list.INSTANCE.getHomeBan().getMode() ==2) {
                                sendBroadcast(new Intent(BluetoothLeService.ACTION_GATT_DISCONNECTED));
                            }
//                            ToastUtil.show(APP.getContext().getString(R.string.toast_27));
                        }
                    }
                }
            }
        };
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);
        boolean hasPermission = true;
        for (UsbDevice usbDevice : deviceList.values()) {
            if (!manager.hasPermission(usbDevice)) {
                hasPermission = false;
                manager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
        return hasPermission;
    }

    private  void notFoundUsb() {
        try {
            new QMUIDialog.MessageDialogBuilder(this)
                    .setTitle(APP.getContext().getString(R.string.continuePopu_tv1))
                    .setMessage(APP.getContext().getString(R.string.toast_29))
                    .addAction(APP.getContext().getString(R.string.continuePopu_tv12), new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                        }
                    })
                    .addAction(APP.getContext().getString(R.string.continuePopu_tv16), new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            connectUav();
                        }
                    })
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  UAV.AttributeChangeListener listener = new UAV.AttributeChangeListener() {
        @Override
        public void onChange(UavAttribute attribute, Object o) {

            switch (attribute) {
                case position:
                    com.skylin.mavlink.model.Point point = (com.skylin.mavlink.model.Point) o;

                    GGABan ggaBan = new GGABan();
                    ggaBan.setLat(point.getLatitude());
                    ggaBan.setLon(point.getLongitude());

                    Intent intent = new Intent(BluetoothLeService.ACTION_DATA_AVAILABLE);

                    intent.putExtra(BluetoothLeService.EXTRA_DATA, ggaBan);
                    sendBroadcast(intent);

                    break;
                case connectionState_disconnected:
//                    ToastUtil.show(APP.getContext().getString(R.string.toast_27));
                    if (HomeActivity.statrc_list.INSTANCE.getHomeBan().getMode() ==2) {

                        sendBroadcast(new Intent(BluetoothLeService.ACTION_GATT_DISCONNECTED));}
                    break;
                case connectionState_connected:
//                    ToastUtil.show(APP.getContext().getString(R.string.toast_28));
                    if (HomeActivity.statrc_list.INSTANCE.getHomeBan().getMode() ==2) {

                        sendBroadcast(new Intent(BluetoothLeService.ACTION_GATT_CONNECTED));
                    }
                    break;
                default:
                    break;
            }
        }
    };


}
