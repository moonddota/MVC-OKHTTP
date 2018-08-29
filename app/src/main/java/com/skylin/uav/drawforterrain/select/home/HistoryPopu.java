package com.skylin.uav.drawforterrain.select.home;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.skylin.uav.R;
import com.skylin.uav.drawforterrain.APP;
import com.skylin.uav.drawforterrain.BaseActivity;
import com.skylin.uav.drawforterrain.BlueToochActivity;
import com.skylin.uav.drawforterrain.checksupdata.JsonGenericsSerializator;
import com.skylin.uav.drawforterrain.setting_channel.DownMappingBan;
import com.skylin.uav.drawforterrain.setting_channel.db.DbBan;
import com.skylin.uav.drawforterrain.setting_channel.db.DbSQL;
import com.skylin.uav.drawforterrain.util.DateUtil;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.GenericsCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import sjj.alog.Log;

/**
 * Created by Moon on 2018/3/8.
 */

public class HistoryPopu {
    private View view;//在哪个view的下面
    private PopupWindow popupWindow;
    private Activity mactivity;
    private int id;
    private RecyclerView history_recycle;
    private ImageView history_refresh;
    private CommonAdapter adapter;
    private ArrayList<DownMappingBan.DataBean.InfoBean> clickList = new ArrayList<DownMappingBan.DataBean.InfoBean>();
    private View popView;
    private DbSQL sql;


    public HistoryPopu(Activity activity, View view, int id,DbSQL sql) {
        this.view = view;
        this.mactivity = activity;
        this.id = id;
        this.sql = sql;
        init();
    }

    public void init() {
        DisplayMetrics dm = new DisplayMetrics();
        mactivity.getWindowManager().getDefaultDisplay().getMetrics(dm);//获取屏幕信息
        LayoutInflater inflater = LayoutInflater.from(mactivity);
        popView = inflater.inflate(R.layout.popu_history, null);

        adapter = new CommonAdapter<DownMappingBan.DataBean.InfoBean>(mactivity, R.layout.item_historypopu, clickList) {

            @Override
            protected void convert(ViewHolder holder, final DownMappingBan.DataBean.InfoBean infoBean, final int position) {
                holder.setText(R.id.history_title, infoBean.getTitle());
                holder.setText(R.id.history_time, infoBean.getCreate_time());
                holder.setText(R.id.history_name, infoBean.getCreate_name());



                String locale = Locale.getDefault().toString();
                if (locale.equals("zh_CN")) {
                    holder.setText(R.id.history_area, infoBean.getArea());
                } else {
                    DecimalFormat df = new DecimalFormat("#0.000");
                    String format = df.format((Double.parseDouble(infoBean.getArea()) / 15));
                    holder.setText(R.id.history_area, format);
                }
            }
        };
        adapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                if (click != null) {
                    click.onPopuClick(view, position, clickList.get(position));
                }
            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                return false;
            }
        });

        history_recycle = popView.findViewById(R.id.history_recycle);
        history_recycle.setLayoutManager(new LayoutManager(mactivity));
        history_recycle.setHasFixedSize(true);
        history_recycle.setAdapter(adapter);

        history_refresh = popView.findViewById(R.id.history_refresh);
        history_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ObjectAnimator anim = ObjectAnimator.ofFloat(history_refresh, "rotation", 0, 360);
                anim.setDuration(500);
                anim.setRepeatCount(3);
                anim.setRepeatMode(ObjectAnimator.RESTART);
                anim.start();
                if (click != null) {
                    click.onPopuClick(v, 1, null);
                }
            }
        });

        initPopu(popView);
    }

    private void initPopu( View popView) {
        DisplayMetrics displayMetrics = mactivity.getResources().getDisplayMetrics();
        if (BaseActivity.isScreenOriatationPortrait(mactivity)) {
            //竖屏
            popupWindow = new PopupWindow(popView, displayMetrics.widthPixels, (int) (displayMetrics.heightPixels *0.4));
        } else {
            //横屏
            popupWindow = new PopupWindow(popView, (int) (displayMetrics.widthPixels*0.6), (int) (displayMetrics.heightPixels *0.6));
        }
        popupWindow.setAnimationStyle(R.style.dialogstyle);//弹出动画
        popupWindow.setFocusable(true);//PopupWindow是否具有获取焦点的能力，默认为False。
        popupWindow.setOutsideTouchable(true);//设置PopupWindow是否响应外部点击事件，默认是true
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        popupWindow.setBackgroundDrawable(new BitmapDrawable());//使点击popupwindow以外的区域时popupwindow自动消失须放在showAsDropDown之前
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(mactivity, 1f);
            }
        });
    }

    public void backgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(lp);
    }


    public void show(ArrayList<DownMappingBan.DataBean.InfoBean> list) {
        backgroundAlpha(mactivity, 0.5f);
//        popupWindow.showAtLocation(view.findViewById(id), Gravity.BOTTOM|Gravity.CENTER, 0, 0);

        popupWindow.showAtLocation(mactivity.findViewById(id), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
//        popupView.startAnimation(animation);

        clickList.clear();
        for (DownMappingBan.DataBean.InfoBean infoBean:list) {
              clickList.add(infoBean);
        }
        adapter.notifyDataSetChanged();
    }

    public void dismiss() {
        popupWindow.dismiss();
        backgroundAlpha(mactivity, 1f);
    }

    public boolean isShow(){
        return popupWindow.isShowing();
    }


    public interface HistoryPopuClick {
        void onPopuClick(View view, int id, DownMappingBan.DataBean.InfoBean bean);
    }

    private HistoryPopuClick click;

    public void setPopuCloseListener(HistoryPopuClick closeListener) {
        this.click = closeListener;
    }
}
