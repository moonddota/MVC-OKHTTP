package com.skylin.uav.drawforterrain.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XmlResourceParser;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.SparseArray;

import com.skylin.uav.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by sjj on 2017/10/27.
 */

public class USBPermissionUtil {
    private static final String ACTION_USB_PERMISSION = "USBPermissionUtil.ACTION_USB_PERMISSION";
    private USBPermissionUtil() {
    }
    public static Observable<Boolean> request(final Context context) {
        return request(context, initDeviceFilter(context));
    }
    /**
     * @param array vendor-id to product-id,product-id
     * @return
     */
    public static Observable<Boolean> request(final Context context, final SparseArray<Set<Integer>> array) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final ObservableEmitter<Boolean> e) throws Exception {
                UsbDevice device = findDevice(context, array);
                if (device == null) throw new Exception("设备未找到");
                UsbManager manager = getManager(context);
                if (!manager.hasPermission(device)) {
                    PendingIntent intent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                                context.unregisterReceiver(this);
                                e.onNext(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false));
                                e.onComplete();
                            }
                        }
                    };
                    context.registerReceiver(mUsbReceiver, filter);
                    manager.requestPermission(device,intent);
                } else {
                    e.onNext(true);
                    e.onComplete();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    private static UsbDevice findDevice(Context context, SparseArray<Set<Integer>> array) {
        UsbDevice usbDevice = null;
        HashMap<String, UsbDevice> deviceList = getManager(context).getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            Set<Integer> set = array.get(device.getVendorId());

            if (set != null && set.contains(device.getProductId())) {
                usbDevice = device;
                break;
            }
        }
        return usbDevice;
    }

    private static UsbManager getManager(Context context) {
        return (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }
    private static SparseArray<Set<Integer>> initDeviceFilter(Context context) {
        XmlResourceParser xml = context.getResources().getXml(R.xml.device_filter);
        try {
            SparseArray<Set<Integer>> strings = new SparseArray<>();
            int event = xml.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if ("usb-device".equals(xml.getName())) {
                            int attributeIntValue = xml.getAttributeIntValue(null, "vendor-id", -1);
                            if (attributeIntValue != -1) {
                                Set<Integer> set = strings.get(attributeIntValue);
                                if (set == null) {
                                    strings.put(attributeIntValue, set = new HashSet<>());
                                }
                                set.add(xml.getAttributeIntValue(null, "product-id", -1));
                            }
                        }
                        break;
                        default:break;
                }
                event = xml.next();
            }
            xml.close();
            return strings;
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
