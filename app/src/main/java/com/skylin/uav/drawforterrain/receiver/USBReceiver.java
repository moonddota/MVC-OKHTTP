package com.skylin.uav.drawforterrain.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import sjj.alog.Log;

/**
 * Created by wh on 2017/7/6.
 */

public class USBReceiver extends BroadcastReceiver {
    public static final String ACTION_USB_HOST = "com.usbdemo.USBReceiver#ACTION_USB_HOST";
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    context.sendBroadcast(new Intent(ACTION_USB_HOST));
//                    Toast.makeText(context, "USBReceiver USB_PERMISSION", Toast.LENGTH_SHORT).show();
                }
            } else {
//                Toast.makeText(context, "request USB_PERMISSION", Toast.LENGTH_SHORT).show();
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                UsbManager mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                mUsbManager.requestPermission(device, mPermissionIntent);
            }
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            Log.e("",e);
        }

    }
}
