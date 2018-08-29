package com.skylin.uav.drawforterrain;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import sjj.alog.Log;

/**
 * Created by Moon on 2018/4/2.
 */

public class RestartReceiver extends BroadcastReceiver {

    public static String DEFAUT_TEAM_ID = "app.com.skylinservice.broadcast.changedefaultteam";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (DEFAUT_TEAM_ID.equals(intent.getAction())) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    context.sendBroadcast(intent);
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
            Log.e("", e);
        }
    }
}
