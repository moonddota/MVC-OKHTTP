package com.skylin.uav.drawforterrain.dialog;

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

import com.skylin.uav.drawforterrain.setting_channel.OnClick;
import com.skylin.uav.R;

/**
 * Created by Moon on 2018/3/15.
 */

public class Basedialog extends Dialog {


    public Basedialog(@NonNull Context context
            , int themeResId
            , String title
            , String massage
            , String but1
            , String but2) {
        super(context, themeResId);


        LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.view_dialog_single_operation, null);
        TextView tit = root.findViewById(R.id.tv_single_operation_dialog_title);
        tit.setText(title);
        TextView msg = root.findViewById(R.id.tv_single_operation_dialog_message);
        msg.setText(massage);

       TextView button1=root.findViewById(R.id.tv_single_operation_dialog_confirm);
        button1.setText(but1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click != null) {
                    click.onPopuClick(v, v.getId());
                }
            }
        });
        TextView button2=root.findViewById(R.id.tv_single_cancel);
        button2.setText(but2);
        button2.setOnClickListener(new View.OnClickListener() {
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
        lp.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.741); // 宽度
        lp.height = (int) (context.getResources().getDisplayMetrics().heightPixels * 0.298); // 宽度
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
