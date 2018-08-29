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

import com.skylin.uav.drawforterrain.setting_channel.OnClick;
import com.skylin.uav.R;

/**
 * Created by Moon on 2018/3/15.
 */

public class Choicedialog  extends Dialog{


    public Choicedialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);


        LinearLayout root = (LinearLayout) LayoutInflater.from(context).inflate(
                R.layout.layout_camera_control, null);
        root.findViewById(R.id.btn_open_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click !=null){
                    click.onPopuClick(v,v.getId());
                }
            }
        });
        root.findViewById(R.id.btn_choose_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click !=null){
                    click.onPopuClick(v,v.getId());
                }
            }
        });
        root.findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click !=null){
                    click.onPopuClick(v,v.getId());
                }
            }
        });
        setContentView(root);
        Window dialogWindow = getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        dialogWindow.setWindowAnimations(R.style.dialogstyle); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = -20; // 新位置Y坐标
        lp.width = (int) context.getResources().getDisplayMetrics().widthPixels; // 宽度
      lp.height = WindowManager.LayoutParams.WRAP_CONTENT; // 高度
//        root.measure(0, 0);
//        lp.height = root.getMeasuredHeight();
        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
    }

    private OnClick click;

    public void setPopuCloseListener( OnClick closeListener){
        this.click = closeListener;
    }

}
