package com.skylin.uav.drawforterrain.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import android.view.GestureDetector;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.skylin.uav.drawforterrain.nofly.Point;

import com.skylin.uav.R;

import java.util.ArrayList;
/**
 * Created by wh on 2017/4/1.
 */
public class BarrierAdapter extends RecyclerView.Adapter<BarrierAdapter.ViewHolder> {

    private ArrayList<Point> list;
    private Context context;
    private int indext = -1;
    private int Tag;
    private int deleat;
    private boolean delete = false;
    private boolean clickAble = true;
    private GestureDetector mGestuerDetector;

    public boolean isClickAble() {
        return clickAble;
    }

    public void setClickAble(boolean clickAble) {
        this.clickAble = clickAble;
    }

    public int getDeleat() {
        return deleat;
    }

    public void setDeleat(int deleat) {
        this.deleat = deleat;
    }

    public boolean getDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }


    public BarrierAdapter(ArrayList<Point> list, int Tag,Context context) {
        this.list = list;
        this.Tag = Tag;
        this.context = context;
    }


    public int getTag() {
        return Tag;
    }

    public void setTag(int tag) {
        Tag = tag;
    }

    public int getIndext() {
        return indext;
    }

    public void setIndext(int indext) {
        this.indext = indext;
    }

    private ItemClickListener itemClickListener;

    public interface ItemClickListener {
        void onItemClicked(View view, int position);
    }

    //设置点击回调接口
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    private LongClickListener longClickListener;

    public interface LongClickListener {
        void onLongClicked(View view, int position);
    }

    //设置长按点击回调接口
    public void setLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.barrier_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.barrier_item_number.setTag(Tag);
        holder.barrier_item_delete.setTag(Tag);
        holder.barrier_item_number.setText((position+1) + "");

        if (clickAble) {

            holder.barrier_item_number.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClicked(v, position);
                }
            });
            holder.barrier_item_number.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (longClickListener != null) {
                        longClickListener.onLongClicked(v, position);
                    }
                    return true;
                }
            });
            holder.barrier_item_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (itemClickListener != null)
                        itemClickListener.onItemClicked(v, position);
                }
            });
        }
        if (!delete) {
            holder.barrier_item_delete.setVisibility(View.INVISIBLE);
            if (position == indext) {
                holder.barrier_item_number.setBackgroundResource(R.drawable.editsharp6);
                holder.barrier_item_number.setTextColor(context.getResources().getColor(R.color.white));
            } else {
                holder.barrier_item_number.setBackgroundResource(R.drawable.editsharp7);
                holder.barrier_item_number.setTextColor(context.getResources().getColor(R.color.text_gray));
            }
        } else {
            holder.barrier_item_delete.setVisibility(View.VISIBLE);
            if (position == deleat) {
                holder.barrier_item_number.setBackgroundResource(R.drawable.editsharp6);
                holder.barrier_item_number.setTextColor(context.getResources().getColor(R.color.white));
            } else {
                holder.barrier_item_number.setBackgroundResource(R.drawable.editsharp7);
                holder.barrier_item_number.setTextColor(context.getResources().getColor(R.color.text_gray));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size() == 0 ? 0 : list.size();
        } else {
            return 0;
        }

    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView barrier_item_number;
       ImageButton barrier_item_delete;
        public ViewHolder(View itemView) {
            super(itemView);
            barrier_item_number = (TextView) itemView.findViewById(R.id.barrier_item_number);
            barrier_item_delete = (ImageButton) itemView.findViewById(R.id.barrier_item_delete);
        }
    }
}
