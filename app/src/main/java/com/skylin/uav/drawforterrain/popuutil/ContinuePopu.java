package com.skylin.uav.drawforterrain.popuutil;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.skylin.uav.drawforterrain.setting_channel.OnClick;
import com.skylin.uav.R;

/**
 * Created by Moon on 2018/3/8.
 */

public class ContinuePopu {
    private View view;//在哪个view的下面
    private PopupWindow popupWindow;
    private Activity mactivity;
    private int id;
    private  TextView tv_single_operation_dialog_confirm;
    private  TextView tv_single_cancel;
    private  TextView tv_single_operation_dialog_title;
    private TextView tv_single_operation_dialog_message;

    public ContinuePopu(Activity activity, View view, int id) {
        this.view = view;
        this.mactivity = activity;
        this.id = id;
        init();
    }

    public void init() {
        DisplayMetrics dm = new DisplayMetrics();
        mactivity.getWindowManager().getDefaultDisplay().getMetrics(dm);//获取屏幕信息
        LayoutInflater inflater = LayoutInflater.from(mactivity);
        View popView = inflater.inflate(R.layout.view_dialog_single_operation, null);


//        FrameLayout popu_group = popView.findViewById(R.id.popu_group);
//        popu_group.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                popupWindow.dismiss();
//            }
//        });

        tv_single_operation_dialog_title = popView.findViewById(R.id.tv_single_operation_dialog_title);
        tv_single_operation_dialog_title.setText("提示");
        tv_single_operation_dialog_message = popView.findViewById(R.id.tv_single_operation_dialog_message);
        tv_single_operation_dialog_message.setText("您正在测绘，开始测绘后当前任务会丢失，确定要开始新的任务吗？");

        tv_single_operation_dialog_confirm = popView.findViewById(R.id.tv_single_operation_dialog_confirm);
        tv_single_operation_dialog_confirm.setText("开始新任务");
        tv_single_operation_dialog_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click!=null){
                    click.onPopuClick(v,v.getId());
                }
            }
        });
        tv_single_cancel = popView.findViewById(R.id.tv_single_cancel);
        tv_single_cancel.setText("继续当前任务");
        tv_single_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click!=null){
                    click.onPopuClick(v,v.getId());
                }
            }
        });

        popupWindow = new PopupWindow(popView, -1, -1);
        popupWindow.setAnimationStyle(R.style.mypopwindow_anim_style);//弹出动画
        popupWindow.setFocusable(true);//PopupWindow是否具有获取焦点的能力，默认为False。
        popupWindow.setOutsideTouchable(true);//设置PopupWindow是否响应外部点击事件，默认是true
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        popupWindow.setBackgroundDrawable(new BitmapDrawable());//使点击popupwindow以外的区域时popupwindow自动消失须放在showAsDropDown之前
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(mactivity,1f);
            }
        });
    }

    public void backgroundAlpha(Activity context, float bgAlpha) {
//        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
//        lp.alpha = bgAlpha;
//        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        context.getWindow().setAttributes(lp);
    }

    public void setText(String titlr,String msage,String s1,String s2){
        tv_single_operation_dialog_title.setText(titlr);
        tv_single_operation_dialog_message.setText(msage);
        tv_single_operation_dialog_confirm.setText(s1);
        tv_single_cancel.setText(s2);
    }

    public void show(){
        backgroundAlpha(mactivity,0.5f);
        popupWindow.showAtLocation(view.findViewById(id), Gravity.CENTER, 0, 0);
    }
    public void dismiss(){
        popupWindow.dismiss();
        backgroundAlpha(mactivity,1f);
    }

    private OnClick click;

    public void setPopuCloseListener( OnClick closeListener){
        this.click = closeListener;
    }
}
