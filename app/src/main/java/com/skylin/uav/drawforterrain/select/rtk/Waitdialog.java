package com.skylin.uav.drawforterrain.select.rtk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skylin.uav.R;
import com.skylin.uav.drawforterrain.BaseActivity;


/**
 * Created by Moon on 2018/3/15.
 */

public class Waitdialog extends Dialog {

    private int time;
    private TextView tv_time;

    public Waitdialog(@NonNull Activity context
            , int themeResId
            , int time
            , String massage
            , double width
            , double height) {
        super(context, themeResId);


        LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.dialog_wait, null);

        this.time = time;

        tv_time = root.findViewById(R.id.waitdialog_time);
        tv_time.setText(time / 1000 + "");

        TextView msg = root.findViewById(R.id.waitdialog_mag);
        msg.setText(massage);


        setContentView(root);
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setWindowAnimations(R.style.dialogstyle); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
//        lp.x = 0; // 新位置X坐标
//        lp.y = 0; // 新位置Y坐标
      if (BaseActivity.isScreenOriatationPortrait(context)){
          lp.width = (int) (context.getResources().getDisplayMetrics().widthPixels /1.5); // 宽度
          lp.height = (int) (context.getResources().getDisplayMetrics().heightPixels /3); // 宽度
      }else {
          lp.width = (int) (context.getResources().getDisplayMetrics().widthPixels /2.7); // 宽度
          lp.height = (int) (context.getResources().getDisplayMetrics().heightPixels /1.7); // 宽度
      }

//      lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
//      lp.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度
//        root.measure(0, 0);
//        lp.width = root.getMeasuredWidth();
//        lp.height = root.getMeasuredHeight();
        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
    }

//    private  void show(){
//        dialogWindow.sh
//    }


    @Override
    public void show() {
        super.show();
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 1000);
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            time = time - 1000;
            if (time == 0){
                handler.removeCallbacks(this);
                dismiss();
            }
            if (time % 1000 == 0){
                tv_time.setText(time/1000 +"");
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    public void dismiss() {
        super.dismiss();
        handler.removeCallbacks(runnable);
    }

}
