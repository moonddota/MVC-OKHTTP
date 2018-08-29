   package com.skylin.uav.drawforterrain.util;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by mooon on 2017/1/11.
 */

public class ToastUtil {
    private static Toast toast,toastlong;
    private static Handler handler;
    public static void init(Context context){
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        toastlong = Toast.makeText(context, "", Toast.LENGTH_LONG);
        handler = new Handler();
    }
    public static void show(final String string){
        handler.post(new Runnable() {
            @Override
            public void run() {
                toast.setText(string);
                toast.show();
            }
        });
    }

    public static void showLong(final String string){
        handler.post(new Runnable() {
            @Override
            public void run() {
                toastlong.setText(string);
                toastlong.show();
            }
        });
    }
}
