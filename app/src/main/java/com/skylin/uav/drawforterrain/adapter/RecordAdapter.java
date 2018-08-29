package com.skylin.uav.drawforterrain.adapter;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.skylin.uav.drawforterrain.setting_channel.DownMappingBan;
import com.skylin.uav.drawforterrain.views.MarqueeTextView;
import com.skylin.uav.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Moon on 2018/3/9.
 */

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {
    private Activity mActivity;
    private ArrayList<DownMappingBan.DataBean.InfoBean> list;

    public RecordAdapter(Activity mActivity, ArrayList<DownMappingBan.DataBean.InfoBean> list) {
        this.mActivity = mActivity;
        this.list = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mActivity).inflate(R.layout.item_mappingrecord, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final DownMappingBan.DataBean.InfoBean ban = list.get(position);

        String area = ban.getArea();
        if (!TextUtils.isEmpty(area)) {
            DecimalFormat df = new DecimalFormat("#0.00");
            String format = df.format(Double.valueOf(area));
            holder.area_mappingrecord.setText(format);
        }

        holder.name_mappingrecord.setText(ban.getTitle());

        holder.create_name.setText(ban.getCreate_name());

        holder.adress_mappingrecord.setText(ban.getProvince() + ban.getCity() + ban.getDistrict());

        if (TextUtils.isEmpty(ban.getUpdate_time()))
            holder.time_mappingrecord.setText(ban.getCreate_time());
        else
            holder.time_mappingrecord.setText(ban.getUpdate_time());

        holder.sync_mappingrecord.setVisibility(ban.getSync() ? View.INVISIBLE : View.VISIBLE);

        holder.cardview_mappingrecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click != null) {
                    click.onPopuClick(v, ban);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView area_mappingrecord;
        TextView name_mappingrecord;
        MarqueeTextView create_name;
        MarqueeTextView adress_mappingrecord;
        TextView time_mappingrecord;
        FrameLayout sync_mappingrecord;
        CardView cardview_mappingrecord;

        public ViewHolder(View v) {
            super(v);
            cardview_mappingrecord = v.findViewById(R.id.cardview_mappingrecord);
            sync_mappingrecord = v.findViewById(R.id.sync_mappingrecord);
            area_mappingrecord = v.findViewById(R.id.area_mappingrecord);
            setText(area_mappingrecord);
            name_mappingrecord = v.findViewById(R.id.name_mappingrecord);
            setText(name_mappingrecord);
            create_name = v.findViewById(R.id.create_name);
            setText(create_name);
            adress_mappingrecord = v.findViewById(R.id.adress_mappingrecord);
            setText(adress_mappingrecord);
            time_mappingrecord = v.findViewById(R.id.time_mappingrecord);
            setText(time_mappingrecord);
        }

        private void setText(TextView text) {
            text.setMarqueeRepeatLimit(-1);
            text.setSingleLine(true);
            text.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            text.setHorizontallyScrolling(true); //让文字可以水平滑动
        }
    }

    public interface PopuClick {
        void onPopuClick(View view, DownMappingBan.DataBean.InfoBean infoBean);
    }

    private PopuClick click;

    public void setOnTiemClickListener(PopuClick closeListener) {
        this.click = closeListener;
    }
}
