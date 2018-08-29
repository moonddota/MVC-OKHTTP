package com.skylin.uav.drawforterrain.select.rtk;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skylin.uav.R;

import java.util.List;

/**
 * Created by moon on 2017/12/28.
 */

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ViewHodel> {

    private Activity mActivity;
    private List<Integer> list;
    private int index = -1;

    public ChannelAdapter(Activity mActivity, List<Integer> list) {
        this.mActivity = mActivity;
        this.list = list;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int i) {
        index = i;
    }


    public interface OnClick {
        void getClick(View view, int i);
    }

    private OnClick onClick;

    public void getView(OnClick onClick) {
        this.onClick = onClick;
    }


    @Override
    public ViewHodel onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHodel(LayoutInflater.from(mActivity).inflate(R.layout.item_channel, parent, false));
    }

    @Override
    public void onBindViewHolder(ChannelAdapter.ViewHodel holder, final int position) {
        holder.tv_channnel_item.setText(list.get(position) + "");
//        Log.e(index  +"   "+position);
        if (index == position) {
            holder.cardView.setBackgroundResource(R.drawable.buttonbackground);
        } else {
            holder.cardView.setBackgroundResource(R.drawable.buttonbackground1);
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClick != null) {
                    onClick.getClick(v, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHodel extends RecyclerView.ViewHolder {
        TextView tv_channnel_item;
        LinearLayout cardView;

        public ViewHodel(View itemView) {
            super(itemView);
            tv_channnel_item = itemView.findViewById(R.id.tv_channnel_item);
            cardView = itemView.findViewById(R.id.item_channnel_layout);
        }
    }
}
