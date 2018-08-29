package com.skylin.uav.drawforterrain.select;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skylin.uav.R;
import com.skylin.uav.drawforterrain.setting_channel.OnClick;

/**
 * Created by Moon on 2018/3/15.
 */

public class SelectDialog extends Dialog {


    public SelectDialog(@NonNull Context context) {
        super(context, R.style.MDialog);


        LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.dialog_select, null);


       TextView button1=root.findViewById(R.id.sdialog_tv1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click != null) {
                    click.onPopuClick(v, v.getId());
                }
            }
        });
        TextView button2=root.findViewById(R.id.sdialog_tv2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click != null) {
                    click.onPopuClick(v, v.getId());
                }
            }
        });

        TextView button3=root.findViewById(R.id.sdialog_tv3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click != null) {
                    click.onPopuClick(v, v.getId());
                }
            }
        });

        TextView button4=root.findViewById(R.id.sdialog_tv4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click != null) {
                    click.onPopuClick(v, v.getId());
                }
            }
        });


        setContentView(root);
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        dialogWindow.setWindowAnimations(R.style.dialogstyle); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
//        lp.x = 0; // 新位置X坐标
//        lp.y = 0; // 新位置Y坐标
        lp.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.52); // 宽度
        lp.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.37); // 宽度
//      lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//      lp.height = WindowManager.LayoutParams.MATCH_PARENT; // 高度
//        root.measure(0, 0);
//        lp.width = root.getMeasuredWidth();
//        lp.height = root.getMeasuredHeight();
        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
    }

    private OnClick click;

    public void setPopuCloseListener(OnClick closeListener) {
        this.click = closeListener;
    }

}
